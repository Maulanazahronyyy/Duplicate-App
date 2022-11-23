package com.better.MaulanaAlarm.presenter

import com.better.MaulanaAlarm.configuration.EditedAlarm
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

/** Created by Yuriy on 11.08.2017. */
interface UiStore {
  fun editing(): BehaviorSubject<EditedAlarm>
  fun onBackPressed(): PublishSubject<String>
  fun createNewAlarm()

  /** createNewAlarm was called -> list updates should be ignored */
  fun transitioningToNewAlarmDetails(): Subject<Boolean>
  fun edit(id: Int, holder: RowHolder? = null)
  fun hideDetails(holder: RowHolder? = null)

  /** Open the drawer when the activity is created. True after theme changes. */
  var openDrawerOnCreate: Boolean
}
