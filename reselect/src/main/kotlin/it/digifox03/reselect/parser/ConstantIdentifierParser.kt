package it.digifox03.reselect.parser

import it.digifox03.reselect.ReselectorCompiler
import it.digifox03.reselect.ReselectorParser
import it.digifox03.reselect.ReselectorParserContainer
import it.digifox03.reselect.compiler.ConstantIdentifierCompiler
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.util.Identifier

internal object ConstantIdentifierParser: ReselectorParser {
    fun register() {
        ReselectorParserContainer.v1.registerParser(
            inType = Nothing::class.java,
            outType = Identifier::class.java,
            key = "identifier",
            parser = this
        )
    }

    override fun parse(
        element: JsonObject,
        helper: ReselectorParser.ReselectorHelper
    ): ReselectorCompiler {
        val idField = element["\$identifier"] ?: element["identifier"]
        check(idField is JsonPrimitive) { "the identifier field must contain a valid identifier" }
        val identifier = checkNotNull(Identifier.tryParse(idField.content)) {
            "the identifier field must contain a valid identifier"
        }
        return ConstantIdentifierCompiler(identifier)
    }
}
