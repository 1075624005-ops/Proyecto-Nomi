package com.example.nomi

import android.app.Application
import com.example.nomi.data.LocalDemoStorage

class NomiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LocalDemoStorage.init(this)
    }
}
