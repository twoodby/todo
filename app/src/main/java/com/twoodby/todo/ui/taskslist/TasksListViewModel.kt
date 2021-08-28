package com.twoodby.todo.ui.taskslist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.twoodby.todo.repository.PreferenceManager
import com.twoodby.todo.repository.SortOrder
import com.twoodby.todo.repository.room.task.Task
import com.twoodby.todo.repository.room.task.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val taskFlow = combine(
        searchQuery,
        preferenceFlow
    ){query, filterPreferences   ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {

    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = isChecked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTask(task))
    }

    fun  onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClicked(hideCompleted: Boolean) = viewModelScope.launch {
        preferenceManager.updateHideCompleted(hideCompleted)
    }

    fun onUndoDeleteClicked(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    val tasks = taskFlow.asLiveData()


    sealed class TasksEvent {
        data class ShowUndoDeleteTask(val task: Task): TasksEvent()
    }
}

