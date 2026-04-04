# Implementation Plan: 持ち物チェックリスト (packing-checklist)

## Overview

Kotlin + Jetpack Compose によるAndroidアプリ「持ち物チェックリスト」機能の実装計画。Clean Architecture（Data Layer → Domain Layer → UI Layer）の順で、依存関係を考慮した段階的な実装を行う。技術スタック: Room Database, Firebase Firestore, Hilt, Kotlin Coroutines + Flow。

## Tasks

- [ ] 1. プロジェクト基盤セットアップ
  - [ ] 1.1 Gradle依存関係とHiltセットアップ
    - Room, Firebase Firestore, Firebase Auth, Hilt, Kotest, Coroutines, Compose Material 3 の依存関係を `build.gradle.kts` に追加
    - Hilt Application クラスと `@HiltAndroidApp` アノテーション設定
    - Kotest Property Testing モジュールのテスト依存関係追加
    - _Requirements: 7.1, 7.3_
  - [ ] 1.2 Domain Models の作成
    - `Checklist`, `ChecklistItem`, `Bag`, `CustomTemplate`, `FamilyGroup`, `BagSuggestion` データクラスを作成
    - `Result<T>` sealed class と `UiState<T>` データクラスを作成
    - `ValidationResult` sealed class を作成（タイトル・名前のバリデーション用）
    - _Requirements: 1.2, 2.2, 3.3, 8.1, 9.2, 10.2_

- [ ] 2. Data Layer - Room Database
  - [ ] 2.1 Room Entity の作成
    - `ChecklistEntity`, `ChecklistItemEntity`, `BagEntity`, `DefaultTemplateItemEntity`, `CustomTemplateEntity`, `CustomTemplateItemEntity`, `FamilyGroupEntity`, `PendingSyncEntity` を設計書通りに作成
    - ForeignKey, Index の設定
    - _Requirements: 7.1_
  - [ ] 2.2 Room DAO の作成
    - `ChecklistDao`: CRUD + 作成日降順ソート取得（Flow）
    - `ChecklistItemDao`: CRUD + checklistId別取得（Flow）、チェック状態更新、Bag割り当て更新
    - `BagDao`: CRUD（Flow）
    - `DefaultTemplateItemDao`: CRUD（Flow）
    - `CustomTemplateDao` + `CustomTemplateItemDao`: CRUD（Flow）
    - `FamilyGroupDao`: CRUD（Flow）
    - `PendingSyncDao`: 未同期エントリ取得、同期済み更新
    - _Requirements: 7.1, 7.2, 7.3_
  - [ ] 2.3 Room Database クラスの作成
    - `PackingChecklistDatabase` を作成し、全Entity・DAOを登録
    - Default_Template の初期データ（パスポート、充電器、着替え、歯ブラシ、常備薬）をプリポピュレート
    - _Requirements: 5.1, 7.1_
  - [ ] 2.4 Room DAO のユニットテスト
    - In-memory Room Database を使用した DAO 操作テスト
    - CRUD操作、カスケード削除、Flow emission の検証
    - _Requirements: 7.1, 7.2_

- [ ] 3. Data Layer - Firebase DataSource
  - [ ] 3.1 Firebase Anonymous Authentication セットアップ
    - Firebase プロジェクト設定と `google-services.json` 配置
    - `AuthManager` クラス作成: 匿名認証の自動サインイン、UID取得
    - _Requirements: 10.1, 10.6_
  - [ ] 3.2 Firestore DataSource の作成
    - `FirestoreDataSource` クラス: familyGroups, sharedChecklists, sharedBags, sharedTemplates の CRUD
    - items サブコレクションへの個別アイテム読み書き
    - `FieldValue.serverTimestamp()` を使用したタイムスタンプ設定
    - _Requirements: 7.4, 10.3, 11.1_
  - [ ] 3.3 Firestore セキュリティルールのデプロイ
    - 設計書記載のセキュリティルールを `firestore.rules` に作成
    - メンバー判定関数 `isMember()` の実装
    - _Requirements: 10.6_

