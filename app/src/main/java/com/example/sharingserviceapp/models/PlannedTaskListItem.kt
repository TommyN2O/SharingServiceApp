package com.example.sharingserviceapp.models

sealed class PlannedTaskListItem {
    data class DateHeader(val date: String) : PlannedTaskListItem()
    data class TaskItem(val task: TaskResponse) : PlannedTaskListItem()
}
