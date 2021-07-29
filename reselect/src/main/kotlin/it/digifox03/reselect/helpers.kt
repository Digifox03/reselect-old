package it.digifox03.reselect

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * The type for an object is determined in the following way:
 *  1. If the object has a type field, the key is the content of that field
 *  2. If the object has at least one field which begins with "$",
 *     the key is the combination of all fields which begin with "$".
 *     The combination of multiple fields is the sorted concatenation of the fields;
 *     to avoid ambiguity, all "$" in the field except the first are doubled.
 */
internal val JsonObject.type: String get() =
    (this["type"] as? JsonPrimitive)?.contentOrNull ?:
    keys
        .filter { it.startsWith("$") }
        .map { it.removePrefix("$").replace("$", "$$") }
        .sorted()
        .joinToString("$")
