package it.digifox03.reselect.compiler

import it.digifox03.reselect.api.ReselectMethod
import net.minecraft.util.Identifier
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.ClassWriter.COMPUTE_MAXS
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import java.lang.reflect.Method

private fun getMethod(type: Class<*>): Method {
    require(type.isInterface) {
        "'$type' must be an interface"
    }
    val validMethods = type.methods.filter {
        !it.isDefault || it.isAnnotationPresent(ReselectMethod::class.java)
    }
    require(validMethods.isNotEmpty()) {
        "there must be at least one candidate method to override in '$type'"
    }
    require(validMethods.size == 1) {
        val methods = validMethods.joinToString(", ") { "'$it'" }
        "the type '$type' must have only one candidate method to override. Candidates are $methods"
    }
    val method = validMethods[0]
    require(method.returnType.isAssignableFrom(Identifier::class.java)) {
        "the return type of '$method' for '$type' must return '${Identifier::class.java}'"
    }
    require(method.typeParameters.isEmpty()) {
        "the method '$method' for '$type' must not have type parameters"
    }
    require(method.isAnnotationPresent(ReselectMethod::class.java)) {
        "the method '$method' for '$type' must be annotated with '@ReselectMethod'"
    }
    return method
}

private fun getFields(method: Method): Array<Triple<String, Int, Type>> {
    var pos = 1
    val params = Type.getArgumentTypes(method)
    return Array(method.parameters.size) {
        val name = requireNotNull(method.parameters[it].name) {
            "method $method must have a name for all parameters"
        }
        val type = params[it]
        Triple(name, pos, type).also<Triple<String, Int, Type>> {
            pos += type.size
        }
    }
}

fun compile(
    className: String,
    clazz: Class<*>,
    generator: ReselectorGenerator
): ByteArray {
    val target = getMethod(clazz)
    val fields = getFields(target)
    val classVisitor = ClassWriter(COMPUTE_MAXS)
    val superType = Type.getType(java.lang.Object::class.java)
    val implements = Type.getType(clazz)
    val mainMethodDescriptor = Type.getMethodDescriptor(Type.getReturnType(target))
    val mainMethodName = "main$"

    generator.className = className

    /// class header
    classVisitor.visit(
        V16,
        ACC_PUBLIC or ACC_SUPER,
        className,
        null,
        superType.internalName,
        arrayOf(implements.internalName)
    )

    /// parameter fields
    for ((name, _, type) in fields) {
        val fieldVisitor = classVisitor.visitField(
            ACC_PRIVATE, name, type.descriptor, null, null
        )
        fieldVisitor.visitEnd()
    }

    /// fields required by generated code
    generator.genFields(classVisitor)

    /// constructor method
    val constructorVisitor = classVisitor.visitMethod(
        ACC_PUBLIC, "<init>", "()V", null, null
    )
    constructorVisitor.visitCode()
    constructorVisitor.visitVarInsn(ALOAD, 0)
    constructorVisitor.visitMethodInsn(INVOKESPECIAL, superType.internalName, "<init>", "()V", false)
    generator.initFunc(constructorVisitor)
    constructorVisitor.visitInsn(RETURN)
    constructorVisitor.visitMaxs(0, 0)
    constructorVisitor.visitEnd()

    /// override method
    val targetVisitor = classVisitor.visitMethod(
        ACC_PUBLIC, target.name, Type.getMethodDescriptor(target), null,
        Array(target.exceptionTypes.size) { Type.getInternalName(target.exceptionTypes[it]) }
    )
    targetVisitor.visitCode()
    targetVisitor.visitVarInsn(ALOAD, 0)
    for ((name, location, type) in fields) {
        targetVisitor.visitInsn(DUP)
        targetVisitor.visitVarInsn(type.getOpcode(ILOAD), location)
        targetVisitor.visitFieldInsn(PUTFIELD, className, name, type.descriptor)
    }
    targetVisitor.visitMethodInsn(
        INVOKESPECIAL, className, mainMethodName, mainMethodDescriptor, false
    )
    targetVisitor.visitInsn(ARETURN)
    targetVisitor.visitMaxs(0, 0)
    targetVisitor.visitEnd()

    /// main method
    val mainVisitor = classVisitor.visitMethod(
        ACC_PRIVATE, mainMethodName, mainMethodDescriptor, null, null
    )
    mainVisitor.visitCode()
    generator.mainFunc(mainVisitor)
    mainVisitor.visitMaxs(0, 0)
    mainVisitor.visitEnd()

    /// required methods
    generator.genMethods(classVisitor)

    /// static method
    val staticVisitor = classVisitor.visitMethod(
        ACC_STATIC, "<clinit>", "()V", null, null
    )
    staticVisitor.visitCode()
    generator.clInitFunc(staticVisitor)
    staticVisitor.visitInsn(RETURN)
    staticVisitor.visitMaxs(0, 0)
    staticVisitor.visitEnd()

    classVisitor.visitEnd()
    return classVisitor.toByteArray()
}
