package com.vitacare.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitacare.app.Repo
import com.vitacare.app.data.Disease
import com.vitacare.app.ui.theme.Emerald
import com.vitacare.app.ui.theme.InkSoft
import com.vitacare.app.ui.theme.MintSoft

@Composable
fun DiseaseListScreen(vm: MainViewModel = viewModel(), onOpen: (String) -> Unit) {
    val diseases by vm.filteredDiseases.collectAsStateWithLifecycle()
    val q by vm.diseaseQuery.collectAsStateWithLifecycle()

    Column(Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        Text("Disease Library", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = q, onValueChange = vm::onDiseaseQuery, modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search diseases…") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            shape = RoundedCornerShape(16.dp), singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)) {
            items(diseases, key = { it.id }) { d ->
                Surface(shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth().clickable { onOpen(d.id) }) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        EmojiBadge(d.emoji)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(d.name, style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Text(d.summary, style = MaterialTheme.typography.bodySmall,
                                color = InkSoft, maxLines = 1)
                        }
                        Icon(Icons.Rounded.ChevronRight, null, tint = InkSoft)
                    }
                }
            }
        }
    }
}

@Composable
fun DiseaseDetailScreen(id: String?, onBack: () -> Unit) {
    var disease by remember { mutableStateOf<Disease?>(null) }
    LaunchedEffect(id) { disease = id?.let { Repo.db.diseaseDao().byId(it) } }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(disease?.name ?: "Disease") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
    }) { pad ->
        val d = disease
        if (d == null) {
            Box(Modifier.fillMaxSize().padding(pad), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(shape = RoundedCornerShape(24.dp), color = MintSoft) {
                    Row(Modifier.padding(20.dp)) {
                        EmojiBadge(d.emoji, 64)
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(d.name, style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold)
                            Text(d.category, style = MaterialTheme.typography.bodySmall,
                                color = Emerald, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text(d.summary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                SectionCard("Symptoms") { d.symptoms.lines().forEach { BulletRow(it) } }
                SectionCard("Effects") { d.effects.lines().forEach { BulletRow(it) } }
                SectionCard("Medication Procedure") {
                    d.medication.lines().forEachIndexed { i, s -> StepRow(i + 1, s) }
                }
                Text(
                    "This information is educational and not a substitute for professional medical advice. Always consult a qualified doctor.",
                    style = MaterialTheme.typography.bodySmall, color = InkSoft
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
