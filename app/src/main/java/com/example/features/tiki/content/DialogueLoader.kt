package com.example.features.tiki.content

import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class DialogueLoader {

    fun loadFromString(jsonStr: String): DialogueLibrary {
        val root = JSONObject(jsonStr)
        val version = root.optString("version", "1.0")
        val language = root.optString("language", "en")
        val dialoguesArray = root.getJSONArray("dialogues")
        
        val dialoguesList = mutableListOf<DialogueMetadata>()
        for (i in 0 until dialoguesArray.length()) {
            val item = dialoguesArray.getJSONObject(i)
            
            val tagsList = mutableListOf<String>()
            val tagsArray = item.optJSONArray("tags")
            if (tagsArray != null) {
                for (j in 0 until tagsArray.length()) {
                    tagsList.add(tagsArray.getString(j))
                }
            }
            
            val additionalProps = mutableMapOf<String, Any>()
            val standardKeys = setOf(
                "id", "text", "emotion", "category", "priority", 
                "relationshipLevel", "minimumStreak", "maximumStreak", 
                "minimumSessionProgress", "maximumSessionProgress", 
                "thinkingState", "cooldown", "weight", "language", "enabled", "tags"
            )
            val keys = item.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                if (key !in standardKeys) {
                    additionalProps[key] = item.get(key)
                }
            }

            val metadata = DialogueMetadata(
                id = item.getString("id"),
                text = item.getString("text"),
                emotion = item.getString("emotion"),
                category = item.getString("category"),
                priority = item.getInt("priority"),
                relationshipLevel = if (item.has("relationshipLevel") && !item.isNull("relationshipLevel")) item.getInt("relationshipLevel") else null,
                minimumStreak = if (item.has("minimumStreak") && !item.isNull("minimumStreak")) item.getInt("minimumStreak") else null,
                maximumStreak = if (item.has("maximumStreak") && !item.isNull("maximumStreak")) item.getInt("maximumStreak") else null,
                minimumSessionProgress = if (item.has("minimumSessionProgress") && !item.isNull("minimumSessionProgress")) item.getDouble("minimumSessionProgress").toFloat() else null,
                maximumSessionProgress = if (item.has("maximumSessionProgress") && !item.isNull("maximumSessionProgress")) item.getDouble("maximumSessionProgress").toFloat() else null,
                thinkingState = if (item.has("thinkingState") && !item.isNull("thinkingState")) item.getString("thinkingState") else null,
                cooldown = if (item.has("cooldown") && !item.isNull("cooldown")) item.getLong("cooldown") else null,
                weight = item.optInt("weight", 100),
                language = item.optString("language", language),
                enabled = item.optBoolean("enabled", true),
                tags = tagsList,
                additionalProperties = additionalProps
            )
            dialoguesList.add(metadata)
        }
        
        return DialogueLibrary(version, language, dialoguesList)
    }

    fun loadFromStream(inputStream: InputStream): DialogueLibrary {
        val jsonStr = inputStream.bufferedReader().use { it.readText() }
        return loadFromString(jsonStr)
    }
}
