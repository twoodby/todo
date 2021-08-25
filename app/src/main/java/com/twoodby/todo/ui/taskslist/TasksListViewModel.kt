package com.twoodby.todo.ui.taskslist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.twoodby.todo.repository.room.task.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class TasksListViewModel
@ViewModelInject
constructor (
    private val taskDao: TaskDao
): ViewModel() {

    // Search Data
    val searchQuery = MutableStateFlow("")
    private val taskFlow = searchQuery.flatMapLatest {
        taskDao.getTasks(it)
    }
    val tasks = taskFlow.asLiveData()


}