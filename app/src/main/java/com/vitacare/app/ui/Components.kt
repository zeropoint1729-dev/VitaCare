package com.vitacare.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vitacare.app.data.Article
import com.vitacare.app.ui.theme.Emerald
import com.vitacare.app.ui.theme.InkSoft
import com.vitacare.app.ui.theme.MintSoft

@Composable
fun ArticleCard(article: Article, onBookmark: () -> Unit, onOpen: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp), color = Color.White,
        shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth().clickable { onOpen() }
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = article.imageUrl, contentDescription = null,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp)).background(MintSoft),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(article.title, fontWeight = FontWeight.SemiBold, maxLines = 2,
                    style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text("${article.source} · ${article.category}", color = InkSoft,
                    style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onBookmark) {
                Icon(
                    if (article.bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (article.bookmarked) Emerald else InkSoft
                )
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = Color.White,
        shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = Emerald)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun BulletRow(text: String) {
    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(Emerald, CircleShape))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun StepRow(index: Int, text: String) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(26.dp).background(MintSoft, CircleShape), contentAlignment = Alignment.Center) {
            Text("$index", color = Emerald, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun EmojiBadge(emoji: String, size: Int = 52) {
    Box(Modifier.size(size.dp).clip(RoundedCornerShape(16.dp)).background(MintSoft),
        contentAlignment = Alignment.Center) {
        Text(emoji, fontSize = (size / 2).sp)
    }
}

fun openUrl(ctx: Context, url: String?) {
    url?.takeIf { it.startsWith("http") }?.let {
        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
    }
}
