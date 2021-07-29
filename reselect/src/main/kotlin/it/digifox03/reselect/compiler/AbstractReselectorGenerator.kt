package it.digifox03.reselect.compiler

import it.digifox03.reselect.ReselectorCompiler
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

internal abstract class AbstractReselectorGenerator: ReselectorCompiler {
    private val children = mutableListOf<ReselectorCompiler>()

    protected fun addChild(vararg children: ReselectorCompiler) {
        this.children.addAll(children)
    }

    private lateinit var name: String
    protected val className: String get() = name
    final override fun setClassName(className: String) {
        name = className
        for (child in children) {
            child.setClassName(className)
        }
    }

    private lateinit var nameProvider: () -> String
    final override fun setNameProvider(provider: () -> String) {
        nameProvider = provider
    }

    protected fun uniqueName(): Lazy<String> = lazy { nameProvider() }

    protected open fun MethodVisitor.clInit() {}
    final override fun clInitFunc(visitor: MethodVisitor) {
        for (child in children) {
            child.clInitFunc(visitor)
        }
        visitor.clInit()
    }

    protected open fun MethodVisitor.init() {}
    final override fun initFunc(visitor: MethodVisitor) {
        for (child in children) {
            child.initFunc(visitor)
        }
        visitor.init()
    }

    protected open fun MethodVisitor.main() {}
    final override fun mainFunc(visitor: MethodVisitor) {
        for (child in children) {
            child.mainFunc(visitor)
        }
        visitor.main()
    }

    protected open fun ClassVisitor.members() {}
    final override fun genMembers(visitor: ClassVisitor) {
        for (child in children) {
            child.genMembers(visitor)
        }
        visitor.members()
    }

    protected fun MethodVisitor.visitGenerator(generator: ReselectorCompiler) {
        generator.mainFunc(this)
    }
}