package com.example.core.database

import org.json.JSONArray
import org.json.JSONObject
import android.util.Log

// Proper Kotlin DTOs to represent the schema of the SQLite JSON columns
data class DefinitionDto(val text: String, val label: String? = null)
data class TranslationDto(val text: String, val language: String? = "Persian")
data class ExampleDto(val text: String, val translation: String? = null)
data class SynonymDto(val text: String)
data class AntonymDto(val text: String)
data class WordFamilyDto(val word: String, val type: String? = null)
data class CollocationDto(val text: String)
data class PhraseDto(val text: String)
data class NoteDto(val text: String)
data class TypeDto(val text: String)

object JsonParserUtils {

    // Parses a JSON string column into proper DTOs and maps them to a list of clean strings.
    fun parseJsonArray(jsonStr: String?): List<String> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        val trimmed = jsonStr.trim()
        if (!trimmed.startsWith("[") && !trimmed.startsWith("{")) {
            // Fallback: simple split
            return trimmed.split(Regex("[,;|\\n]"))
                .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                .filter { it.isNotEmpty() && !it.startsWith("[") && !it.startsWith("{") }
        }
        val list = mutableListOf<String>()
        try {
            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val item = array.get(i)
                    if (item is JSONObject) {
                        val text = extractTextFromObject(item)
                        if (text.isNotEmpty()) list.add(text)
                    } else if (item is JSONArray) {
                        for (j in 0 until item.length()) {
                            list.add(item.optString(j))
                        }
                    } else {
                        val str = item.toString().trim().removeSurrounding("\"")
                        if (str.isNotEmpty()) list.add(str)
                    }
                }
            } else if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                val text = extractTextFromObject(obj)
                if (text.isNotEmpty()) {
                    list.add(text)
                } else {
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = obj.get(key)
                        if (value is JSONArray) {
                            for (i in 0 until value.length()) {
                                list.add(value.optString(i))
                            }
                        } else if (value is JSONObject) {
                            val subText = extractTextFromObject(value)
                            if (subText.isNotEmpty()) list.add(subText)
                        } else {
                            list.add(value.toString())
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("JsonParserUtils", "Error parsing json array: $trimmed", e)
            return trimmed.split(Regex("[,;|\\n]"))
                .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                .filter { it.isNotEmpty() }
        }
        return list.map { cleanString(it) }.filter { it.isNotEmpty() }
    }

    private fun extractTextFromObject(obj: JSONObject): String {
        val keysToTry = listOf(
            "text", "definition", "definition_en", "definition_fa", "meaning", "translation", 
            "example", "examples", "example_en", "example_fa", "word", "value", "fa", "en", 
            "synonym", "antonym", "collocation", "phrase", "note", "type"
        )
        for (key in keysToTry) {
            if (obj.has(key)) {
                val value = obj.opt(key)
                if (value != null && value !is JSONObject && value !is JSONArray) {
                    return value.toString()
                }
            }
        }
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = obj.opt(key)
            if (value != null && value !is JSONObject && value !is JSONArray) {
                return value.toString()
            }
        }
        return ""
    }

    private fun cleanString(str: String): String {
        var res = str.replace(Regex("\\\\[\"']"), "\"")
            .trim()
        while (res.startsWith("\"") && res.endsWith("\"") && res.length >= 2) {
            res = res.substring(1, res.length - 1)
        }
        while (res.startsWith("'") && res.endsWith("'") && res.length >= 2) {
            res = res.substring(1, res.length - 1)
        }
        return res.trim()
    }
}
