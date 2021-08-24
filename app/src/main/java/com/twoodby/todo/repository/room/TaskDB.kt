package com.twoodby.todo.repository.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.twoodby.todo.di.ApplicationScope
import com.twoodby.todo.repository.room.task.Task
import com.twoodby.todo.repository.room.task.TaskDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TaskDB: RoomDatabase() {

    abstract fun tasks(): TaskDao


    class Callback
        @Inject
        constructor(
            private val database: Provider<TaskDB>,
            @ApplicationScope private val appScope: CoroutineScope
        ): RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().tasks()

            appScope.launch {
                dao.insert(Task(name = "Wash the dishes"))
                dao.insert(Task(name = "Do the laundry", important = true))
                dao.insert(Task(name = "Buy Groceries"))
                dao.insert(Task(name = "Prepare Food"))
                dao.insert(Task(name = "Call Mom"))
                dao.insert(Task(name = "Visit Grandma", completed = true))
            }

        }

    }

}