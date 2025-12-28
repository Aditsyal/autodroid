package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.local.dao.TemplateDao
import com.aditsyal.autodroid.data.local.entities.TemplateEntity
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.automation.trigger.TriggerManager
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject

/**
 * Create a macro from a template
 */
class CreateMacroFromTemplateUseCase @Inject constructor(
    private val templateDao: TemplateDao,
    private val createMacroUseCase: CreateMacroUseCase
) {
    private val gson = Gson()

    suspend operator fun invoke(templateId: Long): Long? {
        return try {
            val template = templateDao.getTemplateById(templateId)
            if (template == null) {
                Timber.w("Template not found: $templateId")
                return null
            }

            // Parse macro from JSON
            val macro = gson.fromJson(template.macroJson, MacroDTO::class.java)
            
            // Create new macro with updated name (add timestamp to make it unique)
            val newMacro = macro.copy(
                id = 0, // New macro
                name = "${macro.name} (${System.currentTimeMillis()})",
                createdAt = System.currentTimeMillis()
            )

            // Increment template usage count
            templateDao.incrementUsageCount(templateId)

            // Create the macro
            createMacroUseCase(newMacro)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create macro from template: $templateId")
            null
        }
    }
}