- [ ] 4. Data Layer - Repository 実装
  - [ ] 4.1 ChecklistRepository の実装
    - `ChecklistRepositoryImpl`: Room DAO を使用した Checklist CRUD
    - `getAllChecklists()`: 作成日降順の Flow 返却
    - `setSharingEnabled()`: 共有設定の切り替え
    - _Requirements: 1.2, 1.5, 4.4, 6.1, 6.4, 10.10_
  - [ ] 4.2 ChecklistItemRepository の実装
    - `ChecklistItemRepositoryImpl`: Room DAO を使用した ChecklistItem CRUD
    - `toggleCheck()`: チェック状態切替 + Bag割り当て
    - `uncheckItem()`: チェック解除 + Bag割り当て解除
    - _Requirements: 2.2, 3.3, 3.4, 3.6, 4.2, 4.5_
  - [ ] 4.3 TemplateRepository の実装
    - `TemplateRepositoryImpl`: Default/Custom Template の CRUD
    - Default Template の取得・更新
    - Custom Template の保存（ラベル + 選択項目）、削除、更新
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.8_
  - [ ] 4.4 BagRepository の実装
    - `BagRepositoryImpl`: Bag CRUD、Bag_Registry 管理
    - _Requirements: 9.1, 9.2, 9.4, 9.5_
  - [ ] 4.5 FamilyGroupRepository の実装
    - `FamilyGroupRepositoryImpl`: グループ作成（UUID v4招待コード生成）、参加、退出
    - Firestore の members サブコレクションへのドキュメント追加/削除
    - _Requirements: 10.1, 10.2, 10.5, 10.6_
  - [ ] 4.6 SyncRepository と SyncManager の実装
    - `SyncRepositoryImpl`: PendingSyncEntity の管理
    - `SyncManager`: オンライン時の Firestore 同期、オフライン時のローカル保存
    - コンフリクト解決: `resolveConflict()` 関数（Last-Write-Wins + ドキュメントID辞書順タイブレーク）
    - Firestore リアルタイムリスナーによる変更受信・マージ
    - ネットワーク復旧時の未同期変更送信
    - _Requirements: 7.4, 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ] 5. Checkpoint - Data Layer 完了確認
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Domain Layer - UseCase 実装
  - [ ] 6.1 CreateChecklistUseCase の実装
    - 空タイトルバリデーション（空文字・ホワイトスペースのみ拒否）
    - Default Template 全項目を未チェック・Bag未割り当てで自動追加
    - isSharingEnabled = false で初期作成
    - _Requirements: 1.2, 1.3, 1.4, 10.9_
  - [ ] 6.2 CreateChecklistUseCase のプロパティテスト
    - **Property 1: チェックリスト保存のラウンドトリップ**
    - **Validates: Requirements 1.2, 7.1, 7.2**
    - **Property 2: 空文字列バリデーション**
    - **Validates: Requirements 1.3, 2.4, 9.3**
    - **Property 3: Default_Template自動適用**
    - **Validates: Requirements 1.4**
    - **Property 18: 新規Checklistの非共有初期設定**
    - **Validates: Requirements 10.9**
  - [ ] 6.3 CreateChecklistFromTemplatesUseCase と MergeTemplatesUseCase の実装
    - Default Template + 複数 Custom Template の統合
    - 項目名ベースの重複除去
    - 全項目を未チェック状態で新規 Checklist 作成
    - _Requirements: 5.6, 5.7_
  - [ ]* 6.4 MergeTemplatesUseCase のプロパティテスト
    - **Property 11: テンプレート統合の重複除去**
    - **Validates: Requirements 5.6, 5.7**
  - [ ] 6.5 ToggleCheckItemUseCase の実装
    - 未チェック → チェック済み（Bag選択 or スキップ）
    - チェック済み → 未チェック（Bag割り当て解除）
    - _Requirements: 3.3, 3.4, 3.5, 3.6_
  - [ ] 6.6 ToggleCheckItemUseCase のプロパティテスト
    - **Property 6: チェック操作の状態遷移**
    - **Validates: Requirements 3.3, 3.4**
    - **Property 7: チェック解除によるBag割り当て解除**
    - **Validates: Requirements 3.6**
  - [ ] 6.7 GetBagSuggestionsUseCase の実装
    - 優先順位計算: 直前割り当てBag → 現在Checklist内の割り当て数順 → 残りのBag
    - _Requirements: 3.1, 3.2_
  - [ ]* 6.8 GetBagSuggestionsUseCase のプロパティテスト
    - **Property 5: Bag選択時の優先順位ソート**
    - **Validates: Requirements 3.1, 3.2**
  - [ ] 6.9 SaveAsCustomTemplateUseCase の実装
    - 既存 Checklist から選択項目を Custom Template として保存
    - ラベル設定
    - _Requirements: 5.3, 5.4_
  - [ ]* 6.10 SaveAsCustomTemplateUseCase のプロパティテスト
    - **Property 12: Custom_Template保存のラウンドトリップ**
    - **Validates: Requirements 5.3, 5.4**
  - [ ] 6.11 DeleteBagUseCase の実装
    - Bag 削除 + 関連 ChecklistItem の Bag 割り当て解除
    - _Requirements: 9.5, 9.6_
  - [ ]* 6.12 DeleteBagUseCase のプロパティテスト
    - **Property 17: Bag削除時の関連アイテム更新**
    - **Validates: Requirements 9.6**
  - [ ] 6.13 SyncChecklistUseCase の実装
    - 共有 Checklist の同期処理
    - 共有設定に基づく公開制御（共有時のみ Remote 同期）
    - _Requirements: 10.3, 10.11, 11.1_
  - [ ]* 6.14 SyncChecklistUseCase のプロパティテスト
    - **Property 19: 共有設定による公開制御**
    - **Validates: Requirements 10.3, 10.11**
  - [ ] 6.15 ManageFamilyGroupUseCase の実装
    - Family Group 作成（UUID v4招待コード生成）、参加、退出
    - 参加時の Bag 情報・Custom Template の Remote 同期
    - _Requirements: 10.1, 10.2, 10.5, 10.7, 10.8_
  - [ ]* 6.16 ManageFamilyGroupUseCase のプロパティテスト
    - **Property 20: Family_Group参加・退出**
    - **Validates: Requirements 10.2, 10.5**
  - [ ] 6.17 コンフリクト解決のプロパティテスト
    - **Property 21: コンフリクト解決（Last-Write-Wins with serverTimestamp）**
    - **Validates: Requirements 11.5**

