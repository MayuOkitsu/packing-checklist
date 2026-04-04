package com.mochimono.packingchecklist.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mochimono.packingchecklist.ui.navigation.Screen

enum class BottomNavTab(val emoji: String, val label: String, val route: String) {
    Home("🏠", "ホーム", Screen.ChecklistList.route),
    Bag("👜", "鞄管理", Screen.BagManagement.route),
    Template("📋", "テンプレート", Screen.TemplateManagement.route),
    Family("👨\u200D👩\u200D👧\u200D👦", "家族共有", Screen.FamilyGroup.route)
}

@Composable
fun BottomNavBar(
    currentTab: BottomNavTab,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = Color.White
    ) {
        Column {
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))
            Row(modifier = Modifier.fillMaxWidth()) {
                BottomNavTab.entries.forEach { tab ->
                    val isActive = tab == currentTab
                    val color = if (isActive) Color(0xFF6750A4) else Color(0xFF999999)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextButton(
                            onClick = {
                                if (!isActive) {
                                    navController.navigate(tab.route) {
                                        popUpTo(Screen.ChecklistList.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(tab.emoji, fontSize = 20.sp)
                                Text(tab.label, fontSize = 10.sp, color = color)
                            }
                        }
                    }
                }
            }
        }
    }
}
