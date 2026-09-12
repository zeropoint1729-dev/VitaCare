package com.vitacare.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.vitacare.app.R
import com.vitacare.app.data.AppDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

const val TIP_CHANNEL = "daily_tips"

fun ensureChannel(ctx: Context) {
    val channel = NotificationChannel(
        TIP_CHANNEL, "Daily Health Tips", NotificationManager.IMPORTANCE_DEFAULT
    ).apply { description = "One friendly health tip every morning" }
    NotificationManagerCompat.from(ctx).createNotificationChannel(channel)
}

fun showTipNotification(ctx: Context, text: String) {
    if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) return
    val notification = NotificationCompat.Builder(ctx, TIP_CHANNEL)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("VitaCare · Daily Health Tip")
        .setContentText(text)
        .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(ctx).notify(System.currentTimeMillis().toInt(), notification)
}

class DailyTipWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val tips = AppDatabase.instance(applicationContext).tipDao().all()
        if (tips.isEmpty()) return Result.retry()
        val tip = tips[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % tips.size]
        showTipNotification(applicationContext, tip.text)
        return Result.success()
    }
}

object TipScheduler {
    fun schedule(ctx: Context, hour: Int = 8) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        val request = PeriodicWorkRequestBuilder<DailyTipWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(target.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            "daily_tip", ExistingPeriodicWorkPolicy.KEEP, request
        )
    }

    fun cancel(ctx: Context) = WorkManager.getInstance(ctx).cancelUniqueWork("daily_tip")
}
