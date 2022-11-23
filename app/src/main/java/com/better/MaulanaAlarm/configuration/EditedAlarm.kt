package com.better.MaulanaAlarm.configuration

import com.better.MaulanaAlarm.model.AlarmValue
import com.better.MaulanaAlarm.presenter.RowHolder
import com.better.MaulanaAlarm.util.Optional

/** Created by Yuriy on 09.08.2017. */
data class EditedAlarm(
    val isNew: Boolean = false,
    val id: Int = -1,
    val value: Optional<AlarmValue> = Optional.absent(),
    val holder: Optional<RowHolder> = Optional.absent()
) {
  fun id() = id
  val isEdited: Boolean = value.isPresent()
}
