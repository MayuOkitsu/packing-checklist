package com.mochimono.packingchecklist.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mochimono.packingchecklist.ui.component.BottomNavBar
import com.mochimono.packingchecklist.ui.component.BottomNavTab
import com.mochimono.packingchecklist.ui.navigation.Screen
import com.mochimono.packingchecklist.ui.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateManagementScreen(
    navController: NavController,
    viewModel: TemplateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState.data
    var newDefaultItem by remember { mutableStateOf("") }
    var renameDialogTarget by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }

    // 名前変更ダイアログ
    renameDialogTarget?.let { templateId ->
        AlertDialog(
            onDismissRequest = { renameDialogTarget = null },
            title = { Text("テンプレート名を変更") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateCustomTemplateLabel(templateId, renameText)
                    renameDialogTarget = null
                }) { Text("変更") }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogTarget = null }) { Text("キャンセル") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("テンプレート管理") },
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
        },
        bottomBar = {
            BottomNavBar(currentTab = BottomNavTab.Template, navController = navController)
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                // セクションヘッダー: デフォルトテンプレート
                item {
                    Text(
                        "デフォルトテンプレート",
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }

                // デフォルトテンプレート項目
                items(state?.defaultItems ?: emptyList()) { item ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.name, modifier = Modifier.weight(1f), fontSize = 14.sp)
                            TextButton(
                                onClick = { viewModel.removeDefaultItem(item.id) },
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text("✕", color = Color(0xFFCCCCCC), fontSize = 16.sp)
                            }
                        }
                    }
                }

                // デフォルト項目追加バー
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newDefaultItem,
                            onValueChange = { newDefaultItem = it },
                            placeholder = { Text("デフォルト項目を追加...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.addDefaultItem(newDefaultItem)
                                newDefaultItem = ""
                            })
                        )
                        Button(
                            onClick = {
                                viewModel.addDefaultItem(newDefaultItem)
                                newDefaultItem = ""
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("追加", fontWeight = FontWeight.SemiBold) }
                    }
                }

                // セクションヘッダー: カスタムテンプレート
                item {
                    Text(
                        "カスタムテンプレート",
                        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                }

                // カスタムテンプレートカード
                if (state?.customTemplates.isNullOrEmpty()) {
                    item {
                        Text(
                            "カスタムテンプレートがありません",
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    items(state?.customTemplates ?: emptyList()) { template ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("📋 ${template.label}", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text(
                                    template.items.joinToString(", "),
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextButton(onClick = {
                                        renameText = template.label
                                        renameDialogTarget = template.id
                                    }) { Text("名前変更") }
                                    TextButton(onClick = {
                                        navController.navigate(Screen.CustomTemplateEdit.createRoute(template.id))
                                    }) { Text("項目編集") }
                                    TextButton(onClick = {
                                        viewModel.deleteCustomTemplate(template.id)
                                    }) { Text("削除", color = Color(0xFFEF4444)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
