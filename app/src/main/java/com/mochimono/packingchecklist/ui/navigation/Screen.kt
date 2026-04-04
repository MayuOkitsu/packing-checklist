package com.mochimono.packingchecklist.ui.navigation

sealed class Screen(val route: String) {
    data object ChecklistList : Screen("checklist_list")
    data object ChecklistDetail : Screen("checklist_detail/{checklistId}") {
        fun createRoute(checklistId: String) = "checklist_detail/$checklistId"
    }
    data object CreateChecklist : Screen("create_checklist")
    data object TemplateSelection : Screen("template_selection")
    data object TemplateManagement : Screen("template_management")
    data object CustomTemplateEdit : Screen("custom_template_edit/{templateId}") {
        fun createRoute(templateId: String) = "custom_template_edit/$templateId"
    }
    data object BagManagement : Screen("bag_management")
    data object FamilyGroup : Screen("family_group")
}
