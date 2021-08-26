package com.twoodby.todo.ui.taskslist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.twoodby.todo.repository.room.task.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest

class TasksListViewModel
@ViewModelInject
constructor (
    private val taskDao: TaskDao
): ViewModel() {

    val sortOrder = MutableStateFlow(SortOrder.BY_DATE)
    val hideCompleted = MutableStateFlow(false)
    // Search Data
    val searchQuery = MutableStateFlow("")

    private val taskFlow = combine(
        searchQuery,
        sortOrder,
        hideCompleted
    ){query, sortOrder, hideComplete   ->
        Triple(query, sortOrder, hideComplete)
    }.flatMapLatest { (query, sortOrder, hideComplete) ->
        taskDao.getTasks(query, sortOrder, hideComplete)
    }

    val tasks = taskFlow.asLiveData()


}

enum class SortOrder { BY_NAME, BY_DATE}