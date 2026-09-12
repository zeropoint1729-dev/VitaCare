package com.vitacare.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitacare.app.ui.theme.Bg
import com.vitacare.app.ui.theme.Emerald
import com.vitacare.app.ui.theme.EmeraldDark
import com.vitacare.app.ui.theme.InkSoft
import com.vitacare.app.ui.theme.MintSoft
import java.util.Calendar

fun dayOfYear() = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

private fun greeting() = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good Morning"
    in 12..16 -> "Good Afternoon"
    in 17..21 -> "Good Evening"
    else -> "Good Night"
}

@Composable
fun HomeScreen(
    vm: MainViewModel = viewModel(),
    onOpenDisease: (String) -> Unit,
    onSeeAllNews: () -> Unit
) {
    val tips by vm.tips.collectAsStateWithLifecycle()
    val articles by vm.articles.collectAsStateWithLifecycle()
    val diseases by vm.diseases.collectAsStateWithLifecycle()
    val tip = tips.getOrNull(dayOfYear() % tips.size.coerceAtLeast(1))
    val ctx = LocalContext.current

    Column(Modifier.verticalScroll(rememberScrollState()).background(Bg)) {
        Box(Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Emerald, EmeraldDark)))
            .padding(24.dp)) {
            Column {
                Text("${greeting()},", color = Color.White,
                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("Here's your health overview for today.",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(16.dp))
        Surface(shape = RoundedCornerShape(24.dp), color = MintSoft,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.NotificationsActive, null, tint = Emerald,
                        modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Daily Health Tip", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(2.dp))
                    Text(tip?.text ?: "Stay hydrated - aim for 8 glasses of water today.",
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Shortcut("Heart", Icons.Rounded.Favorite, onSeeAllNews)
            Shortcut("Mind", Icons.Rounded.Psychology, onSeeAllNews)
            Shortcut("Fitness", Icons.Rounded.FitnessCenter, onSeeAllNews)
            Shortcut("Nutrition", Icons.Rounded.Restaurant, onSeeAllNews)
        }

        Spacer(Modifier.height(24.dp))
        HeaderRow("Common Conditions", "See all") {}
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            diseases.take(3).forEach { d ->
                Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth().clickable { onOpenDisease(d.id) }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        EmojiBadge(d.emoji)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(d.name, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Text(d.summary, style = MaterialTheme.typography.bodySmall,
                                color = InkSoft, maxLines = 1)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        HeaderRow("Latest News", "See all", onSeeAllNews)
        Spacer(Modifier.height(8.dp))
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            articles.take(3).forEach { a ->
                ArticleCard(a, onBookmark = { vm.toggleBookmark(a) }, onOpen = { openUrl(ctx, a.url) })
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderRow(title: String, action: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        TextButton(onClick = onClick) { Text(action, color = Emerald) }
    }
}

@Composable
private fun Shortcut(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(MintSoft),
            contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Emerald, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = InkSoft)
    }
}
