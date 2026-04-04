package com.mochimono.packingchecklist

import com.mochimono.packingchecklist.data.repository.ChecklistItemRepository
import com.mochimono.packingchecklist.domain.model.Bag
import com.mochimono.packingchecklist.domain.model.ChecklistItem
import com.mochimono.packingchecklist.domain.usecase.ToggleCheckItemUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.mockk

class ToggleCheckItemUseCasePropertyTest : FunSpec({

    val itemRepo = mockk<ChecklistItemRepository>()
    val useCase = ToggleCheckItemUseCase(itemRepo)

    // Feature: packing-checklist, Property 6: チェック操作の状態遷移
    test("未チェック状態のItemをチェックするとisChecked=trueかつ選択Bagが割り当てられる") {
        checkAll(
            100,
            Arb.string(1..20).filter { it.isNotBlank() }, // itemId
            Arb.string(1..20).filter { it.isNotBlank() }, // checklistId
            Arb.string(1..20).filter { it.isNotBlank() }, // name
            Arb.string(1..20).filter { it.isNotBlank() }.orNull(0.3) // bagId (nullable)
        ) { itemId, checklistId, name, bagId ->
            val uncheckedItem = ChecklistItem(itemId, checklistId, name, isChecked = false, bag = null)

            val expectedBag = bagId?.let { Bag(it, "bag") }
            coEvery { itemRepo.toggleCheck(itemId, bagId) } returns ChecklistItem(
                itemId, checklistId, name, isChecked = true, bag = expectedBag
            )

            val result = useCase(uncheckedItem, bagId)
            result.isChecked shouldBe true
            if (bagId != null) {
                result.bag?.id shouldBe bagId
            } else {
                result.bag.shouldBeNull()
            }
        }
    }

    // Feature: packing-checklist, Property 7: チェック解除によるBag割り当て解除
    test("チェック済みItemを解除するとisChecked=falseかつBag=nullになる") {
        checkAll(
            100,
            Arb.string(1..20).filter { it.isNotBlank() }, // itemId
            Arb.string(1..20).filter { it.isNotBlank() }, // checklistId
            Arb.string(1..20).filter { it.isNotBlank() }, // name
            Arb.string(1..20).filter { it.isNotBlank() }  // bagId
        ) { itemId, checklistId, name, bagId ->
            val checkedItem = ChecklistItem(
                itemId, checklistId, name, isChecked = true, bag = Bag(bagId, "bag")
            )

            coEvery { itemRepo.uncheckItem(itemId) } returns ChecklistItem(
                itemId, checklistId, name, isChecked = false, bag = null
            )

            val result = useCase(checkedItem)
            result.isChecked shouldBe false
            result.bag.shouldBeNull()
        }
    }
})
