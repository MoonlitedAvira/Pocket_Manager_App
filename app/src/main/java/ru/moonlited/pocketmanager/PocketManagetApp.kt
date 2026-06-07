package ru.moonlited.pocketmanager

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.moonlited.pocketmanager.di.appModule

class PocketManagerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PocketManagerApp)
            modules(appModule)
        }
    }
}