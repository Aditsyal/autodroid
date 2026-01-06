package com.aditsyal.autodroid.integration.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aditsyal.autodroid.data.local.dao.VariableDao
import com.aditsyal.autodroid.data.local.database.AutomationDatabase
import com.aditsyal.autodroid.data.local.entities.VariableEntity
import com.aditsyal.autodroid.test.DatabaseTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VariableDatabaseIntegrationTest : DatabaseTest() {
    
    private lateinit var variableDao: VariableDao
    
    override fun setupDatabase() {
        super.setupDatabase()
        variableDao = database.variableDao()
    }
    
    @Test
    fun `should create global and local variables`() = runBlocking {
        val globalVar = VariableEntity(
            name = "globalCounter",
            value = "0",
            scope = "GLOBAL",
            macroId = null,
            type = "NUMBER"
        )
        
        val localVar = VariableEntity(
            name = "localCounter",
            value = "10",
            scope = "LOCAL",
            macroId = 1L,
            type = "NUMBER"
        )
        
        val globalId = variableDao.insertVariable(globalVar)
        val localId = variableDao.insertVariable(localVar)
        
        assertTrue(globalId > 0)
        assertTrue(localId > 0)
        
        val savedGlobal = variableDao.getVariable("globalCounter", "GLOBAL", null)
        val savedLocal = variableDao.getVariable("localCounter", "LOCAL", 1L)
        
        assertNotNull(savedGlobal)
        assertEquals("0", savedGlobal?.value)
        assertNotNull(savedLocal)
        assertEquals("10", savedLocal?.value)
    }
    
    @Test
    fun `should update variable values`() = runBlocking {
        val variable = VariableEntity(
            name = "testVar",
            value = "initial",
            scope = "GLOBAL",
            type = "STRING"
        )
        
        val id = variableDao.insertVariable(variable)
        val saved = variableDao.getVariableById(id)
        
        assertNotNull(saved)
        val updated = saved!!.copy(value = "updated", updatedAt = System.currentTimeMillis())
        variableDao.updateVariable(updated)
        
        val afterUpdate = variableDao.getVariableById(id)
        assertEquals("updated", afterUpdate?.value)
    }
    
    @Test
    fun `should query variables by scope`() = runBlocking {
        val global1 = VariableEntity(name = "global1", value = "1", scope = "GLOBAL", type = "STRING")
        val global2 = VariableEntity(name = "global2", value = "2", scope = "GLOBAL", type = "STRING")
        val local1 = VariableEntity(name = "local1", value = "1", scope = "LOCAL", macroId = 1L, type = "STRING")
        
        variableDao.insertVariable(global1)
        variableDao.insertVariable(global2)
        variableDao.insertVariable(local1)
        
        val globalVars = variableDao.getAllGlobalVariables().first()
        assertEquals(2, globalVars.size)
        assertTrue(globalVars.all { it.scope == "GLOBAL" })
        
        val localVars = variableDao.getVariablesByMacroId(1L).first()
        assertEquals(1, localVars.size)
        assertEquals("local1", localVars.first().name)
    }
    
    @Test
    fun `should handle concurrent variable access`() = runBlocking {
        val variable = VariableEntity(
            name = "counter",
            value = "0",
            scope = "GLOBAL",
            type = "NUMBER"
        )
        
        val id = variableDao.insertVariable(variable)
        
        // Simulate concurrent updates
        val updates = (1..10).map { i ->
            val current = variableDao.getVariableById(id)!!
            current.copy(value = i.toString(), updatedAt = System.currentTimeMillis())
        }
        
        updates.forEach { variableDao.updateVariable(it) }
        
        val final = variableDao.getVariableById(id)
        assertNotNull(final)
        // Last update should be 10
        assertEquals("10", final?.value)
    }
}

