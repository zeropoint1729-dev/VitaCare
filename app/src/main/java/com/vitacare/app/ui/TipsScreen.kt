package com.vitacare.app.ui

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitacare.app.notifications.TipScheduler
import com.vitacare.app.notifications.showTipNotification
import com.vitacare.app.ui.theme.Emerald
import com.vitacare.app.ui.theme.InkSoft

@Composable
fun TipsScreen(vm: MainViewModel = viewModel()) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("vitacare", Context.MODE_PRIVATE) }
    var enabled by remember { mutableStateOf(prefs.getBoolean("daily_enabled", true)) }
    val tips by vm.tips.collectAsStateWithLifecycle()
    val today = tips.getOrNull(dayOfYear() % tips.size.coerceAtLeast(1))

    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Daily Tips", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 1.dp) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.NotificationsActive, null, tint = Emerald)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Morning tip notification", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold)
                    Text("Delivered daily at 8:00 AM", style = MaterialTheme.typography.bodySmall,
                        color = InkSoft)
                }
                Switch(checked = enabled, onCheckedChange = {
                    enabled = it
                    prefs.edit().putBoolean("daily_enabled", it).apply()
                    if (it) TipScheduler.schedule(ctx) else TipScheduler.cancel(ctx)
                })
            }
        }

        Button(
            onClick = { today?.let { showTipNotification(ctx, it.text) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald)
        ) { Text("Preview today's notification") }

        Text("All Tips", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        tips.forEach { t ->
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 1.dp) {
                Column(Modifier.padding(16.dp)) {
                    Text(t.category, style = MaterialTheme.typography.labelSmall,
                        color = Emerald, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(t.text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
