package com.ivanz851.minesweeper.helpers

import android.app.Application
import com.vk.sdk.VKSdk


class VkSdkInitializer : Application() {
    override fun onCreate() {
        super.onCreate()

        VKSdk.initialize(applicationContext)
    }
}

