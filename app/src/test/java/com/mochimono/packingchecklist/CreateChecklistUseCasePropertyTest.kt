package com.mochimono.packingchecklist

import com.mochimono.packingchecklist.data.repository.ChecklistItemRepository
import com.mochimono.packingchecklist.data.repository.ChecklistRepository
import com.mochimono.packingchecklist.data.repository.TemplateRepository
import com.mochimono.packingchecklist.domain.model.Checklist
import com.mochimono.packingchecklist.domain.model.ChecklistItem
import com.mochimono.packingchecklist.domain.model.Result
import com.mochimono.packingchecklist.domain.model.TemplateItem
import com.mochimono.packingchecklist.domain.usecase.CreateChecklistUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class CreateChecklistUseCasePropertyTest : FunSpec({

    val checklistRepo = mockk<ChecklistRepository>()
    val itemRepo = mockk<ChecklistItemRepository>()
    val templateRepo = mockk<TemplateRepository>()
    val useCase = CreateChecklistUseCase(checklistRepo, itemRepo, templateRepo)

    // Feature: packing-checklist, Property 1: チェックリスト保存のラウンドトリップ
    test("有効なタイトルでChecklistを作成すると同じタイトルのChecklistが返される") {
        checkAll(100, Arb.string(1..100).filter { it.isNotBlank() }) { title ->
            val trimmed = title.trim()
            coEvery { checklistRepo.createChecklist(trimmed) } returns Checklist(
                id = UUID.randomUUID().toString(), title = trimmed,
                createdAt = System.currentTimeMillis(), isSharingEnabled = false
            )
            coEvery { templateRepo.getDefaultTemplate() } returns flowOf(emptyList())

            val result = useCase(title)
            result.shouldBeInstanceOf<Result.Success<Checklist>>()
            result.data.title shouldBe trimmed
        }
    }

    // Feature: packing-checklist, Property 2: 空文字列バリデーション
    test("空文字列・ホワイトスペースのみの文字列はバリデーションで拒否される") {
        checkAll(100, Arb.string(0..50).filter { it.isBlank() }) { input ->
            val result = useCase(input)
            result.shouldBeInstanceOf<Result.Error>()
            coVerify(exactly = 0) { checklistRepo.createChecklist(any()) }
        }
    }

    // Feature: packing-checklist, Property 3: Default_Template自動適用
    test("新しいChecklistを作成するとDefault_Templateの全項目が未チェック・Bag未割り当てで追加される") {
        checkAll(
            100,
            Arb.string(1..50).filter { it.isNotBlank() },
            Arb.list(Arb.string(1..30).filter { it.isNotBlank() }, 0..10)
        ) { title, templateNames ->
            val trimmed = title.trim()
            val templateItems = templateNames.mapIndexed { i, name ->
                TemplateItem("t$i", name, i)
            }
            val checklistId = UUID.randomUUID().toString()

            coEvery { checklistRepo.createChecklist(trimmed) } returns Checklist(
                id = checklistId, title = trimmed,
                createdAt = System.currentTimeMillis(), isSharingEnabled = false
            )
            coEvery { templateRepo.getDefaultTemplate() } returns flowOf(templateItems)

            val addedItems = mutableListOf<Pair<String, String>>()
            coEvery { itemRepo.addItem(any(), any()) } answers {
                val cId = firstArg<String>()
                val name = secondArg<String>()
                addedItems.add(cId to name)
                ChecklistItem(UUID.randomUUID().toString(), cId, name, false, null, 0L)
            }

            addedItems.clear()
            val result = useCase(title)
            result.shouldBeInstanceOf<Result.Success<Checklist>>()

            addedItems.size shouldBe templateNames.size
            addedItems.forEach { (cId, _) -> cId shouldBe checklistId }
            addedItems.map { it.second } shouldBe templateNames
        }
    }
})
