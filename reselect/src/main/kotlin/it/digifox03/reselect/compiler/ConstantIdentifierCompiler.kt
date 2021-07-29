package it.digifox03.reselect.compiler

import net.minecraft.util.Identifier
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

internal class ConstantIdentifierCompiler(
    private val identifier: Identifier,
): AbstractReselectorGenerator() {
    private val field by uniqueName()
    private val fieldType = Type.getType(Identifier::class.java)

    override fun ClassVisitor.members() {
        visitField(
            ACC_STATIC or ACC_PRIVATE, field, fieldType.descriptor, null, null
        ).apply {
            visitEnd()
        }
    }

    override fun MethodVisitor.clInit() {
        visitTypeInsn(NEW, fieldType.internalName)
        visitInsn(DUP)
        visitLdcInsn(identifier.namespace)
        visitLdcInsn(identifier.path)
        visitMethodInsn(
            INVOKESPECIAL, fieldType.internalName,
            "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", false
        )
        visitFieldInsn(PUTSTATIC, className, field, fieldType.descriptor)
    }

    override fun MethodVisitor.main() {
        visitFieldInsn(GETSTATIC, className, field, fieldType.descriptor)
        visitInsn(ARETURN)
    }
}
