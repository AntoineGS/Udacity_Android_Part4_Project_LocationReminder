package com.udacity.project4.locationreminders

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

fun newReminderDTO(): ReminderDTO {
    return ReminderDTO(
        "Trash",
        "Take out the trash",
        "Home",
        10.0,
        5.0
    )
}