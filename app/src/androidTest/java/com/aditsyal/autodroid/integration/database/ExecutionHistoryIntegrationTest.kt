package com.aditsyal.autodroid.integration.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aditsyal.autodroid.data.local.dao.ExecutionLogDao
import com.aditsyal.autodroid.data.local.dao.MacroDao
import com.aditsyal.autodroid.data.local.database.AutomationDatabase
import com.aditsyal.autodroid.data.local.entities.ExecutionLogEntity
import com.aditsyal.autodroid.data.local.entities.MacroEntity
import com.aditsyal.autodroid.test.DatabaseTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExecutionHistoryIntegrationTest : DatabaseTest() {
    
    private lateinit var executionLogDao: ExecutionLogDao
    private lateinit var macroDao: MacroDao
    
    override fun setupDatabase() {
        super.setupDatabase()
        executionLogDao = database.executionLogDao()
        macroDao = database.macroDao()
    }
    
    @Test
    fun `should log execution with all fields`() = runBlocking {
        val macro = MacroEntity(name = "Test Macro", description = "Test")
        val macroId = macroDao.insertMacro(macro)
        
        val log = ExecutionLogEntity(
            macroId = macroId,
            executedAt = System.currentTimeMillis(),
            executionStatus = "SUCCESS",
            errorMessage = null,
            executionDurationMs = 150L,
            actionsExecuted = 2
        )
        
        val logId = executionLogDao.insertExecutionLog(log)
        
        assertTrue(logId > 0)
        
        val allLogs = executionLogDao.getAllExecutionLogs(limit = 10).first()
        assertTrue(allLogs.isNotEmpty())
        val saved = allLogs.first()
        assertEquals("SUCCESS", saved.executionStatus)
        assertEquals(150L, saved.executionDurationMs)
    }
    
    @Test
    fun `should query execution history by macro`() = runBlocking {
        val macro1 = MacroEntity(name = "Macro 1")
        val macro2 = MacroEntity(name = "Macro 2")
        val macroId1 = macroDao.insertMacro(macro1)
        val macroId2 = macroDao.insertMacro(macro2)
        
        val log1 = ExecutionLogEntity(
            macroId = macroId1,
            executedAt = System.currentTimeMillis(),
            executionStatus = "SUCCESS"
        )
        val log2 = ExecutionLogEntity(
            macroId = macroId1,
            executedAt = System.currentTimeMillis() + 1000,
            executionStatus = "SUCCESS"
        )
        val log3 = ExecutionLogEntity(
            macroId = macroId2,
            executedAt = System.currentTimeMillis() + 2000,
            executionStatus = "SUCCESS"
        )
        
        executionLogDao.insertExecutionLog(log1)
        executionLogDao.insertExecutionLog(log2)
        executionLogDao.insertExecutionLog(log3)
        
        val macro1Logs = executionLogDao.getExecutionLogsByMacroId(macroId1, limit = 10).first()
        assertEquals(2, macro1Logs.size)
        assertTrue(macro1Logs.all { it.macroId == macroId1 })
    }
    
    @Test
    fun `should filter by execution status`() = runBlocking {
        val macro = MacroEntity(name = "Test Macro")
        val macroId = macroDao.insertMacro(macro)
        
        val successLog = ExecutionLogEntity(
            macroId = macroId,
            executedAt = System.currentTimeMillis(),
            executionStatus = "SUCCESS"
        )
        val failedLog = ExecutionLogEntity(
            macroId = macroId,
            executedAt = System.currentTimeMillis() + 1000,
            executionStatus = "FAILED",
            errorMessage = "Test error"
        )
        val skippedLog = ExecutionLogEntity(
            macroId = macroId,
            executedAt = System.currentTimeMillis() + 2000,
            executionStatus = "SKIPPED"
        )
        
        executionLogDao.insertExecutionLog(successLog)
        executionLogDao.insertExecutionLog(failedLog)
        executionLogDao.insertExecutionLog(skippedLog)
        
        val allLogs = executionLogDao.getAllExecutionLogs(limit = 10).first()
        assertEquals(3, allLogs.size)
        
        val successLogs = allLogs.filter { it.executionStatus == "SUCCESS" }
        assertEquals(1, successLogs.size)
        
        val failedLogs = allLogs.filter { it.executionStatus == "FAILED" }
        assertEquals(1, failedLogs.size)
        assertNotNull(failedLogs.first().errorMessage)
    }
    
    @Test
    fun `should support pagination of execution logs`() = runBlocking {
        val macro = MacroEntity(name = "Test Macro")
        val macroId = macroDao.insertMacro(macro)
        
        // Insert 15 logs
        repeat(15) { i ->
            val log = ExecutionLogEntity(
                macroId = macroId,
                executedAt = System.currentTimeMillis() + i * 1000L,
                executionStatus = "SUCCESS"
            )
            executionLogDao.insertExecutionLog(log)
        }
        
        // Get first page (limit 10)
        val firstPage = executionLogDao.getAllExecutionLogs(limit = 10).first()
        assertEquals(10, firstPage.size)
        
        // Get all logs
        val allLogs = executionLogDao.getAllExecutionLogs(limit = 100).first()
        assertEquals(15, allLogs.size)
    }
}

