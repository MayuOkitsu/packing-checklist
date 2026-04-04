package com.mochimono.packingchecklist.ui.screen

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mochimono.packingchecklist.domain.model.ChecklistItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAsTemplateDialog(
    items: List<ChecklistItem>,
    onSave: (label: String, selectedItemNames: List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val selectedIds = remember { mutableStateMapOf<String, Boolean>().apply { items.forEach { put(it.id, true) } } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("テンプレートとして保存", fontSize = 18.sp, color = Color(0xFF1C1B1F))
            Spacer(Modifier.height(12.dp))
            Text("ラベル（目的地・用途）", fontSize = 13.sp, color = Color(0xFF666666))
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                placeholder = { Text("例: USJ", fontSize = 14.sp) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Text("保存する項目を選択:", fontSize = 13.sp, color = Color(0xFF666666))
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items) { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = selectedIds[item.id] == true,
                            onCheckedChange = { selectedIds[item.id] = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF6750A4))
                        )
                        Text(item.name, fontSize = 14.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            errorMessage?.let {
                Text(it, color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("キャンセル", color = Color(0xFF6750A4))
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val selected = items.filter { selectedIds[it.id] == true }.map { it.name }
                        when {
                            label.isBlank() -> errorMessage = "ラベルを入力してください"
                            selected.isEmpty() -> errorMessage = "項目を1つ以上選択してください"
                            else -> onSave(label.trim(), selected)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) { Text("保存", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}
