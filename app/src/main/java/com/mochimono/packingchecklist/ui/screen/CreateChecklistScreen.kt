package com.mochimono.packingchecklist.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.mochimono.packingchecklist.ui.navigation.Screen
import com.mochimono.packingchecklist.ui.viewmodel.CreateChecklistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChecklistScreen(
    navController: NavController,
    viewModel: CreateChecklistViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.createdChecklistId) {
        state.createdChecklistId?.let {
            navController.navigate(Screen.ChecklistList.route) {
                popUpTo(Screen.ChecklistList.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新規チェックリスト", fontSize = 18.sp) },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("←", color = Color.White, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6750A4),
                    titleContentColor = Color.White
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
            Text("タイトル", fontSize = 13.sp, color = Color(0xFF666666))
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                placeholder = { Text("例: 沖縄旅行 2026", fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                isError = state.errorMessage != null
            )
            if (state.errorMessage != null) {
                Text(
                    state.errorMessage!!,
                    color = Color(0xFFEF4444),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = viewModel::create,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
            ) {
                Text("作成", fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }
        }
    }
}
