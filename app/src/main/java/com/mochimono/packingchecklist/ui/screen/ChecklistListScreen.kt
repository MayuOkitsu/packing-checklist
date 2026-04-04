package com.mochimono.packingchecklist.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mochimono.packingchecklist.domain.model.Checklist
import com.mochimono.packingchecklist.ui.component.BottomNavBar
import com.mochimono.packingchecklist.ui.component.BottomNavTab
import com.mochimono.packingchecklist.ui.navigation.Screen
import com.mochimono.packingchecklist.ui.viewmodel.ChecklistListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistListScreen(
    navController: NavController,
    viewModel: ChecklistListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var deleteTarget by remember { mutableStateOf<Checklist?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("持ち物チェックリスト", fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Column {
                // FABエリア
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate(Screen.CreateChecklist.route) },
                        containerColor = Color(0xFF6750A4),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(28.dp)
                    ) { Text("＋ 新規作成", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                    ExtendedFloatingActionButton(
                        onClick = { navController.navigate(Screen.TemplateSelection.route) },
                        containerColor = Color(0xFFF5F0FF),
                        contentColor = Color(0xFF6750A4),
                        shape = RoundedCornerShape(28.dp),
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) { Text("📋 テンプレートから作成", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                }
                BottomNavBar(currentTab = BottomNavTab.Home, navController = navController)
            }
        }
    ) { padding ->
        val checklists = uiState.data.orEmpty()

        if (checklists.isEmpty() && !uiState.isLoading) {
            // 空状態
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📦", fontSize = 48.sp)
                    Text("チェックリストがありません", color = Color(0xFF999999))
                    Text("下のボタンから作成しましょう", fontSize = 13.sp, color = Color(0xFF999999), modifier = Modifier.padding(top = 4.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(checklists, key = { it.id }) { checklist ->
                    ChecklistCard(
                        checklist = checklist,
                        onClick = { navController.navigate(Screen.ChecklistDetail.createRoute(checklist.id)) },
                        onDelete = { deleteTarget = checklist }
                    )
                }
            }
        }
    }

    // 削除確認ダイアログ
    deleteTarget?.let { cl ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("削除しますか？", fontSize = 18.sp) },
            text = { Text("「${cl.title}」を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteChecklist(cl.id); deleteTarget = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) { Text("削除", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("キャンセル", color = Color(0xFF6750A4))
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun ChecklistCard(
    checklist: Checklist,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = (if (checklist.isSharingEnabled) "🔗 " else "") + checklist.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1C1B1F),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dateFormat.format(Date(checklist.createdAt)),
                    fontSize = 12.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "${checklist.checkedCount}/${checklist.totalCount}",
                fontSize = 13.sp,
                color = Color(0xFF6750A4),
                fontWeight = FontWeight.SemiBold
            )
            TextButton(
                onClick = onDelete,
                contentPadding = PaddingValues(4.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Text("✕", fontSize = 16.sp, color = Color(0xFFCCCCCC))
            }
        }
    }
}
