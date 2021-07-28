package it.digifox03.reselect.compiler

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

internal interface ReselectorGenerator {
    fun setNameProvider(provider: () -> String)
    fun setClassName(className: String)

    /**
     * generate code for the main function (root must always return something)
     */
    fun mainFunc(visitor: MethodVisitor)

    /**
     * generate code for the constructor
     */
    fun initFunc(visitor: MethodVisitor)

    /**
     * generate code for the constant initialization block
     */
    fun clInitFunc(visitor: MethodVisitor)

    /**
     * generate all needed fields and methods
     */
    fun genMembers(visitor: ClassVisitor)
}
