package com.mochimono.packingchecklist.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mochimono.packingchecklist.domain.model.BagSuggestion
import com.mochimono.packingchecklist.domain.model.ChecklistItem
import com.mochimono.packingchecklist.ui.viewmodel.ChecklistDetailViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistDetailScreen(
    checklistId: String,
    navController: NavController,
    viewModel: ChecklistDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lastBagId by viewModel.lastAssignedBagId.collectAsStateWithLifecycle()
    val state = uiState.data ?: return

    var menuExpanded by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }
    var pendingCheckItem by remember { mutableStateOf<ChecklistItem?>(null) }
    var bagSuggestions by remember { mutableStateOf<List<BagSuggestion>>(emptyList()) }
    var newItemText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<ChecklistItem?>(null) }
    var editingItemName by remember { mutableStateOf("") }
    var showTitleEditDialog by remember { mutableStateOf(false) }
    var editingTitle by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.title, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.combinedClickable(
                            onClick = {},
                            onLongClick = { editingTitle = state.title; showTitleEditDialog = true }
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る", tint = Color.White)
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            state.progress,
                            fontSize = 13.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "メニュー", tint = Color.White)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("テンプレートとして保存", fontSize = 14.sp) },
                                onClick = { menuExpanded = false; showTemplateDialog = true }
                            )
                            DropdownMenuItem(
                                text = { Text("共有設定を切り替え", fontSize = 14.sp) },
                                onClick = { menuExpanded = false; viewModel.toggleSharing() }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 一覧/鞄別トグル
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(false to "一覧", true to "鞄別").forEach { (grouped, label) ->
                    val active = state.isGroupedView == grouped
                    OutlinedButton(
                        onClick = { if (!active) viewModel.toggleGroupedView() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (active) Color(0xFF6750A4) else Color.White,
                            contentColor = if (active) Color.White else Color.Black
                        ),
                        border = if (active) null else ButtonDefaults.outlinedButtonBorder(true)
                    ) { Text(label, fontSize = 13.sp) }
                }
            }

            // アイテムリスト
            val onToggle: (ChecklistItem) -> Unit = { item ->
                if (item.isChecked) {
                    viewModel.toggleCheck(item, null)
                } else {
                    scope.launch {
                        val suggestions = viewModel.getBagSuggestions()
                        if (suggestions.isEmpty()) {
                            viewModel.toggleCheck(item, null)
                        } else {
                            pendingCheckItem = item
                            bagSuggestions = suggestions
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                if (state.isGroupedView) {
                    state.groupedItems.forEach { group ->
                        item {
                            BagGroupHeader(bagName = group.bag?.name, count = group.items.size)
                        }
                        items(group.items, key = { it.id }) { item ->
                            CheckItem(item, onToggle = { onToggle(item) }, onDelete = { viewModel.deleteItem(item.id) },
                                onEditName = { editingItem = item; editingItemName = item.name })
                        }
                    }
                } else {
                    items(state.items, key = { it.id }) { item ->
                        CheckItem(item, onToggle = { onToggle(item) }, onDelete = { viewModel.deleteItem(item.id) },
                            onEditName = { editingItem = item; editingItemName = item.name })
                    }
                }
            }

            // 下部入力バー
            Surface(color = Color.White) {
                Column {
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newItemText,
                            onValueChange = { newItemText = it },
                            placeholder = { Text("持ち物を追加...", fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.addItem(newItemText); newItemText = "" },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                        ) { Text("追加", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }
    }

    // Bag選択ダイアログ
    pendingCheckItem?.let { item ->
        if (bagSuggestions.isNotEmpty()) {
            BagSelectionDialog(
                suggestions = bagSuggestions,
                onSelectBag = { bagId -> viewModel.toggleCheck(item, bagId); pendingCheckItem = null },
                onSkip = { viewModel.toggleCheck(item, null); pendingCheckItem = null },
                onDismiss = { pendingCheckItem = null }
            )
        }
    }

    // テンプレート保存ダイアログ
    if (showTemplateDialog) {
        SaveAsTemplateDialog(
            items = state.items,
            onSave = { label, names -> viewModel.saveAsTemplate(label, names); showTemplateDialog = false },
            onDismiss = { showTemplateDialog = false }
        )
    }

    // タイトル編集ダイアログ（要件4.1: 長押し）
    if (showTitleEditDialog) {
        AlertDialog(
            onDismissRequest = { showTitleEditDialog = false },
            title = { Text("タイトルを編集") },
            text = {
                OutlinedTextField(
                    value = editingTitle,
                    onValueChange = { editingTitle = it },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editingTitle.isNotBlank()) {
                        viewModel.updateTitle(editingTitle.trim())
                        showTitleEditDialog = false
                    }
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showTitleEditDialog = false }) { Text("キャンセル") }
            }
        )
    }

    // 項目名編集ダイアログ（要件4.5: タップ）
    editingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("項目名を編集") },
            text = {
                OutlinedTextField(
                    value = editingItemName,
                    onValueChange = { editingItemName = it },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editingItemName.isNotBlank()) {
                        viewModel.updateItemName(item.id, editingItemName.trim())
                        editingItem = null
                    }
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) { Text("キャンセル") }
            }
        )
    }
}

@Composable
private fun BagGroupHeader(bagName: String?, count: Int) {
    Text(
        text = if (bagName == null) "📦 未分類（$count）" else "👜 $bagName（$count）",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (bagName == null) Color(0xFF999999) else Color(0xFF6750A4),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

@Composable
private fun CheckItem(
    item: ChecklistItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEditName: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                item.name,
                fontSize = 14.sp,
                color = if (item.isChecked) Color(0xFF999999) else Color(0xFF333333),
                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.weight(1f).clickable { onEditName() }
            )
            item.bag?.let { bag ->
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF5F0FF)) {
                    Text(
                        "👜 ${bag.name}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "${item.name}を削除", tint = Color(0xFFCCCCCC), modifier = Modifier.size(16.dp))
            }
        }
    }
}
