package com.aditsyal.autodroid.integration.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aditsyal.autodroid.data.local.dao.TemplateDao
import com.aditsyal.autodroid.data.local.database.AutomationDatabase
import com.aditsyal.autodroid.data.local.entities.TemplateEntity
import com.aditsyal.autodroid.test.DatabaseTest
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TemplateDatabaseIntegrationTest : DatabaseTest() {
    
    private lateinit var templateDao: TemplateDao
    private val gson = Gson()
    
    override fun setupDatabase() {
        super.setupDatabase()
        templateDao = database.templateDao()
    }
    
    @Test
    fun `should create template from macro`() = runBlocking {
        val macroJson = gson.toJson(mapOf(
            "name" to "Test Macro",
            "triggers" to emptyList<Any>(),
            "actions" to emptyList<Any>()
        ))
        
        val template = TemplateEntity(
            name = "Test Template",
            description = "Test description",
            category = "TEST",
            macroJson = macroJson,
            isBuiltIn = false,
            enabled = true
        )
        
        val templateId = templateDao.insertTemplate(template)
        
        assertTrue(templateId > 0)
        
        val saved = templateDao.getTemplateById(templateId)
        assertNotNull(saved)
        assertEquals("Test Template", saved?.name)
        assertEquals("TEST", saved?.category)
    }
    
    @Test
    fun `should load template and parse macro JSON`() = runBlocking {
        val macroData = mapOf(
            "name" to "Morning Routine",
            "triggers" to listOf(mapOf("type" to "TIME", "time" to "07:00")),
            "actions" to listOf(mapOf("type" to "SHOW_TOAST", "message" to "Good morning"))
        )
        val macroJson = gson.toJson(macroData)
        
        val template = TemplateEntity(
            name = "Morning Routine Template",
            description = "Wake up automation",
            category = "ROUTINE",
            macroJson = macroJson
        )
        
        val templateId = templateDao.insertTemplate(template)
        val loaded = templateDao.getTemplateById(templateId)
        
        assertNotNull(loaded)
        val parsed = gson.fromJson(loaded!!.macroJson, Map::class.java)
        assertNotNull(parsed)
        assertEquals("Morning Routine", (parsed as Map<*, *>)["name"])
    }
    
    @Test
    fun `should increment template usage count`() = runBlocking {
        val template = TemplateEntity(
            name = "Popular Template",
            description = "Test",
            category = "TEST",
            macroJson = "{}",
            usageCount = 0
        )
        
        val templateId = templateDao.insertTemplate(template)
        
        // Increment multiple times
        templateDao.incrementUsageCount(templateId)
        templateDao.incrementUsageCount(templateId)
        templateDao.incrementUsageCount(templateId)
        
        val updated = templateDao.getTemplateById(templateId)
        assertEquals(3, updated?.usageCount)
    }
    
    @Test
    fun `should query templates by category`() = runBlocking {
        val template1 = TemplateEntity(
            name = "Template 1",
            description = "Test",
            category = "ROUTINE",
            macroJson = "{}"
        )
        val template2 = TemplateEntity(
            name = "Template 2",
            description = "Test",
            category = "ROUTINE",
            macroJson = "{}"
        )
        val template3 = TemplateEntity(
            name = "Template 3",
            description = "Test",
            category = "PRODUCTIVITY",
            macroJson = "{}"
        )
        
        templateDao.insertTemplate(template1)
        templateDao.insertTemplate(template2)
        templateDao.insertTemplate(template3)
        
        val routineTemplates = templateDao.getTemplatesByCategory("ROUTINE").first()
        assertEquals(2, routineTemplates.size)
        assertTrue(routineTemplates.all { it.category == "ROUTINE" })
        
        val productivityTemplates = templateDao.getTemplatesByCategory("PRODUCTIVITY").first()
        assertEquals(1, productivityTemplates.size)
    }
}


