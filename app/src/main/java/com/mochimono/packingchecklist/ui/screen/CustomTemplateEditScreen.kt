package com.mochimono.packingchecklist.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mochimono.packingchecklist.ui.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTemplateEditScreen(
    templateId: String,
    navController: NavController,
    viewModel: TemplateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val template = uiState.data?.customTemplates?.find { it.id == templateId }
    var newItem by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 ${template?.label ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                val items = template?.items ?: emptyList()
                itemsIndexed(items) { index, itemName ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(itemName, modifier = Modifier.weight(1f), fontSize = 14.sp)
                            TextButton(
                                onClick = {
                                    val updated = items.toMutableList().apply { removeAt(index) }
                                    viewModel.updateCustomTemplateItems(templateId, updated)
                                },
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text("✕", color = Color(0xFFCCCCCC), fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // 下部入力バー
            Surface(tonalElevation = 1.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newItem,
                        onValueChange = { newItem = it },
                        placeholder = { Text("項目を追加...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newItem.isNotBlank()) {
                                val updated = (template?.items ?: emptyList()) + newItem.trim()
                                viewModel.updateCustomTemplateItems(templateId, updated)
                                newItem = ""
                            }
                        })
                    )
                    Button(
                        onClick = {
                            if (newItem.isNotBlank()) {
                                val updated = (template?.items ?: emptyList()) + newItem.trim()
                                viewModel.updateCustomTemplateItems(templateId, updated)
                                newItem = ""
                            }
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("追加", fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
