package com.better.MaulanaAlarm

import com.better.MaulanaAlarm.model.AlarmStore
import com.better.MaulanaAlarm.model.ContainerFactory
import com.better.MaulanaAlarm.persistance.DatabaseQuery
import com.better.MaulanaAlarm.stores.modify

internal class DatabaseQueryMock {
  companion object {
    @JvmStatic
    fun createStub(list: MutableList<AlarmStore>): DatabaseQuery {
      return object : DatabaseQuery {
        override suspend fun query(): List<AlarmStore> {
          return list
        }
      }
    }

    @JvmStatic
    fun createWithFactory(factory: ContainerFactory): DatabaseQuery {
      return object : DatabaseQuery {
        override suspend fun query(): List<AlarmStore> {
          val container =
              factory.create().apply {
                modify { withId(100500).withIsEnabled(true).withLabel("hello") }
              }
          return listOf(container)
        }
      }
    }
  }
}
