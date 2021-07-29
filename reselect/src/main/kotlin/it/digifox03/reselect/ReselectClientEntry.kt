package it.digifox03.reselect

import it.digifox03.reselect.parser.ConstantIdentifierParser
import it.digifox03.reselect.parser.V1Parser
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resource.ResourceType

class ReselectClientEntry: ClientModInitializer {
    override fun onInitializeClient() {
        V1Parser.register()
        ConstantIdentifierParser.register()

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(ReselectResourceManager())
    }
}
