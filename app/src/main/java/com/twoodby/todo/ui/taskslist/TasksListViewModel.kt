package com.twoodby.todo.ui.taskslist

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.twoodby.todo.repository.room.task.TaskDao
import javax.inject.Inject

class TasksListViewModel
@ViewModelInject
constructor (
    private val taskDao: TaskDao
): ViewModel() {

    val tasks = taskDao.getTasks().asLiveData()

}