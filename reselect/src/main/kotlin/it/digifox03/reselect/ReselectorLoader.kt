package it.digifox03.reselect

import it.digifox03.reselect.api.ReselectorCompiler
import it.digifox03.reselect.compiler.ReselectorGenerator
import it.digifox03.reselect.compiler.compile
import net.minecraft.util.Identifier

internal class ReselectorLoader(
    parent: ClassLoader,
    val loader: (Identifier) -> ReselectorGenerator?
) : ClassLoader(parent), ReselectorCompiler {
    private fun getGenerator(id: Identifier): ReselectorGenerator {
        return requireNotNull(loader(id)) {
            "the mod that uses the reselector '$id' must provide or ensure that a root reselector with that name exists"
        }
    }

    private fun <T> load(id: Identifier, type: Class<T>, internalName: String, qualifiedName: String): Class<*> {
        val data = compile(internalName, type, getGenerator(id))
        val clazz = defineClass(qualifiedName, data, 0, data.size)
        check(type.isAssignableFrom(clazz)) {
            "The generated class does not conform to the specified interface, this is an error within Reselect"
        }
        return clazz
    }

    override fun <T : Any> get(id: Identifier, type: Class<T>): T {
        // hopefully i can just use the package "reselector" without causing issues
        val internalName = "reselector/${id.namespace}/${id.path}"
        val qualifiedName = internalName.replace('/', '.')
        val clazz = findLoadedClass(qualifiedName) ?: load(id, type, internalName, qualifiedName)
        val instance = clazz.getConstructor().newInstance()
        require(type.isInstance(instance)) {
            "All requests for the same reselector '$id' must have the same type."
        }
        return type.cast(instance)
    }
}
