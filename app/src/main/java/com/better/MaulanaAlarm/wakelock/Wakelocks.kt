package com.better.MaulanaAlarm.wakelock

interface Wakelocks {
  fun acquireServiceLock()

  fun releaseServiceLock()
}
