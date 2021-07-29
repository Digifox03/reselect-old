package it.digifox03.reselect.parser

import it.digifox03.reselect.ReselectorCompiler
import it.digifox03.reselect.ReselectorParser
import it.digifox03.reselect.ReselectorParserContainer
import it.digifox03.reselect.type
import kotlinx.serialization.json.JsonObject
import net.minecraft.util.Identifier

internal object V1Parser: ReselectorParser {
    fun register() {
        ReselectorParser.addVersion("0.1", this)
    }

    override fun parse(
        element: JsonObject,
        helper: ReselectorParser.ReselectorHelper
    ): ReselectorCompiler {
        val root = checkNotNull(element["root"] as? JsonObject) {
            "the root field must contain a valid object"
        }
        return ReselectorParserContainer.v1.getParser(
            Nothing::class.java,
            Identifier::class.java,
            root.type
        ).parse(root, helper)
    }
}
