package com.mochimono.packingchecklist.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mochimono.packingchecklist.domain.model.BagSuggestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BagSelectionDialog(
    suggestions: List<BagSuggestion>,
    onSelectBag: (String) -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("どの鞄に入れた？", fontSize = 18.sp, color = Color(0xFF1C1B1F))
            Spacer(Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(suggestions) { suggestion ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectBag(suggestion.bag.id) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.5.dp, Color(0xFFEEEEEE)),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👜", fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                suggestion.bag.name + if (suggestion.priority == 0) " ⭐" else "",
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (suggestion.assignedCount > 0) {
                                Text("${suggestion.assignedCount}個入り", fontSize = 12.sp, color = Color(0xFF999999))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("スキップ（鞄なし）", color = Color(0xFF6750A4), fontSize = 14.sp)
            }
        }
    }
}
