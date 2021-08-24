package com.twoodby.todo.ui.taskslist

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.twoodby.todo.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksListFragment : Fragment(R.layout.fragment_tasks) {
    private val viewModel: TasksListViewModel by viewModels()
}
