package com.example.core.components

import android.content.Context
import com.example.R
import java.util.Locale

object AvatarManager {
    data class AvatarInfo(
        val id: String,          // filename without extension, e.g. "cyber_avatar_ultra_dream"
        val resId: Int,          // R.drawable.xxx
        val displayName: String  // e.g. "Dream Traveler"
    )

    private val predefinedNames = mapOf(
        "cyber_avatar_ultra_dream" to "Dream Traveler",
        "cyber_avatar_ultra_astro" to "Astro Kid",
        "cyber_avatar_ultra_cybergirl" to "Cyber Punk Girl",
        "cyber_avatar_ultra_robot" to "Urban Robot",
        "cyber_avatar_ultra_hacker" to "Tech Hacker",
        "cyber_avatar_ultra_steampunk" to "Steam Inventor",
        "cyber_avatar_ultra_gamer" to "Gamer Pro"
    )

    fun getAvailableAvatars(context: Context): List<AvatarInfo> {
        val avatars = mutableListOf<AvatarInfo>()
        try {
            val drawableClass = R.drawable::class.java
            val fields = drawableClass.fields
            for (field in fields) {
                val name = field.name
                if (name.startsWith("cyber_avatar_ultra_")) {
                    val resId = field.getInt(null)
                    val displayName = getDisplayNameForId(name)
                    avatars.add(AvatarInfo(id = name, resId = resId, displayName = displayName))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Fallback to ensure we never have an empty list
        if (avatars.isEmpty()) {
            predefinedNames.forEach { (id, name) ->
                val resId = context.resources.getIdentifier(id, "drawable", context.packageName)
                if (resId != 0) {
                    avatars.add(AvatarInfo(id = id, resId = resId, displayName = name))
                }
            }
        }
        
        // Sort alphabetically to keep consistent grid order
        return avatars.sortedBy { it.id }
    }

    fun getDisplayNameForId(id: String): String {
        predefinedNames[id]?.let { return it }
        
        // Auto-generate name from filename (e.g. future_space_ninja -> Future Space Ninja)
        var cleanName = id
        if (cleanName.startsWith("cyber_avatar_ultra_")) {
            cleanName = cleanName.substring("cyber_avatar_ultra_".length)
        } else if (cleanName.startsWith("avatar_")) {
            cleanName = cleanName.substring("avatar_".length)
        }
        
        return cleanName.split("_", "-")
            .filter { it.isNotEmpty() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
    }
    
    fun getAvatarResId(context: Context, id: String): Int {
        if (id.isEmpty()) return R.drawable.cyber_avatar_ultra_dream
        val resId = context.resources.getIdentifier(id, "drawable", context.packageName)
        return if (resId != 0) resId else R.drawable.cyber_avatar_ultra_dream
    }
}
