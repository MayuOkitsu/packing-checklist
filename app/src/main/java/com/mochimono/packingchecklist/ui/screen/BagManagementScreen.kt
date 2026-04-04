package com.mochimono.packingchecklist.ui.screen

import androidx.compose.foundation.clickable
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
import com.mochimono.packingchecklist.ui.viewmodel.BagViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagManagementScreen(
    navController: NavController,
    viewModel: BagViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState.data
    var newBagName by remember { mutableStateOf("") }
    var renameDialogTarget by remember { mutableStateOf<String?>(null) }
    var renameText by remember { mutableStateOf("") }

    // 名前変更ダイアログ
    renameDialogTarget?.let { bagId ->
        AlertDialog(
            onDismissRequest = { renameDialogTarget = null },
            title = { Text("鞄の名前を変更") },
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
                    viewModel.updateBagName(bagId, renameText)
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
                title = { Text("鞄管理") },
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
            BottomNavBar(currentTab = BottomNavTab.Bag, navController = navController)
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Bag一覧
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (state?.bags.isNullOrEmpty()) {
                    item {
                        Text(
                            "鞄が登録されていません",
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    items(state?.bags ?: emptyList()) { bag ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("👜", fontSize = 24.sp)
                                Text(
                                    bag.name,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            renameText = bag.name
                                            renameDialogTarget = bag.id
                                        },
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                TextButton(
                                    onClick = { viewModel.deleteBag(bag.id) },
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Text("✕", color = Color(0xFFCCCCCC), fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 下部入力バー + エラーメッセージ
            Column {
                state?.errorMessage?.let { error ->
                    Text(
                        error,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Surface(tonalElevation = 1.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newBagName,
                            onValueChange = {
                                newBagName = it
                                viewModel.clearError()
                            },
                            placeholder = { Text("鞄の名前を入力...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.addBag(newBagName)
                                if (newBagName.isNotBlank()) newBagName = ""
                            })
                        )
                        Button(
                            onClick = {
                                viewModel.addBag(newBagName)
                                if (newBagName.isNotBlank()) newBagName = ""
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("登録", fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }
    }
}