- [ ] 7. Checkpoint - Domain Layer 完了確認
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 8. UI Layer - ViewModel 実装
  - [ ] 8.1 ChecklistListViewModel の実装
    - チェックリスト一覧取得（作成日降順）
    - 各 Checklist のタイトル、進捗（M/N形式）、作成日、共有アイコン表示データ
    - Checklist 削除処理（削除確認ダイアログ連携）
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 4.3, 4.4, 10.4_
  - [ ]* 8.2 ChecklistListViewModel のプロパティテスト
    - **Property 13: チェックリスト一覧の作成日降順ソート**
    - **Validates: Requirements 6.4**
    - **Property 14: 一覧表示データの完全性**
    - **Validates: Requirements 6.2**
  - [ ] 8.3 ChecklistDetailViewModel の実装
    - チェックリスト詳細表示、項目一覧（Flow）
    - チェック操作（ToggleCheckItemUseCase 呼び出し）
    - 項目追加（空名前バリデーション）、項目削除（スワイプ）、項目名編集
    - Bag別グループ化表示（未分類グループ含む）
    - 進捗計算（M/N形式）、取り消し線表示
    - タイトル編集（長押しダイアログ）
    - テンプレート保存（SaveAsCustomTemplateUseCase 呼び出し）
    - 共有設定切り替え
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.1, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 4.1, 4.2, 4.5, 5.3, 8.1, 8.2, 8.3, 10.10_
  - [ ]* 8.4 ChecklistDetailViewModel のプロパティテスト
    - **Property 4: 持ち物追加による項目数増加**
    - **Validates: Requirements 2.2, 2.3**
    - **Property 8: チェック進捗の正確性**
    - **Validates: Requirements 3.8**
    - **Property 9: Checklist_Item削除**
    - **Validates: Requirements 4.2**
    - **Property 15: Bag別グループ化**
    - **Validates: Requirements 8.2, 8.3**
  - [ ] 8.5 CreateChecklistViewModel の実装
    - タイトル入力バリデーション（空文字拒否、エラーメッセージ表示）
    - Default Template 適用付き作成処理
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_
  - [ ] 8.6 TemplateViewModel の実装
    - Default Template 編集（追加・削除・変更）
    - Custom Template 一覧表示、CRUD、ラベル編集
    - テンプレート選択による新規 Checklist 作成（複数選択対応、重複除去）
    - _Requirements: 5.1, 5.2, 5.4, 5.5, 5.6, 5.7, 5.8_
  - [ ] 8.7 BagViewModel の実装
    - Bag 登録（空名前バリデーション、エラーメッセージ表示）
    - Bag 一覧表示、名前変更、削除（関連 ChecklistItem 更新）
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_
  - [ ]* 8.8 BagViewModel のプロパティテスト
    - **Property 16: Bag登録のラウンドトリップ**
    - **Validates: Requirements 9.2**
  - [ ] 8.9 FamilyGroupViewModel の実装
    - Family Group 作成（招待コード生成・表示）
    - 招待コード入力による参加
    - グループ退出
    - _Requirements: 10.1, 10.2, 10.5, 10.6_

