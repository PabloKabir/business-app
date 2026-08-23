package com.example.businessapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.example.businessapp.data.AppDatabase
import com.example.businessapp.data.Business
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dao = AppDatabase.get(this).businessDao()
        setContent { BusinessBookScreen(dao) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessBookScreen(dao: com.example.businessapp.data.BusinessDao) {
    var query by remember { mutableStateOf("") }
    var dialogOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Business?>(null) }
    val businesses by (if (query.isBlank()) dao.observeAll() else dao.search(query)).collectAsState(initial = emptyList())

    Scaffold(
        topBar = { TopAppBar(title = { Text("دفتر کسب‌وکار") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editing = null; dialogOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = "افزودن")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("جستجو") },
                placeholder = { Text("نام، دسته‌بندی، تلفن، آدرس...") },
                singleLine = true
            )

            Text("${businesses.size} مورد", style = MaterialTheme.typography.labelLarge)

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(businesses, key = { it.id }) { item ->
                    BusinessCard(
                        item = item,
                        onEdit = { editing = item; dialogOpen = true },
                        onDelete = { CoroutineScope(Dispatchers.IO).launch { dao.delete(item) } }
                    )
                }
            }
        }
    }

    if (dialogOpen) {
        BusinessDialog(
            initial = editing,
            onDismiss = { dialogOpen = false },
            onSave = { saved ->
                CoroutineScope(Dispatchers.IO).launch {
                    if (saved.id == 0L) dao.insert(saved) else dao.update(saved)
                }
                dialogOpen = false
            }
        )
    }
}

@Composable
fun BusinessCard(item: Business, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                    if (item.category.isNotBlank()) Text(item.category)
                    if (item.phone.isNotBlank()) Text("تلفن: ${item.phone}")
                    if (item.address.isNotBlank()) Text("آدرس: ${item.address}")
                    if (item.notes.isNotBlank()) Text("یادداشت: ${item.notes}")
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "ویرایش") }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "حذف") }
                }
            }
        }
    }
}

@Composable
fun BusinessDialog(initial: Business?, onDismiss: () -> Unit, onSave: (Business) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "ثبت کسب‌وکار" else "ویرایش کسب‌وکار") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("نام کسب‌وکار") }, singleLine = true)
                OutlinedTextField(category, { category = it }, label = { Text("دسته‌بندی") }, singleLine = true)
                OutlinedTextField(phone, { phone = it }, label = { Text("تلفن") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), singleLine = true)
                OutlinedTextField(address, { address = it }, label = { Text("آدرس") })
                OutlinedTextField(notes, { notes = it }, label = { Text("یادداشت") })
            }
        },
        confirmButton = {
            Button(enabled = name.isNotBlank(), onClick = {
                onSave(Business(initial?.id ?: 0L, name.trim(), category.trim(), phone.trim(), address.trim(), notes.trim()))
            }) { Text("ذخیره") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("لغو") } }
    )
}
