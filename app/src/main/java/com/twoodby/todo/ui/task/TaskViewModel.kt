package com.twoodby.todo.ui.task

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.twoodby.todo.R
import com.twoodby.todo.repository.room.task.Task
import com.twoodby.todo.repository.room.task.TaskDao
import com.twoodby.todo.ui.taskslist.TasksListViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    @Assisted private val state: SavedStateHandle
) : ViewModel() {

    val task = state.get<Task>("task")
    var taskName = state.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            state.set("taskName", value)
        }

    var taskImportance = state.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            state.set("taskImportance", value)
        }

    private val addTaskEventChannel = Channel<AddTaskEvents>()
    val addTaskEvent = addTaskEventChannel.receiveAsFlow()

    fun onSavedClicked() {
        if (taskName.isBlank()) {
            showInvalidInputMessage(R.string.error_empty_name)
            return
        }

        if (task != null) {
            updateTask(task.copy(name = taskName, important = taskImportance))
        } else {
            createTask(Task(name = taskName, important = taskImportance))
        }

    }

    private fun showInvalidInputMessage(errorMsg: Int) = viewModelScope.launch {
        addTaskEventChannel.send(AddTaskEvents.ShowInvalidInputMessage(R.string.error_empty_name))
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskDao.update(task)
        addTaskEventChannel.send(AddTaskEvents.NavigateBackWithResult(R.string.message_task_updated))
    }

    private fun createTask(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
        addTaskEventChannel.send(AddTaskEvents.NavigateBackWithResult(R.string.message_task_inserted))
    }

    sealed class AddTaskEvents {
        data class ShowInvalidInputMessage(val message:Int) : AddTaskEvents()
        data class NavigateBackWithResult(val result: Int): AddTaskEvents()
    }
}