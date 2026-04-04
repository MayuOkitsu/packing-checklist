package com.mochimono.packingchecklist.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mochimono.packingchecklist.ui.navigation.Screen
import com.mochimono.packingchecklist.ui.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateSelectionScreen(
    navController: NavController,
    viewModel: TemplateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState.data
    var title by remember { mutableStateOf("") }

    // 作成成功時にリスト画面へ遷移
    LaunchedEffect(state?.createdChecklistId) {
        state?.createdChecklistId?.let {
            navController.navigate(Screen.ChecklistList.route) {
                popUpTo(Screen.ChecklistList.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("テンプレートから作成") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "使いたいテンプレートを選択してください（複数可）",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(state?.customTemplates ?: emptyList()) { template ->
                    val selected = state?.selectedTemplateIds?.contains(template.id) == true
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, if (selected) MaterialTheme.colorScheme.primary else Color(0xFFEEEEEE)),
                        color = if (selected) Color(0xFFF5F0FF) else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleTemplateSelection(template.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = { viewModel.toggleTemplateSelection(template.id) }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(template.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    template.items.joinToString(", "),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Text("チェックリストのタイトル", fontSize = 13.sp, color = Color.Gray)
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("例: USJ旅行 2026") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.createFromSelectedTemplates(title) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("作成", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
