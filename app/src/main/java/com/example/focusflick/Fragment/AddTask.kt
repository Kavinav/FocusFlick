package com.example.focusflick.Fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.focusflick.databinding.FragmentAddTaskBinding
import com.example.focusflick.model.TaskModel
import com.example.focusflick.utils.WorkManagerService
import com.example.focusflick.viewModel.TaskViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class AddTask : Fragment() {

    private lateinit var binding: FragmentAddTaskBinding
    private val taskViewModel: TaskViewModel by viewModels()
    private var taskToUpdate: TaskModel? = null
    private var selectedText: String = "High"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if we're editing an existing task
        taskToUpdate = arguments?.getSerializable("task") as? TaskModel

        // If task is present, pre-fill the fields for editing
        taskToUpdate?.let { task ->
            binding.tittleText.setText(task.tittle)
            binding.desText.setText(task.des)
            binding.dateText.setText(task.taskDate)
            binding.timeText.setText(task.time)
            // Set other fields like category and priority based on your UI
        }

        // Handle permission
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1)
        }

        // Initialize DatePicker
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select task reminder date")
            .build()

        binding.dateText.setOnClickListener {
            datePicker.show(childFragmentManager, "DATE_PICKER")
        }

        datePicker.addOnPositiveButtonClickListener { selection ->
            val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
            val formattedDate = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(selection),
                ZoneId.systemDefault()
            ).format(dateFormatter)
            binding.dateText.setText(formattedDate)
        }

        // Initialize TimePicker
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setTitleText("Select time")
            .build()

        binding.timeText.setOnClickListener {
            timePicker.show(childFragmentManager, "TIME_PICKER")
        }

        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour
            val minute = timePicker.minute
            val formattedTime = String.format(
                "%02d:%02d %s", if (hour == 0 || hour == 12) 12 else hour % 12, minute,
                if (hour < 12) "AM" else "PM"
            )
            binding.timeText.setText(formattedTime)
        }

        // ChipGroup for task priority
        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            for (id in checkedIds) {
                val chip = group.findViewById<Chip>(id)
                if (chip.isChecked) {
                    selectedText = chip.text.toString()
                }
            }
        }

        // Save or Update button logic
        binding.saveButton.setOnClickListener {
            val title = binding.tittleText.text.toString()
            val description = binding.desText.text.toString()
            val date = binding.dateText.text.toString()
            val time = binding.timeText.text.toString()
            val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())

            if (title.isBlank()) {
                binding.tittleText.error = "Please provide a title"
            } else {
                val task = taskToUpdate?.copy(
                    tittle = title,
                    des = description,
                    taskDate = date,
                    time = time
                ) ?: TaskModel(
                    tittle = title,
                    des = description,
                    taskDate = date,
                    time = time,
                    currentDate = currentDate,
                    category = selectedText
                )

                if (taskToUpdate != null) {
                    // Update the existing task
                    taskViewModel.updateTask(task)
                    Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    // Insert new task
                    taskViewModel.insertTask(task)
                    Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show()
                }

                findNavController().popBackStack()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted
        } else {
            // Permission denied
        }
    }
}