- [ ] 9. Checkpoint - ViewModel 完了確認
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 10. UI Layer - Compose Screen 実装
  - [ ] 10.1 ChecklistListScreen の実装
    - チェックリスト一覧表示（タイトル、進捗、作成日、共有アイコン）
    - 新規作成ボタン、テンプレートから作成ボタン
    - Checklist タップで詳細画面遷移
    - Checklist 左スワイプで削除確認ダイアログ表示
    - 鞄管理、テンプレート管理、家族共有設定への画面遷移
    - _Requirements: 1.1, 4.3, 4.4, 6.1, 6.2, 6.3, 6.4, 10.4_
  - [ ] 10.2 ChecklistDetailScreen の実装
    - Checklist_Item 一覧表示（チェックボックス、名前、Bag名、取り消し線）
    - チェック操作 → BagSelectionDialog 表示
    - 項目追加ボタン → 入力欄表示
    - 項目左スワイプで削除（確認なし）
    - 項目名タップで編集モード
    - タイトル長押しで編集ダイアログ
    - 進捗表示（M/N形式）
    - Bag別グループ化表示切替
    - テンプレートとして保存メニュー
    - 共有設定切り替えメニュー
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.1, 3.7, 3.8, 4.1, 4.2, 4.5, 5.3, 8.1, 8.2, 8.3, 10.10_
  - [ ] 10.3 BagSelectionDialog の実装
    - Bag_Registry の Bag 一覧を優先順位付きで表示
    - Bag 選択またはスキップボタン
    - Bag_Registry が空の場合はダイアログ非表示
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  - [ ] 10.4 SaveAsTemplateDialog の実装
    - Checklist_Item の選択可能な一覧表示
    - ラベル入力欄
    - 選択した項目のみを Custom Template として保存
    - _Requirements: 5.3, 5.4_
  - [ ] 10.5 CreateChecklistScreen の実装
    - タイトル入力フィールド
    - 空タイトル時のエラーメッセージ表示
    - 保存ボタン
    - _Requirements: 1.1, 1.2, 1.3_
  - [ ] 10.6 TemplateSelectionScreen と TemplateManagementScreen の実装
    - Custom Template 一覧表示・複数選択
    - 選択したテンプレートから新規 Checklist 作成
    - Default Template 編集画面（項目の追加・削除をリスト形式で）
    - Custom Template 一覧表示・名前変更・削除
    - _Requirements: 5.2, 5.5, 5.6, 5.7, 5.8_
  - [ ] 10.7 CustomTemplateEditScreen の実装
    - Custom Template の項目編集画面（Default Template編集と同じリスト形式UI）
    - 項目の追加（下部入力バー）・削除（✕ボタン）
    - テンプレート管理画面から画面遷移で表示
    - _Requirements: 5.8_
  - [ ] 10.8 BagManagementScreen の実装
    - Bag 一覧表示
    - 新規登録（名前入力、空名前エラー表示）
    - 名前変更（タップで編集）、削除
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5_
  - [ ] 10.9 FamilyGroupScreen の実装
    - Family Group 作成ボタン → 招待コード表示
    - 招待コードのワンタップコピー機能（クリップボードにコピー、コピー完了フィードバック表示）
    - 招待コード入力フィールド → 参加
    - グループ退出ボタン
    - _Requirements: 10.1, 10.2, 10.5_

