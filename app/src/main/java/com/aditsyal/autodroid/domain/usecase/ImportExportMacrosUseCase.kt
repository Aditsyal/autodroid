package com.aditsyal.autodroid.domain.usecase

import android.content.Context
import android.net.Uri
import com.aditsyal.autodroid.data.local.dao.MacroDao
import com.aditsyal.autodroid.data.local.dao.TemplateDao
import com.aditsyal.autodroid.data.local.dao.VariableDao
import com.aditsyal.autodroid.data.local.dao.TriggerDao
import com.aditsyal.autodroid.data.local.dao.ActionDao
import com.aditsyal.autodroid.data.local.dao.ConstraintDao
import com.aditsyal.autodroid.data.local.entities.*
import com.aditsyal.autodroid.data.local.entities.TemplateEntity
import com.aditsyal.autodroid.data.models.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportExportMacrosUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val macroDao: MacroDao,
    private val templateDao: TemplateDao,
    private val variableDao: VariableDao,
    private val triggerDao: TriggerDao,
    private val actionDao: ActionDao,
    private val constraintDao: ConstraintDao
) {

    companion object {
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
    }

    suspend fun exportAllMacros(): ExportResult {
        return withContext(Dispatchers.IO) {
            try {
                // Get all macros with details using synchronous DAO methods
                val macroWithDetailsList = macroDao.getAllMacrosSync().mapNotNull { entity ->
                    macroDao.getMacroWithDetailsByIdSync(entity.id)
                }
                val macros = macroWithDetailsList.map { it.toDTO() }

                // Get all global variables
                val variableEntities = variableDao.getAllGlobalVariablesSync()
                val variables = variableEntities.map { it.toDTO() }

                // Get all templates (if templateDao has sync method, otherwise leave empty)
                val templates = emptyList<TemplateDTO>()

                val exportData = ExportData(
                    version = "1.0",
                    exportDate = System.currentTimeMillis(),
                    macros = macros,
                    variables = variables,
                    templates = templates
                )

                val json = gson.toJson(exportData)
                val fileName = "autodroid_export_${System.currentTimeMillis()}.json"
                val uri = createExportFile(fileName, json)

                Timber.i("Exported ${macros.size} macros, ${variables.size} variables, ${templates.size} templates")
                ExportResult(
                    success = true,
                    uri = uri,
                    macroCount = macros.size,
                    variableCount = variables.size,
                    templateCount = templates.size
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to export macros")
                ExportResult(
                    success = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    suspend fun exportSingleMacro(macroId: Long): ExportResult {
        return withContext(Dispatchers.IO) {
            try {
                // Get macro with details using synchronous DAO method
                val macroWithDetails = macroDao.getMacroWithDetailsByIdSync(macroId)
                    ?: throw IllegalArgumentException("Macro with ID $macroId not found")

                val macroDTO = macroWithDetails.toDTO()

                // Get related global variables (if any variables are referenced)
                val variables = emptyList<VariableDTO>() // For single macro, we could filter variables by macroId
                val templates = emptyList<TemplateDTO>()

                val exportData = ExportData(
                    version = "1.0",
                    exportDate = System.currentTimeMillis(),
                    macros = listOf(macroDTO),
                    variables = variables,
                    templates = templates
                )

                val json = gson.toJson(exportData)
                val fileName = "autodroid_macro_${macroDTO.name}_${System.currentTimeMillis()}.json"
                val uri = createExportFile(fileName, json)

                Timber.i("Exported macro '${macroDTO.name}' with ${macroDTO.triggers.size} triggers, ${macroDTO.actions.size} actions")
                ExportResult(
                    success = true,
                    uri = uri,
                    macroCount = 1,
                    variableCount = variables.size,
                    templateCount = templates.size
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to export macro $macroId")
                ExportResult(
                    success = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    suspend fun importMacros(uri: Uri): ImportResult {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                        reader.readText()
                    }
                } ?: throw IllegalArgumentException("Failed to read file")

                val exportData = gson.fromJson(jsonString, ExportData::class.java)

                val importedMacros = mutableListOf<MacroDTO>()
                val importedVariables = mutableListOf<VariableDTO>()
                val importedTemplates = mutableListOf<TemplateDTO>()

                var conflictCount = 0

                for (macroJson in exportData.macros) {
                    // Check for name conflicts using synchronous DAO method
                    val existingMacro = macroDao.getMacroByNameSync(macroJson.name)
                    if (existingMacro != null) {
                        conflictCount++
                        continue
                    }

                    val macroId = macroDao.insertMacro(macroJson.toEntity())
                    importedMacros.add(macroJson.copy(id = macroId))

                    // Import triggers, actions, and constraints for this macro
                    for (trigger in macroJson.triggers) {
                        val triggerEntity = trigger.toEntity(macroId)
                        triggerDao.insertTrigger(triggerEntity)
                    }

                    for (action in macroJson.actions) {
                        val actionEntity = action.toEntity(macroId)
                        actionDao.insertAction(actionEntity)
                    }

                    for (constraint in macroJson.constraints) {
                        val constraintEntity = constraint.toEntity(macroId)
                        constraintDao.insertConstraint(constraintEntity)
                    }
                }

                for (variableJson in exportData.variables) {
                    if (variableJson.scope == "GLOBAL") {
                        // Check for name conflicts using synchronous DAO method
                        val existing = variableDao.getGlobalVariableByNameSync(variableJson.name)
                        if (existing != null) {
                            conflictCount++
                            continue
                        }

                        val varId = variableDao.insertVariable(variableJson.toEntity())
                        importedVariables.add(variableJson.copy(id = varId))
                    }
                }

                for (templateJson in exportData.templates) {
                    val templateEntity = templateJson.toEntity()
                    val templateId = templateDao.insertTemplate(templateEntity)
                    val macro = gson.fromJson(templateEntity.macroJson, MacroDTO::class.java)
                    importedTemplates.add(templateJson.copy(id = templateId, macro = macro.copy(id = templateId)))
                }

                Timber.i("Imported ${importedMacros.size} macros, ${importedVariables.size} variables, ${importedTemplates.size} templates")
                ImportResult(
                    success = true,
                    macroCount = importedMacros.size,
                    variableCount = importedVariables.size,
                    templateCount = importedTemplates.size,
                    conflictCount = conflictCount,
                    exportDate = exportData.exportDate
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to import macros")
                ImportResult(
                    success = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun createExportFile(fileName: String, content: String): Uri {
        val resolver = context.contentResolver
                val collection = android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/json")
                }

        val uri = resolver.insert(collection, contentValues) ?: throw IllegalArgumentException("Failed to create file")
        resolver.openOutputStream(uri)?.use { stream ->
            OutputStreamWriter(stream, StandardCharsets.UTF_8).use { writer ->
                writer.write(content)
            }
        } ?: throw IllegalArgumentException("Failed to create file")

        return uri
    }
}

data class ExportData(
    val version: String,
    val exportDate: Long,
    val macros: List<MacroDTO>,
    val variables: List<VariableDTO>,
    val templates: List<TemplateDTO>
)

data class ExportResult(
    val success: Boolean,
    val uri: Uri? = null,
    val macroCount: Int = 0,
    val variableCount: Int = 0,
    val templateCount: Int = 0,
    val error: String? = null
)

data class ImportResult(
    val success: Boolean,
    val macroCount: Int = 0,
    val variableCount: Int = 0,
    val templateCount: Int = 0,
    val conflictCount: Int = 0,
    val exportDate: Long? = null,
    val error: String? = null
)

// Extension functions for entity to DTO conversion
private fun MacroWithDetails.toDTO() = macro.toDTO(
    triggers = triggers.map { it.toDTO() },
    actions = actions.map { it.toDTO() },
    constraints = constraints.map { it.toDTO() }
)

private fun MacroEntity.toDTO(
    triggers: List<TriggerDTO>,
    actions: List<ActionDTO>,
    constraints: List<ConstraintDTO>
) = MacroDTO(
    id = id,
    name = name,
    description = description,
    enabled = enabled,
    createdAt = createdAt,
    lastExecuted = lastExecuted,
    triggers = triggers,
    actions = actions,
    constraints = constraints
)

private fun TriggerEntity.toDTO() = TriggerDTO(
    id = id,
    macroId = macroId,
    triggerType = triggerType,
    triggerConfig = parseJsonToMap(triggerConfig)
)

private fun ActionEntity.toDTO() = ActionDTO(
    id = id,
    actionType = actionType,
    actionConfig = parseJsonToMap(actionConfig),
    executionOrder = executionOrder,
    delayAfter = delayAfter
)

private fun ConstraintEntity.toDTO() = ConstraintDTO(
    id = id,
    constraintType = constraintType,
    constraintConfig = parseJsonToMap(constraintConfig)
)

private fun parseJsonToMap(json: String): Map<String, Any> {
    return try {
        val type = object : TypeToken<Map<String, Any>>() {}.type
        ImportExportMacrosUseCase.gson.fromJson(json, type)
    } catch (e: Exception) {
        Timber.w(e, "Failed to parse JSON config")
        emptyMap()
    }
}

private fun MacroDTO.toEntity(): com.aditsyal.autodroid.data.local.entities.MacroEntity {
    return com.aditsyal.autodroid.data.local.entities.MacroEntity(
        id = id,
        name = name,
        description = description,
        enabled = enabled,
        createdAt = createdAt,
        lastExecuted = lastExecuted
    )
}

private fun VariableDTO.toEntity(): com.aditsyal.autodroid.data.local.entities.VariableEntity {
    return com.aditsyal.autodroid.data.local.entities.VariableEntity(
        id = id,
        name = name,
        value = value,
        scope = scope,
        macroId = macroId,
        type = type
    )
}

private fun TriggerDTO.toEntity(macroId: Long) = TriggerEntity(
    id = id,
    macroId = macroId,
    triggerType = triggerType,
    triggerConfig = ImportExportMacrosUseCase.gson.toJson(triggerConfig),
    enabled = true,
    createdAt = System.currentTimeMillis()
)

private fun ActionDTO.toEntity(macroId: Long) = ActionEntity(
    id = id,
    macroId = macroId,
    actionType = actionType,
    actionConfig = ImportExportMacrosUseCase.gson.toJson(actionConfig),
    executionOrder = executionOrder,
    delayAfter = delayAfter,
    enabled = true,
    createdAt = System.currentTimeMillis()
)

private fun ConstraintDTO.toEntity(macroId: Long) = ConstraintEntity(
    id = id,
    macroId = macroId,
    constraintType = constraintType,
    constraintConfig = ImportExportMacrosUseCase.gson.toJson(constraintConfig),
    enabled = true,
    createdAt = System.currentTimeMillis()
)