package com.example.japanmap

import android.app.Application
import com.example.japanmap.di.AppContainer

class JapanMapApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
