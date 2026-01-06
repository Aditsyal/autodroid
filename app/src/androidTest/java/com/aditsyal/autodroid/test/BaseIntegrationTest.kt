package com.aditsyal.autodroid.test

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aditsyal.autodroid.data.local.database.AutomationDatabase
import com.google.gson.Gson
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
abstract class BaseIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    protected lateinit var database: AutomationDatabase
    protected lateinit var testDispatcher: TestDispatcher
    protected val gson = Gson()
    
    @Before
    open fun setup() {
        hiltRule.inject()
        
        // Set up test dispatcher
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        // Create in-memory database
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AutomationDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }
    
    @After
    open fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }
}

