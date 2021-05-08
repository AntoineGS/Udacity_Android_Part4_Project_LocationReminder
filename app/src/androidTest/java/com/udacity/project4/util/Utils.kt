package com.udacity.project4.util

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import java.util.*

fun newReminderDTO(): ReminderDTO {
    val rand = Random()
    return ReminderDTO(
        "Trash" + rand.nextInt(10000).toString(),
        "Take out the trash" + rand.nextInt(10000).toString(),
        "Home" + rand.nextInt(10000).toString(),
        rand.nextInt(360).toDouble(),
        rand.nextInt(360).toDouble()
    )
}