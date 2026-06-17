// ui/navigation/Screen.kt
package ru.moonlited.pocketmanager.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object RegisterRoute

@Serializable
object TaskListRoute

@Serializable
object PomodoroRoute

@Serializable
object SanTestRoute

@Serializable
data class StatsRoute(val initialTest: String? = null)

@Serializable
object TestsRoute

@Serializable
object MaslachTestRoute

@Serializable
object MunsterbergTestRoute

@Serializable
object RoleSelectionRoute

@Serializable
object SettingsRoute

@Serializable
object ProfileRoute

@Serializable
object ManagerCompanyRoute

@Serializable
object WorkingDayTimerRoute

@Serializable
data class PositionScheduleRoute(val departmentId: Int, val positionId: Int)