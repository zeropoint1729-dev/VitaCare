package com.vitacare.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

val CATEGORIES = listOf("All", "Nutrition", "Fitness", "Mental Health", "Heart Health", "World Health")

@Composable
fun NewsScreen(vm: MainViewModel = viewModel()) {
    val articles by vm.articles.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val cat by vm.category.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Health News & Magazines", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { vm.refreshNews() }) { Icon(Icons.Rounded.Refresh, "Refresh") }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query, onValueChange = vm::onQuery, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search articles…") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            shape = RoundedCornerShape(16.dp), singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(CATEGORIES) { c ->
                FilterChip(selected = cat == c, onClick = { vm.onCategory(c) }, label = { Text(c) })
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)) {
            items(articles, key = { it.id }) { a ->
                ArticleCard(a, onBookmark = { vm.toggleBookmark(a) }, onOpen = { openUrl(ctx, a.url) })
            }
        }
    }
}
