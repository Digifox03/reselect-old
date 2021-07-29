package it.digifox03.reselect

import kotlinx.serialization.json.JsonElement
import net.minecraft.util.Identifier

internal interface ReselectorParser {
    companion object {
        private val versions = mutableMapOf<String, ReselectorParser>()
        fun addVersion(version: String, parser: ReselectorParser) {
            check(version !in versions) { "attempt to add version '$version' twice" }
            versions[version] = parser
        }
        fun getParser(version: String): ReselectorParser {
            return requireNotNull(versions[version]) {
                "version '$version' is not implemented"
            }
        }
    }

    interface ReselectorHelper {
        val dataSet: Map<String, Class<*>>
        val superReselector: ReselectorCompiler
        fun delegate(id: Identifier): ReselectorCompiler
    }

    fun parse(
        element: JsonElement,
        helper: ReselectorHelper
    ): ReselectorCompiler
}
