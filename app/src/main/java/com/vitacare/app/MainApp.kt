package com.vitacare.app

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.vitacare.app.data.AppDatabase
import com.vitacare.app.data.SeedData
import com.vitacare.app.notifications.TipScheduler
import com.vitacare.app.notifications.ensureChannel
import com.vitacare.app.ui.VitaCareRoot
import com.vitacare.app.ui.theme.VitaCareTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Repo {
    lateinit var db: AppDatabase
}

class VitaCareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Repo.db = AppDatabase.instance(this)
        ensureChannel(this)
        CoroutineScope(Dispatchers.IO).launch { SeedData.seedIfEmpty(Repo.db) }
        TipScheduler.schedule(this)
    }
}

class MainActivity : ComponentActivity() {
    private val perm = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            perm.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent { VitaCareTheme { VitaCareRoot() } }
    }
}