- [ ] 11. Navigation とワイヤリング
  - [ ] 11.1 Jetpack Compose Navigation の設定
    - NavHost と各画面のルート定義
    - 画面遷移図に基づくナビゲーショングラフ作成
    - 引数の受け渡し（checklistId 等）
    - _Requirements: 1.1, 6.3_
  - [ ] 11.2 Hilt DI モジュールの作成
    - DatabaseModule: Room Database, DAO の提供
    - FirebaseModule: Firestore, Auth の提供
    - RepositoryModule: 各 Repository の提供
    - _Requirements: 7.1, 7.3_
  - [ ] 11.3 MainActivity と Application クラスの統合
    - `@AndroidEntryPoint` アノテーション
    - Compose テーマ設定（Material 3）
    - 起動時にチェックリスト一覧画面を表示
    - _Requirements: 6.1_

- [ ] 12. Checkpoint - UI Layer 完了確認
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 13. リアルタイム同期とオフライン対応の統合
  - [ ] 13.1 リアルタイムリスナーの統合
    - 共有 Checklist 詳細画面表示中の Firestore リアルタイムリスナー起動
    - items サブコレクションの変更検知と Room DB へのマージ
    - _Requirements: 11.2_
  - [ ] 13.2 オフライン対応の統合
    - ネットワーク状態監視（ConnectivityManager）
    - オフライン時のローカル変更保存（PendingSyncEntity）
    - ネットワーク復旧時の未同期変更送信
    - _Requirements: 11.3, 11.4_
  - [ ] 13.3 オフライン同期のプロパティテスト
    - **Property 22: オフライン変更の同期**
    - **Validates: Requirements 11.3, 11.4**
  - [ ] 13.4 Checklist削除カスケードのプロパティテスト
    - **Property 10: Checklist削除のカスケード**
    - **Validates: Requirements 4.4**

- [ ] 14. Final Checkpoint - 全体統合確認
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Tasks WITHOUT `*` are required (including core property tests: Property 1〜3, 6〜7, 10, 21, 22 and DAO unit tests)
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- Clean Architecture の依存方向: UI → Domain → Data を厳守
- バックグラウンドスレッドでのデータ保存は Kotlin Coroutines (Dispatchers.IO) で実現

## Effort Estimates

| Phase | Tasks | Estimate |
|---|---|---|
| 1. プロジェクト基盤 | 1.1〜1.2 | 1日 |
| 2. Room Database | 2.1〜2.4 | 3日 |
| 3. Firebase DataSource | 3.1〜3.3 | 2日 |
| 4. Repository | 4.1〜4.6 | 4日 |
| 5. Checkpoint | - | 0.5日 |
| 6. UseCase + コアテスト | 6.1〜6.17 | 7日 |
| 7. Checkpoint | - | 0.5日 |
| 8. ViewModel + テスト | 8.1〜8.9 | 5日 |
| 9. Checkpoint | - | 0.5日 |
| 10. Compose Screen | 10.1〜10.9 | 7日 |
| 11. Navigation/DI | 11.1〜11.3 | 2日 |
| 12. Checkpoint | - | 0.5日 |
| 13. 同期統合 + テスト | 13.1〜13.4 | 5日 |
| 14. Final Checkpoint | - | 0.5日 |
| バッファ（20%） | - | 5日 |
| **合計** | | **約43日（8.5週間、1名体制）** |
