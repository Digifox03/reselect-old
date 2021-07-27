package it.digifox03.reselect

import it.digifox03.reselect.api.ReselectorUser
import it.digifox03.reselect.compiler.ReselectorGenerator
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.util.Identifier

internal class ReselectLoader : SimpleSynchronousResourceReloadListener {
    private val fabricId = Identifier("reselect", "reselectors")
    override fun getFabricId() = fabricId
    private fun loader(manager: ResourceManager, identifier: Identifier): ReselectorGenerator {
        TODO()
    }

    override fun reload(manager: ResourceManager) {
        val users = FabricLoader.getInstance()
            .getEntrypoints("reselector_user", ReselectorUser::class.java)

        for (user in users) {
            val compiler = ReselectorLoader(user.javaClass.classLoader) { loader(manager, it) }
            user.onReselectorReload(compiler)
        }
    }
}

@Suppress("unused")
fun clientEntry() {
    ResourceManagerHelper
        .get(ResourceType.CLIENT_RESOURCES)
        .registerReloadListener(ReselectLoader())
}
