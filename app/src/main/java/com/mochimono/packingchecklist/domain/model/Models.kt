package com.mochimono.packingchecklist.domain.model

data class Checklist(
    val id: String,
    val title: String,
    val createdAt: Long,
    val isSharingEnabled: Boolean,
    val items: List<ChecklistItem> = emptyList(),
    val checkedCount: Int = 0,
    val totalCount: Int = 0
)

data class ChecklistItem(
    val id: String,
    val checklistId: String,
    val name: String,
    val isChecked: Boolean,
    val bag: Bag? = null,
    val lastModifiedAt: Long = 0L
)

data class Bag(
    val id: String,
    val name: String
)

data class CustomTemplate(
    val id: String,
    val label: String,
    val items: List<String>
)

data class FamilyGroup(
    val id: String,
    val inviteCode: String
)

data class TemplateItem(
    val id: String,
    val name: String,
    val sortOrder: Int
)

data class BagSuggestion(
    val bag: Bag,
    val priority: Int // 0: 直前割り当て, 1: 割り当て数順, 2: その他
)
