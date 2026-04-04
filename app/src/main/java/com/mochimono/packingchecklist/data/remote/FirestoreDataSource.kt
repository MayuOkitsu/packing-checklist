package com.mochimono.packingchecklist.data.remote

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // --- Collection references ---

    private fun groupDoc(groupId: String) =
        firestore.collection("familyGroups").document(groupId)

    private fun membersCol(groupId: String) =
        groupDoc(groupId).collection("members")

    private fun checklistsCol(groupId: String) =
        groupDoc(groupId).collection("sharedChecklists")

    private fun itemsCol(groupId: String, checklistId: String) =
        checklistsCol(groupId).document(checklistId).collection("items")

    private fun bagsCol(groupId: String) =
        groupDoc(groupId).collection("sharedBags")

    private fun templatesCol(groupId: String) =
        groupDoc(groupId).collection("sharedTemplates")

    // --- FamilyGroup ---

    suspend fun createFamilyGroup(groupId: String, inviteCode: String) {
        groupDoc(groupId).set(
            mapOf("inviteCode" to inviteCode, "createdAt" to FieldValue.serverTimestamp())
        ).await()
    }

    suspend fun findGroupByInviteCode(inviteCode: String): DocumentSnapshot? {
        val snap = firestore.collection("familyGroups")
            .whereEqualTo("inviteCode", inviteCode)
            .limit(1)
            .get().await()
        return snap.documents.firstOrNull()
    }

    // --- Members ---

    suspend fun joinGroup(groupId: String, userId: String) {
        membersCol(groupId).document(userId).set(
            mapOf("joinedAt" to FieldValue.serverTimestamp())
        ).await()
    }

    suspend fun leaveGroup(groupId: String, userId: String) {
        membersCol(groupId).document(userId).delete().await()
    }

    // --- SharedChecklists ---

    suspend fun setChecklist(groupId: String, checklistId: String, data: Map<String, Any>) {
        val withTimestamp = data.toMutableMap().apply {
            putIfAbsent("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }
        checklistsCol(groupId).document(checklistId).set(withTimestamp).await()
    }

    suspend fun deleteChecklist(groupId: String, checklistId: String) {
        checklistsCol(groupId).document(checklistId).delete().await()
    }

    fun observeChecklists(groupId: String): Flow<QuerySnapshot> = callbackFlow {
        val registration = checklistsCol(groupId).addSnapshotListener { snap, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            snap?.let { trySend(it) }
        }
        awaitClose { registration.remove() }
    }

    // --- Checklist Items (subcollection) ---

    suspend fun setItem(groupId: String, checklistId: String, itemId: String, data: Map<String, Any>) {
        val withTimestamp = data.toMutableMap().apply {
            put("lastModifiedAt", FieldValue.serverTimestamp())
        }
        itemsCol(groupId, checklistId).document(itemId).set(withTimestamp).await()
    }

    suspend fun deleteItem(groupId: String, checklistId: String, itemId: String) {
        itemsCol(groupId, checklistId).document(itemId).delete().await()
    }

    fun observeItems(groupId: String, checklistId: String): Flow<QuerySnapshot> = callbackFlow {
        val registration = itemsCol(groupId, checklistId).addSnapshotListener { snap, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            snap?.let { trySend(it) }
        }
        awaitClose { registration.remove() }
    }

    // --- SharedBags ---

    suspend fun setBag(groupId: String, bagId: String, data: Map<String, Any>) {
        val withTimestamp = data.toMutableMap().apply {
            putIfAbsent("createdAt", FieldValue.serverTimestamp())
        }
        bagsCol(groupId).document(bagId).set(withTimestamp).await()
    }

    suspend fun deleteBag(groupId: String, bagId: String) {
        bagsCol(groupId).document(bagId).delete().await()
    }

    fun observeBags(groupId: String): Flow<QuerySnapshot> = callbackFlow {
        val registration = bagsCol(groupId).addSnapshotListener { snap, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            snap?.let { trySend(it) }
        }
        awaitClose { registration.remove() }
    }

    // --- SharedTemplates ---

    suspend fun setTemplate(groupId: String, templateId: String, data: Map<String, Any>) {
        val withTimestamp = data.toMutableMap().apply {
            putIfAbsent("createdAt", FieldValue.serverTimestamp())
            put("updatedAt", FieldValue.serverTimestamp())
        }
        templatesCol(groupId).document(templateId).set(withTimestamp).await()
    }

    suspend fun deleteTemplate(groupId: String, templateId: String) {
        templatesCol(groupId).document(templateId).delete().await()
    }

    fun observeTemplates(groupId: String): Flow<QuerySnapshot> = callbackFlow {
        val registration = templatesCol(groupId).addSnapshotListener { snap, error ->
            if (error != null) { close(error); return@addSnapshotListener }
            snap?.let { trySend(it) }
        }
        awaitClose { registration.remove() }
    }
}
