package it.digifox03.reselect

import it.digifox03.reselect.api.ReselectMethod
import it.digifox03.reselect.compiler.ReselectorGenerator
import it.digifox03.reselect.parser.ReselectorParser
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.util.Identifier
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.reflect.Method
import it.digifox03.reselect.api.ReselectorCompiler as IReselectorCompiler

class ReselectCompiler(
    private val generation: String,
    private val readReselector: (id: Identifier, level: Int) -> JsonElement,
    private val loadClass: (data: ByteArray) -> Class<*>
): IReselectorCompiler {
    override fun <T : Any> get(id: Identifier, type: Class<T>): T {
        val internalClassName = newClassName(id)
        val method = findTargetMethod(type)
        val dataSet = getDataSet(method)
        val generator = buildGenerator(id, 0, dataSet)
        val classData = compile(generator, type, method, internalClassName)
        val clazz = loadClass(classData)
        val instance = clazz.getConstructor().newInstance()
        return type.cast(instance)
    }

    private var classCounter = 0
    private fun newClassName(id: Identifier): String {
        return "reselector/$generation/${classCounter++}/${id.namespace}/${id.path}"
    }

    private fun findTargetMethod(type: Class<*>): Method {
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

    private fun getDataSet(method: Method): Map<String, Class<*>> {
        return method.parameters.associate {
            val name = requireNotNull(it.name) {
                "all parameters of the method '$method' must have a name"
            }
            name to it.type
        }
    }

    private fun buildGenerator(id: Identifier, depth: Int, dataSet: Map<String, Class<*>>): ReselectorGenerator {
        val helper = object : ReselectorParser.ReselectorHelper {
            override val superReselector get() = buildGenerator(id, depth + 1, dataSet)
            override fun delegate(id: Identifier) = buildGenerator(id, 0, dataSet)
        }
        val jsonObject = requireNotNull(readReselector(id, depth) as? JsonObject) {
            "the contents of $id must be an identifier"
        }
        val version = requireNotNull(jsonObject["version"] as? JsonPrimitive) {
            "the contents of $id version must be a string or a number"
        }.content
        return ReselectorParser.getParser(version).parse(jsonObject, helper)
    }

    private fun compile(
        generator: ReselectorGenerator,
        type: Class<*>,
        target: Method,
        className: String // the internal one must be used
    ): ByteArray {
        val fields = getFields(target)
        val superClass = Type.getType(java.lang.Object::class.java)
        val implements = Type.getType(type)
        val mainDesc = Type.getMethodDescriptor(Type.getReturnType(target))
        initializeGenerator(generator, className)
        return compileClass(className, superClass, implements) {
            generator.genMembers(this@compileClass)
            compileParameterFields(fields)
            compileInit(superClass, generator)
            compileTarget(target, fields, className, mainDesc)
            compileMain(mainDesc, generator)
            compileConst(generator)
        }
    }

    private fun initializeGenerator(generator: ReselectorGenerator, className: String) {
        generator.setClassName(className)
        var counter = 0
        generator.setNameProvider { "${counter++}$" }
    }

    private fun compileClass(
        className: String,
        superClass: Type,
        implements: Type,
        block: ClassVisitor.() -> Unit
    ): ByteArray {
        val classVisitor = ClassWriter(ClassWriter.COMPUTE_MAXS)
        classVisitor.visit(
            Opcodes.V16,
            Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER,
            className,
            null,
            superClass.internalName,
            arrayOf(implements.internalName)
        )
        block(classVisitor)
        classVisitor.visitEnd()
        return classVisitor.toByteArray()
    }

    private fun ClassVisitor.compileParameterFields(
        fields: Array<Triple<String, Int, Type>>
    ) {
        for ((name, _, fieldType) in fields) {
            val fieldVisitor = visitField(
                Opcodes.ACC_PRIVATE, name, fieldType.descriptor, null, null
            )
            fieldVisitor.visitEnd()
        }
    }

    private fun ClassVisitor.compileInit(
        superClass: Type,
        generator: ReselectorGenerator
    ) {
        val constructorVisitor = visitMethod(
            Opcodes.ACC_PUBLIC, "<init>", "()V", null, null
        )
        constructorVisitor.visitCode()
        constructorVisitor.visitVarInsn(Opcodes.ALOAD, 0)
        constructorVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, superClass.internalName, "<init>", "()V", false)
        generator.initFunc(constructorVisitor)
        constructorVisitor.visitInsn(Opcodes.RETURN)
        constructorVisitor.visitMaxs(0, 0)
        constructorVisitor.visitEnd()
    }

    private fun ClassVisitor.compileTarget(
        target: Method,
        fields: Array<Triple<String, Int, Type>>,
        className: String,
        destDescriptor: String?
    ) {
        val targetVisitor = visitMethod(
            Opcodes.ACC_PUBLIC, target.name, Type.getMethodDescriptor(target), null,
            Array(target.exceptionTypes.size) { Type.getInternalName(target.exceptionTypes[it]) }
        )
        targetVisitor.visitCode()
        targetVisitor.visitVarInsn(Opcodes.ALOAD, 0)
        for ((name, location, fieldType) in fields) {
            targetVisitor.visitInsn(Opcodes.DUP)
            targetVisitor.visitVarInsn(fieldType.getOpcode(Opcodes.ILOAD), location)
            targetVisitor.visitFieldInsn(Opcodes.PUTFIELD, className, name, fieldType.descriptor)
        }
        targetVisitor.visitMethodInsn(
            Opcodes.INVOKESPECIAL, className, "main$", destDescriptor, false
        )
        targetVisitor.visitInsn(Opcodes.ARETURN)
        targetVisitor.visitMaxs(0, 0)
        targetVisitor.visitEnd()
    }

    private fun ClassVisitor.compileMain(
        descriptor: String?,
        generator: ReselectorGenerator
    ) {
        val mainVisitor = visitMethod(
            Opcodes.ACC_PRIVATE, "main$", descriptor, null, null
        )
        mainVisitor.visitCode()
        generator.mainFunc(mainVisitor)
        mainVisitor.visitMaxs(0, 0)
        mainVisitor.visitEnd()
    }

    private fun ClassVisitor.compileConst(
        generator: ReselectorGenerator
    ) {
        val staticVisitor = visitMethod(
            Opcodes.ACC_STATIC, "<clinit>", "()V", null, null
        )
        staticVisitor.visitCode()
        generator.clInitFunc(staticVisitor)
        staticVisitor.visitInsn(Opcodes.RETURN)
        staticVisitor.visitMaxs(0, 0)
        staticVisitor.visitEnd()
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
}
