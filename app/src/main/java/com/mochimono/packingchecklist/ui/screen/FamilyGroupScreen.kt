package com.mochimono.packingchecklist.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mochimono.packingchecklist.ui.component.BottomNavBar
import com.mochimono.packingchecklist.ui.component.BottomNavTab
import com.mochimono.packingchecklist.ui.navigation.Screen
import com.mochimono.packingchecklist.ui.viewmodel.FamilyGroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyGroupScreen(
    navController: NavController,
    viewModel: FamilyGroupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState.data
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("家族共有") },
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
            BottomNavBar(currentTab = BottomNavTab.Family, navController = navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (state?.group == null) {
                // 未参加時: グループ作成カード
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("グループを作成", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "招待コードを生成して家族に共有しましょう",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Button(
                            onClick = { viewModel.createGroup() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("グループを作成", fontWeight = FontWeight.SemiBold) }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 招待コード入力カード
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("グループに参加", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "家族から受け取った招待コードを入力",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        OutlinedTextField(
                            value = state?.inviteCodeInput ?: "",
                            onValueChange = { viewModel.updateInviteCodeInput(it) },
                            placeholder = { Text("招待コードを入力") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.joinGroup() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("参加", fontWeight = FontWeight.SemiBold) }
                    }
                }
            } else {
                // 参加済み: 招待コード表示カード
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("家族グループ", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("招待コード:", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))

                        // モノスペース招待コード（タップでコピー）
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF5F0FF),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clickable {
                                    viewModel.getInviteCode()?.let { code ->
                                        clipboardManager.setText(AnnotatedString(code))
                                        viewModel.onCodeCopied()
                                    }
                                }
                        ) {
                            Text(
                                state.group.inviteCode,
                                modifier = Modifier.padding(12.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            if (state.codeCopied) "コピーしました ✅" else "タップでコピー",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(Modifier.height(16.dp))

                        // 退出ボタン
                        Button(
                            onClick = { viewModel.leaveGroup() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) { Text("グループから退出", fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }
    }
}
