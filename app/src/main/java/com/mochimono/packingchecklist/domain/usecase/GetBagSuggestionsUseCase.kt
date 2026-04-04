package com.mochimono.packingchecklist.domain.usecase

import com.mochimono.packingchecklist.data.repository.BagRepository
import com.mochimono.packingchecklist.data.repository.ChecklistItemRepository
import com.mochimono.packingchecklist.domain.model.BagSuggestion
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetBagSuggestionsUseCase @Inject constructor(
    private val bagRepository: BagRepository,
    private val checklistItemRepository: ChecklistItemRepository
) {
    suspend operator fun invoke(checklistId: String, lastAssignedBagId: String?): List<BagSuggestion> {
        val allBags = bagRepository.getAllBags().first()
        if (allBags.isEmpty()) return emptyList()

        val items = checklistItemRepository.getItemsByChecklistId(checklistId).first()
        val bagCounts = items.filter { it.bag != null }.groupingBy { it.bag!!.id }.eachCount()

        val lastBag = allBags.find { it.id == lastAssignedBagId }
        val remaining = if (lastBag != null) allBags - lastBag else allBags

        val sorted = remaining.sortedByDescending { bagCounts[it.id] ?: 0 }
        val countBags = sorted.filter { (bagCounts[it.id] ?: 0) > 0 }
        val otherBags = sorted.filter { (bagCounts[it.id] ?: 0) == 0 }

        return buildList {
            lastBag?.let { add(BagSuggestion(it, priority = 0)) }
            countBags.forEach { add(BagSuggestion(it, priority = 1)) }
            otherBags.forEach { add(BagSuggestion(it, priority = 2)) }
        }
    }
}
