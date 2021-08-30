package com.twoodby.todo.ui.taskslist

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.twoodby.todo.R
import com.twoodby.todo.databinding.FragmentTasksBinding
import com.twoodby.todo.repository.SortOrder
import com.twoodby.todo.repository.room.task.Task
import com.twoodby.todo.ui.MainActivity
import com.twoodby.todo.util.extends.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksListFragment : Fragment(R.layout.fragment_tasks), TasksListAdapter.OnItemClickListener {
    private val viewModel: TasksListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view)

        val taskAdapter = TasksListAdapter(this)

        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = taskAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                }
            }).attachToRecyclerView(recyclerViewTasks)

            fapAddTask.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }

        }

        setFragmentResultListener("addRequestFragment") { _, bundle ->
            var result = bundle.getInt("add_edit_results")
            viewModel.onAddEditResult(result)
        }

        viewModel.tasks.observe(viewLifecycleOwner ) {
            taskAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is TasksListViewModel.TasksEvent.ShowUndoDeleteTask -> {
                        Snackbar.make(requireView(), R.string.task_deleted_snack_bar, Snackbar.LENGTH_LONG)
                            .setAction(R.string.task_deleted_action_text) {
                                viewModel.onUndoDeleteClicked(event.task)
                            }.show()
                    }

                    is TasksListViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action = TasksListFragmentDirections.actionTasksListFragmentToTaskFragment(title = getString(R.string.TitleAddTask))
                        findNavController().navigate(action)
                    }

                    is TasksListViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        val action = TasksListFragmentDirections.actionTasksListFragmentToTaskFragment(task = event.task, title = getString(R.string.TitleNewTask))
                        findNavController().navigate(action)
                    }
                    is TasksListViewModel.TasksEvent.ShowSnackBar ->
                        Snackbar.make(requireView(), getString(event.msgId), Snackbar.LENGTH_LONG).show()
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).supportActionBar?.title = getString(R.string.TitleTaskList)
    }

    override fun onItemClick(task: Task) {
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClicked(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tasks).isChecked =
                viewModel.preferenceFlow.first().hideCompleted

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when(item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }

            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }

            R.id.action_hide_completed_tasks -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClicked(item.isChecked)
                true
            }

            R.id.action_delete_completed_tasks -> {
                true
            }

            else -> super.onOptionsItemSelected(item)
        }



    }
}
