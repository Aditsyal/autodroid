package com.aditsyal.autodroid.data.models

import android.os.Parcelable
import com.aditsyal.autodroid.data.local.entities.TemplateEntity
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class TemplateDTO(
    val id: Long = 0,
    val name: String,
    val description: String,
    val category: String,
    val icon: String,
    val macro: MacroDTO,
    val isBuiltIn: Boolean = true,
    val popularityScore: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {

    fun toEntity(): TemplateEntity {
        val macroJson = Gson().toJson(macro)
        return TemplateEntity(
            id = id,
            name = name,
            description = description,
            category = category,
            macroJson = macroJson,
            icon = icon.ifEmpty { null },
            isBuiltIn = isBuiltIn,
            enabled = true,
            createdAt = createdAt,
            usageCount = popularityScore
        )
    }
}

fun TemplateEntity.toDTO(): TemplateDTO {
    val macro = try {
        Gson().fromJson(macroJson, MacroDTO::class.java)
    } catch (e: Exception) {
        MacroDTO(name = name, description = description)
    }

    return TemplateDTO(
        id = id,
        name = name,
        description = description,
        category = category,
        icon = icon ?: "",
        macro = macro,
        isBuiltIn = isBuiltIn,
        popularityScore = usageCount,
        createdAt = createdAt
    )
}

enum class TemplateCategory(val displayName: String, val icon: String) {
    SLEEP("Sleep", "🌙"),
    WORK("Work", "💼"),
    HOME("Home", "🏠"),
    BATTERY("Battery", "🔋"),
    MEDIA("Media", "🎵"),
    NOTIFICATIONS("Notifications", "🔔"),
    AUTOMATION("Automation", "⚙️"),
    PRODUCTIVITY("Productivity", "📝"),
    COMMUNICATION("Communication", "💬")
}