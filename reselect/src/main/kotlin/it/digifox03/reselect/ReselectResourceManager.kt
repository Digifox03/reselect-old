package it.digifox03.reselect

import it.digifox03.reselect.api.ReselectorUser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

internal class ReselectResourceManager : SimpleSynchronousResourceReloadListener {
    private val fabricId = Identifier("reselect", "reselectors")
    private var generation = 0
    override fun getFabricId() = fabricId

    private fun load(manager: ResourceManager, identifier: Identifier, level: Int): JsonElement {
        return if (level == 0) {
            val resource = manager.getResource(identifier)
            val element = Json.parseToJsonElement(resource.inputStream.reader().readText())
            resource.close()
            element
        } else {
            val resources = manager.getAllResources(identifier)
            val resource = resources[resources.size - level]
            val element = Json.parseToJsonElement(resource.inputStream.reader().readText())
            resources.forEach { it.close() }
            element
        }
    }

    override fun reload(manager: ResourceManager) {
        val users = FabricLoader.getInstance()
            .getEntrypoints("reselector_user", ReselectorUser::class.java)

        for (user in users) {
            val loader = ReselectClassLoader(user.javaClass.classLoader)
            val compiler = ReselectCompiler(
                "${generation++}",
                { id, level -> load(manager, id, level) },
                loader::loadClass
            )
            user.onReselectorReload(compiler)
        }
    }
}
