package com.vitacare.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitacare.app.Repo
import com.vitacare.app.data.Article
import com.vitacare.app.data.NewsRemote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val db = Repo.db

    val query = MutableStateFlow("")
    val category = MutableStateFlow("All")
    val diseaseQuery = MutableStateFlow("")

    val tips = db.tipDao().observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diseases = db.diseaseDao().observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val articles = combine(db.articleDao().observe(), query, category) { list, q, c ->
        list.filter {
            (c == "All" || it.category == c) && it.title.contains(q, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredDiseases = combine(db.diseaseDao().observe(), diseaseQuery) { list, q ->
        list.filter { it.name.contains(q, true) || it.category.contains(q, true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onQuery(v: String) { query.value = v }
    fun onCategory(c: String) { category.value = c }
    fun onDiseaseQuery(v: String) { diseaseQuery.value = v }

    fun toggleBookmark(a: Article) = viewModelScope.launch {
        db.articleDao().setBookmark(a.id, !a.bookmarked)
    }

    fun refreshNews() = viewModelScope.launch { NewsRemote.fetchInto(db.articleDao()) }
}
