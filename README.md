# 持ち物チェックリスト (Packing Checklist)

旅行や外出時の持ち物をリスト化し、チェック操作・鞄割り当て・テンプレート管理・家族間共有ができるAndroidアプリ。

## 技術スタック

- Kotlin + Jetpack Compose (Material 3)
- Room Database (ローカル永続化)
- Firebase Firestore (リアルタイム同期)
- Firebase Anonymous Authentication
- Hilt (DI)
- Kotlin Coroutines + Flow
- MVVM + Repository + Clean Architecture

## セットアップ手順

### 1. Firebase設定

1. [Firebase Console](https://console.firebase.google.com/) でプロジェクトを作成
2. 「Androidアプリを追加」→ パッケージ名に `com.mochimono.packingchecklist` を入力
3. `google-services.json` をダウンロードし、`app/` ディレクトリに配置
4. Firebase Consoleで以下を有効化:
   - **Authentication** → Sign-in method → 「匿名」を有効化
   - **Firestore Database** → 「データベースを作成」→ テストモードで開始

### 2. ビルド＆実行

```bash
# Android Studioで開く
open -a "Android Studio" .
```

- Gradle Syncが完了するまで待つ（初回は数分）
- エミュレータまたは実機を選択
- ▶ Run で実行

### 3. Firestoreセキュリティルール適用（家族共有機能を使う場合）

```bash
npm install -g firebase-tools
firebase login
firebase init firestore   # firestore.rules を選択
firebase deploy --only firestore:rules
```

## テスト実行

```bash
# ユニットテスト（Kotest Property Testing）
./gradlew test

# 統合テスト（Room DAO、エミュレータ必要）
./gradlew connectedAndroidTest
```

## プロジェクト構成

```
app/src/main/java/com/mochimono/packingchecklist/
├── data/
│   ├── local/entity/    Room Entity (8個)
│   ├── local/dao/       Room DAO (8個)
│   ├── local/db/        Database + 初期データ
│   ├── remote/          AuthManager, FirestoreDataSource, NetworkMonitor
│   └── repository/      Repository (6個) + SyncManager
├── domain/
│   ├── model/           Domain Models
│   └── usecase/         UseCase (9個)
├── ui/
│   ├── screen/          Compose Screen (10個)
│   ├── viewmodel/       ViewModel (6個)
│   ├── component/       BottomNavBar
│   ├── navigation/      Screen routes + NavHost
│   └── theme/           Material 3 Theme
├── di/                  Hilt DI Modules (3個)
├── PackingChecklistApp.kt
└── MainActivity.kt
```

## ドキュメント

- [要件定義書](.kiro/specs/packing-checklist/requirements.md)
- [設計書](.kiro/specs/packing-checklist/design.md)
- [実装計画](.kiro/specs/packing-checklist/tasks.md)
- [レビュー結果](.kiro/specs/packing-checklist/expert-reviews.md)
- [HTMLモック](mock/index.html) — ブラウザで開くとUIモックを確認可能
