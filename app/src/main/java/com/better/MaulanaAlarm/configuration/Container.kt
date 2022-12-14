package com.better.MaulanaAlarm.configuration

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.PowerManager
import android.os.Vibrator
import android.telephony.TelephonyManager
import com.better.MaulanaAlarm.alert.BackgroundNotifications
import com.better.MaulanaAlarm.background.AlertServicePusher
import com.better.MaulanaAlarm.background.KlaxonPlugin
import com.better.MaulanaAlarm.background.PlayerWrapper
import com.better.MaulanaAlarm.bugreports.BugReporter
import com.better.MaulanaAlarm.interfaces.IAlarmsManager
import com.better.MaulanaAlarm.logger.Logger
import com.better.MaulanaAlarm.logger.LoggerFactory
import com.better.MaulanaAlarm.logger.loggerModule
import com.better.MaulanaAlarm.model.AlarmCore
import com.better.MaulanaAlarm.model.AlarmCoreFactory
import com.better.MaulanaAlarm.model.AlarmSetter
import com.better.MaulanaAlarm.model.AlarmStateNotifier
import com.better.MaulanaAlarm.model.Alarms
import com.better.MaulanaAlarm.model.AlarmsScheduler
import com.better.MaulanaAlarm.model.Calendars
import com.better.MaulanaAlarm.model.ContainerFactory
import com.better.MaulanaAlarm.model.IAlarmsScheduler
import com.better.MaulanaAlarm.persistance.DatabaseQuery
import com.better.MaulanaAlarm.persistance.PersistingContainerFactory
import com.better.MaulanaAlarm.persistance.RetryingDatabaseQuery
import com.better.MaulanaAlarm.presenter.AlarmsListActivity
import com.better.MaulanaAlarm.presenter.DynamicThemeHandler
import com.better.MaulanaAlarm.presenter.ScheduledReceiver
import com.better.MaulanaAlarm.presenter.ToastPresenter
import com.better.MaulanaAlarm.stores.SharedRxDataStoreFactory
import com.better.MaulanaAlarm.util.Optional
import com.better.MaulanaAlarm.wakelock.WakeLockManager
import com.better.MaulanaAlarm.wakelock.Wakelocks
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList
import java.util.Calendar
import org.koin.core.Koin
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

fun Scope.logger(tag: String): Logger {
  return get<LoggerFactory>().createLogger(tag)
}

fun Koin.logger(tag: String): Logger {
  return get<LoggerFactory>().createLogger(tag)
}

fun startKoin(context: Context): Koin {
  // The following line triggers the initialization of ACRA

  val module = module {
    single<DynamicThemeHandler> { DynamicThemeHandler(get()) }
    single<BugReporter> { BugReporter(logger("BugReporter"), context) }
    factory<Context> { context }
    factory(named("dateFormatOverride")) { "none" }
    factory<Single<Boolean>>(named("dateFormat")) {
      Single.fromCallable {
        get<String>(named("dateFormatOverride")).let { if (it == "none") null else it.toBoolean() }
            ?: android.text.format.DateFormat.is24HourFormat(context)
      }
    }

    single<Prefs> {
      val factory = SharedRxDataStoreFactory.create(get(), logger("preferences"))
      Prefs.create(get(named("dateFormat")), factory)
    }

    single<Store> {
      Store(
          alarmsSubject = BehaviorSubject.createDefault(ArrayList()),
          next = BehaviorSubject.createDefault<Optional<Store.Next>>(Optional.absent()),
          sets = PublishSubject.create(),
          events = PublishSubject.create())
    }

    factory { get<Context>().getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    single<AlarmSetter> { AlarmSetter.AlarmSetterImpl(logger("AlarmSetter"), get(), get()) }
    factory { Calendars { Calendar.getInstance() } }
    single<AlarmsScheduler> {
      AlarmsScheduler(get(), logger("AlarmsScheduler"), get(), get(), get())
    }
    factory<IAlarmsScheduler> { get<AlarmsScheduler>() }
    single<AlarmCore.IStateNotifier> { AlarmStateNotifier(get()) }
    single<ContainerFactory> { PersistingContainerFactory(get(), get()) }
    factory { get<Context>().contentResolver }
    single<DatabaseQuery> { RetryingDatabaseQuery(get(), get(), logger("DatabaseQuery")) }
    single<AlarmCoreFactory> {
      AlarmCoreFactory(logger("AlarmCore"), get(), get(), get(), get(), get())
    }
    single<Alarms> { Alarms(get(), get(), get(), get(), logger("Alarms")) }
    factory<IAlarmsManager> { get<Alarms>() }
    single { ScheduledReceiver(get(), get(), get(), get()) }
    single { ToastPresenter(get(), get()) }
    single { AlertServicePusher(get(), get(), get(), logger("AlertServicePusher")) }
    single { BackgroundNotifications(get(), get(), get(), get(), get()) }
    factory<Wakelocks> { get<WakeLockManager>() }
    factory<Scheduler> { AndroidSchedulers.mainThread() }
    single<WakeLockManager> { WakeLockManager(logger("WakeLockManager"), get()) }
    factory { get<Context>().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    factory { get<Context>().getSystemService(Context.POWER_SERVICE) as PowerManager }
    factory { get<Context>().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager }
    factory { get<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    factory { get<Context>().getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    factory { get<Context>().resources }

    factory(named("volumePreferenceDemo")) {
      KlaxonPlugin(
          log = logger("VolumePreference"),
          playerFactory = { PlayerWrapper(get(), get(), logger("VolumePreference")) },
          prealarmVolume = get<Prefs>().preAlarmVolume.observe(),
          fadeInTimeInMillis = Observable.just(100),
          inCall = Observable.just(false),
          scheduler = get())
    }
  }

  return startKoin {
        modules(module)
        modules(AlarmsListActivity.uiStoreModule)
        modules(loggerModule())
      }
      .koin
}

fun overrideIs24hoursFormatOverride(is24hours: Boolean) {
  loadKoinModules(
      module =
          module(override = true) { factory(named("dateFormatOverride")) { is24hours.toString() } })
}
