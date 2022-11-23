package com.better.MaulanaAlarm.util

class Preconditions {
  companion object {
    @JvmStatic
    fun checkArgument(value: Boolean, template: String, vararg args: Any) {
      if (!value) throw IllegalArgumentException(template.format(args))
    }
  }
}
