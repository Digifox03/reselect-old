package it.digifox03.reselect

internal class ReselectorParserContainer {
    companion object {
        val v1 = ReselectorParserContainer()
    }

    private data class ReselectorParserData(
        val parser: ReselectorParser,
        val inType: Class<*>,
        val outType: Class<*>
    )

    private val parserBase = mutableMapOf<String, ReselectorParserData>()
    fun registerParser(inType: Class<*>, outType: Class<*>, key: String, parser: ReselectorParser) {
        check(key !in parserBase) {
            "attempt to register a parser $key twice"
        }
        parserBase[key] = ReselectorParserData(parser, inType, outType)
    }
    fun getParser(inType: Class<*>, outType: Class<*>, key: String): ReselectorParser {
        val (eParser, eInType, eOutType) = requireNotNull(parserBase[key]) { "Unknown key $key" }
        check(eInType.isAssignableFrom(inType)) {
            "required object with in type $inType, but $eInType was found"
        }
        check(outType.isAssignableFrom(eOutType)) {
            "required object with out type $outType, but $eOutType was found"
        }
        return eParser
    }
}
