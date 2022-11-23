package com.better.MaulanaAlarm.model

import com.better.MaulanaAlarm.configuration.Prefs
import com.better.MaulanaAlarm.configuration.Store
import com.better.MaulanaAlarm.logger.Logger

/** Created by Yuriy on 09.08.2017. */
class AlarmCoreFactory(
    private val logger: Logger,
    private val alarmsScheduler: IAlarmsScheduler,
    private val broadcaster: AlarmCore.IStateNotifier,
    private val prefs: Prefs,
    private val store: Store,
    private val calendars: Calendars
) {
  fun create(container: AlarmStore): AlarmCore {
    return AlarmCore(container, logger, alarmsScheduler, broadcaster, prefs, store, calendars)
  }
}
