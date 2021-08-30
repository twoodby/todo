package com.twoodby.todo.ui.task

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.twoodby.todo.R
import com.twoodby.todo.databinding.FragmentAddEditTaskBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    private val viewModel: TaskViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val binding = FragmentAddEditTaskBinding.bind(view)
        binding.apply {
            editTextTaskName.setText(viewModel.taskName)
            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            checkboxImportant.isChecked = viewModel.taskImportance
            checkboxImportant.jumpDrawablesToCurrentState()
            checkboxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            textViewDateCreated.isVisible = viewModel.task != null
            textViewDateCreated.text = getString(R.string.taskDate, viewModel.task?.createdDateFormatted)

            fapSaveTask.setOnClickListener {
                viewModel.onSavedClicked()
            }

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addTaskEvent.collect { event ->
                when (event) {
                    is TaskViewModel.AddTaskEvents.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus()
                        setFragmentResult(
                            requestKey = "addRequestFragment",
                            bundleOf("add_edit_results" to event.result
                        ))
                        findNavController().popBackStack()
                    }
                    is TaskViewModel.AddTaskEvents.ShowInvalidInputMessage -> showSnackBar(event.message)
                }
            }
        }
    }


    fun showSnackBar(msg: Int) {
        Snackbar.make(requireView(), msg, Snackbar.LENGTH_LONG).show()
    }

}