package com.twoodby.todo.ui.taskslist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.twoodby.todo.repository.PreferenceManager
import com.twoodby.todo.repository.SortOrder
import com.twoodby.todo.repository.room.task.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class TasksListViewModel
@ViewModelInject
constructor (
    private val taskDao: TaskDao,
    private val preferenceManager: PreferenceManager
): ViewModel() {
    // Search Data
    val searchQuery = MutableStateFlow("")
    val preferenceFlow = preferenceManager.preferenceFlow

    private val taskFlow = combine(
        searchQuery,
        preferenceFlow
    ){query, filterPreferences   ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    fun  onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClicked(hideCompleted: Boolean) = viewModelScope.launch {
        preferenceManager.updateHideCompleted(hideCompleted)
    }

    val tasks = taskFlow.asLiveData()


}

