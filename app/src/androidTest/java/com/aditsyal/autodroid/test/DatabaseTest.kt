package com.aditsyal.autodroid.test

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.aditsyal.autodroid.data.local.database.AutomationDatabase
import org.junit.After
import org.junit.Before

abstract class DatabaseTest {
    
    protected lateinit var database: AutomationDatabase
    
    @Before
    open fun setupDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AutomationDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }
    
    @After
    open fun closeDatabase() {
        database.close()
    }
}

