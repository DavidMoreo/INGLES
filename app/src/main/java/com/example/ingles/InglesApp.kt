package com.example.ingles

import android.app.Application

class InglesApp : Application() {
    companion object {
        lateinit var context: Application
            private set
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
} 