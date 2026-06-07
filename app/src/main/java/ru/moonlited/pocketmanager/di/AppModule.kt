package ru.moonlited.pocketmanager.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.moonlited.pocketmanager.data.api.ApiClient
import ru.moonlited.pocketmanager.data.local.AppDatabase
import ru.moonlited.pocketmanager.data.repository.TaskRepository
import ru.moonlited.pocketmanager.utils.SessionManager
import ru.moonlited.pocketmanager.viewmodel.TaskViewModel

val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "pocket_manager_db"
        ).fallbackToDestructiveMigration()
        .build()
    }

    single { get<AppDatabase>().taskDao() }

    single { SessionManager(androidContext()) }

    single { ApiClient.create(sessionManager = get()) }

    single { TaskRepository(taskDao = get(), apiService = get(), sessionManager = get()) }

    viewModel { TaskViewModel(taskRepository = get(), sessionManager = get()) }
}