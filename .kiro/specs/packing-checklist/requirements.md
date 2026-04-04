# Requirements Document

## Introduction

本ドキュメントは、Androidアプリにおける「持ち物チェックリスト」機能の要件を定義する。ユーザーが旅行や外出の際に必要な持ち物をリスト化し、チェックを付けて忘れ物を防止するための機能である。チェックリストの作成・編集・チェック操作・テンプレート管理（デフォルトテンプレート・個別テンプレート）・鞄管理・家族間共有を主要な機能として提供する。

## Glossary

- **Checklist_App**: 持ち物チェックリスト機能を提供するAndroidアプリケーション
- **Checklist**: ユーザーが作成する持ち物のリスト。タイトルと複数のChecklist_Itemで構成される
- **Checklist_Item**: Checklist内の個々の持ち物項目。名前、チェック状態、割り当てられたBagの情報を持つ
- **Template**: 再利用可能なChecklistの雛形。Default_TemplateとCustom_Templateの2種類が存在する
- **Default_Template**: アプリに予め搭載されている、旅行に必ず持って行く一般的な持ち物セットのテンプレート。Userが項目の追加・削除・変更を行い編集可能であり、新規Checklist作成時に自動的に適用される
- **Custom_Template**: ユーザーが特定の目的地や用途（例: USJ、スキー旅行）に合わせて自由に作成・管理できるテンプレート
- **Bag**: ユーザーが登録する鞄の情報。名前、種類、色などの属性を持つ
- **Bag_Registry**: ユーザーが登録した全てのBag情報を管理する保存領域
- **Family_Group**: Checklist、Bag_RegistryのBag情報、Custom_Templateを家族間で共有するためのグループ。複数のUserで構成される
- **Sharing_Setting**: Checklistごとに設定可能な共有/非共有の切り替え設定。共有が有効なChecklistのみがFamily_Groupのメンバーに公開される
- **Local_Database**: 端末内にデータを永続化するためのローカルストレージ（Room Database）
- **Remote_Database**: Family_Group間でデータを同期するためのクラウドストレージ
- **User**: 本アプリを利用するAndroidデバイスの操作者

## Requirements

### Requirement 1: チェックリストの作成

**User Story:** As a User, I want 新しいチェックリストを作成したい, so that 旅行や外出の目的に合わせた持ち物リストを準備できる

#### Acceptance Criteria

1. WHEN User が新規作成ボタンをタップした時, THE Checklist_App SHALL チェックリスト作成画面を表示する
2. WHEN User がタイトルを入力し保存ボタンをタップした時, THE Checklist_App SHALL 入力されたタイトルで新しいChecklistをLocal_Databaseに保存する
3. IF User がタイトルを空のまま保存ボタンをタップした場合, THEN THE Checklist_App SHALL 「タイトルを入力してください」というエラーメッセージを表示する
4. WHEN 新しいChecklistが作成された時, THE Checklist_App SHALL Default_Templateに登録されている全項目を未チェック状態のChecklist_Itemとして自動的にChecklistに追加する
5. THE Checklist_App SHALL 作成されたChecklistをチェックリスト一覧画面に表示する

### Requirement 2: チェックリストへの持ち物追加

**User Story:** As a User, I want チェックリストに持ち物を追加したい, so that 必要な持ち物を漏れなく管理できる

#### Acceptance Criteria

1. WHEN User がチェックリスト詳細画面で追加ボタンをタップした時, THE Checklist_App SHALL 持ち物名の入力欄を表示する
2. WHEN User が持ち物名を入力し確定した時, THE Checklist_App SHALL 新しいChecklist_ItemをChecklistに追加しLocal_Databaseに保存する
3. THE Checklist_App SHALL 追加されたChecklist_Itemを未チェック状態で表示する
4. IF User が持ち物名を空のまま確定した場合, THEN THE Checklist_App SHALL 項目を追加せず入力欄をクリアする

### Requirement 3: 持ち物のチェック操作

**User Story:** As a User, I want 持ち物にチェックを付ける際にどの鞄に入れたかを選択したい, so that 準備済みの持ち物と未準備の持ち物を区別しつつ、どの鞄に入れたかも同時に記録できる

#### Acceptance Criteria

1. WHEN User がChecklist_Itemのチェックボックスをタップした時, THE Checklist_App SHALL Bag_Registryに登録済みのBag一覧を選択肢として表示し、Bagの選択またはスキップを促す。その際、現在のChecklist内で既にChecklist_Itemが割り当てられているBagを一覧の上位に優先表示する
2. WHEN Bag選択画面が表示された時, THE Checklist_App SHALL 直前にChecklist_Itemを割り当てたBagを一覧の最上位に表示し、続いて現在のChecklist内で他のChecklist_Itemが割り当てられているBagを割り当て数の多い順に表示し、残りのBagをその後に表示する
3. WHEN User がBag一覧からBagを選択した時, THE Checklist_App SHALL 該当Checklist_Itemのチェック状態をチェック済みに変更し、選択されたBagを割り当ててLocal_Databaseに保存する
4. WHEN User がBag選択をスキップした時, THE Checklist_App SHALL 該当Checklist_Itemのチェック状態をチェック済みに変更し、Bag未割り当てのままLocal_Databaseに保存する
5. IF Bag_Registryに登録済みのBagが存在しない場合, THEN THE Checklist_App SHALL Bag選択画面を表示せず、該当Checklist_Itemのチェック状態をチェック済みに切り替えLocal_Databaseに保存する
6. WHEN User がチェック済みのChecklist_Itemのチェックを外した時, THE Checklist_App SHALL 該当Checklist_Itemのチェック状態を未チェックに変更し、割り当てられていたBagの割り当てを解除してLocal_Databaseに保存する
7. WHILE Checklist_Itemがチェック済み状態の間, THE Checklist_App SHALL 該当項目に取り消し線を表示する
8. THE Checklist_App SHALL チェックリスト詳細画面にチェック済み項目数と全項目数を「3/10」の形式で表示する

### Requirement 4: チェックリストの編集と削除

**User Story:** As a User, I want チェックリストや持ち物を編集・削除したい, so that リストの内容を最新の状態に保てる

#### Acceptance Criteria

1. WHEN User がChecklistのタイトルを長押しした時, THE Checklist_App SHALL タイトル編集ダイアログを表示する
2. WHEN User がChecklist_Itemを左スワイプした時, THE Checklist_App SHALL 削除確認なしで該当Checklist_ItemをChecklistから削除しLocal_Databaseを更新する
3. WHEN User がチェックリスト一覧画面でChecklistを左スワイプした時, THE Checklist_App SHALL 削除確認ダイアログを表示する
4. WHEN User が削除確認ダイアログで「削除」を選択した時, THE Checklist_App SHALL 該当Checklistと関連する全Checklist_ItemをLocal_Databaseから削除する
5. WHEN User がChecklist_Itemの名前をタップした時, THE Checklist_App SHALL 項目名の編集モードに切り替える

### Requirement 5: テンプレート機能

**User Story:** As a User, I want 旅行に必ず持って行く持ち物はデフォルトテンプレートとして利用し、特定の目的地向けの持ち物セットは個別テンプレートとして自由に管理したい, so that 毎回同じ持ち物を入力する手間を省き、目的地に応じた持ち物も効率的に準備できる

#### Acceptance Criteria

1. THE Checklist_App SHALL 旅行に一般的に必要な持ち物セット（例: パスポート、充電器、着替え、歯ブラシ、常備薬）をDefault_Templateとしてアプリに搭載する
2. THE Checklist_App SHALL Default_Templateの項目に対してUserが追加・削除・変更の編集操作を行える機能を提供する
3. WHEN User が既存のChecklistからテンプレート保存を選択した時, THE Checklist_App SHALL 該当Checklist内の全Checklist_Itemを選択可能な一覧として表示し、Userが選択したChecklist_ItemのみをCustom_TemplateとしてLocal_Databaseに保存する
4. THE Checklist_App SHALL Custom_Templateに目的地や用途を示すラベル（例: 「USJ」「スキー旅行」）を設定できる機能を提供する
5. WHEN User がテンプレートから新規チェックリスト作成を選択した時, THE Checklist_App SHALL Custom_Templateの一覧を表示する
6. WHEN User がCustom_Templateを選択した時, THE Checklist_App SHALL Default_Templateの全項目とCustom_Template内の全項目を統合し重複を除去した未チェック状態の新しいChecklistを作成する
7. WHEN User が複数のCustom_Templateを選択した時, THE Checklist_App SHALL Default_Templateの全項目と選択された全Custom_Templateの項目を統合し重複を除去した新しいChecklistを作成する
8. THE Checklist_App SHALL Custom_Templateの削除・名前変更・項目編集機能を提供する

### Requirement 6: チェックリスト一覧表示

**User Story:** As a User, I want 作成したチェックリストを一覧で確認したい, so that 目的のチェックリストにすぐアクセスできる

#### Acceptance Criteria

1. THE Checklist_App SHALL 起動時にチェックリスト一覧画面を表示する
2. THE Checklist_App SHALL 各Checklistのタイトル、チェック進捗（例: 3/10）、作成日を一覧に表示する
3. WHEN User がChecklistをタップした時, THE Checklist_App SHALL 該当Checklistの詳細画面に遷移する
4. THE Checklist_App SHALL チェックリスト一覧を作成日の降順（新しい順）で表示する

### Requirement 7: データの永続化

**User Story:** As a User, I want アプリを閉じてもデータが保持されてほしい, so that いつでもチェックリストの状態を確認・再開できる

#### Acceptance Criteria

1. THE Checklist_App SHALL 全てのChecklist、Checklist_Item、Template、Bag、Bag_RegistryのデータをLocal_Databaseに永続化する
2. WHEN Checklist_App が再起動された時, THE Checklist_App SHALL Local_Databaseから全データを復元し前回の状態を表示する
3. THE Checklist_App SHALL データの保存処理をUIスレッドとは別のバックグラウンドスレッドで実行する
4. WHILE User がFamily_Groupに参加している間, THE Checklist_App SHALL 共有対象のChecklistデータ、Bag_RegistryのBag情報、およびCustom_TemplateをRemote_Databaseにも永続化する

### Requirement 8: 鞄管理

**User Story:** As a User, I want 持ち物をどの鞄に入れたかを管理したい, so that チェックリストを見るだけでどの持ち物がどの鞄に入っているかを把握できる

#### Acceptance Criteria

1. THE Checklist_App SHALL チェックリスト詳細画面で各Checklist_Itemに割り当てられたBagの名前を表示する
2. THE Checklist_App SHALL チェックリスト詳細画面でBag別にChecklist_Itemをグループ化して表示する機能を提供する
3. THE Checklist_App SHALL Bagが未割り当てのChecklist_Itemを「未分類」グループとして表示する

> **注記**: Checklist_ItemへのBag割り当て操作はRequirement 3（持ち物のチェック操作）に統合されている。チェックを入れる際にBagを選択し、チェックを外す際にBag割り当てが解除される。

### Requirement 9: 鞄情報の登録と再利用

**User Story:** As a User, I want 鞄の情報を登録して新しいチェックリストでも再利用したい, so that 毎回同じ鞄情報を入力する手間を省ける

#### Acceptance Criteria

1. WHEN User が鞄管理画面で新規登録ボタンをタップした時, THE Checklist_App SHALL 鞄の名前を入力できる登録画面を表示する
2. WHEN User が鞄の名前を入力し保存した時, THE Checklist_App SHALL 新しいBagをBag_Registryに登録しLocal_Databaseに保存する
3. IF User が鞄の名前を空のまま保存した場合, THEN THE Checklist_App SHALL 「鞄の名前を入力してください」というエラーメッセージを表示する
4. THE Checklist_App SHALL Bag_Registryに登録された全てのBagを鞄管理画面に一覧表示する
5. THE Checklist_App SHALL Bag_Registryに登録されたBagの名前変更・削除機能を提供する
6. WHEN User がBag_RegistryからBagを削除した時, THE Checklist_App SHALL 該当Bagが割り当てられている全Checklist_ItemのBag割り当てを解除しLocal_Databaseを更新する
7. WHEN User が新しいChecklistを作成した時, THE Checklist_App SHALL Bag_Registryに登録済みのBagをChecklist_Itemへの割り当て候補として利用可能にする

### Requirement 10: 家族共有

**User Story:** As a User, I want チェックリスト・鞄情報・カスタムテンプレートを家族と共有したい, so that 家族全員で持ち物の準備状況を確認し分担でき、鞄やテンプレートの情報も共有できる

#### Acceptance Criteria

1. WHEN User がFamily_Groupの作成を選択した時, THE Checklist_App SHALL 招待コードを生成しFamily_GroupをRemote_Databaseに作成する
2. THE Checklist_App SHALL 生成された招待コードを画面に表示し、Userがワンタップでクリップボードにコピーできる機能を提供する
3. WHEN User が招待コードを入力してFamily_Groupに参加した時, THE Checklist_App SHALL 該当UserをFamily_Groupのメンバーとして登録する
3. WHEN User がChecklistの共有設定を有効にした時, THE Checklist_App SHALL 該当ChecklistをFamily_Groupの全メンバーが閲覧・編集可能な状態にする
4. THE Checklist_App SHALL 共有されたChecklistの一覧画面に共有アイコンを表示する
5. WHEN User がFamily_Groupから退出を選択した時, THE Checklist_App SHALL 該当UserをFamily_Groupのメンバーから除外する
6. THE Checklist_App SHALL 招待コードを推測困難な十分な長さ（UUID v4ベース等）で生成し、招待コードの複雑性によりセキュリティを担保する
7. WHEN User がFamily_Groupに参加した時, THE Checklist_App SHALL 該当UserのBag_Registryに登録されたBag情報をFamily_Groupの全メンバーが閲覧可能な状態でRemote_Databaseに同期する
8. WHEN User がFamily_Groupに参加した時, THE Checklist_App SHALL 該当UserのCustom_TemplateをFamily_Groupの全メンバーが閲覧・利用可能な状態でRemote_Databaseに同期する
9. THE Checklist_App SHALL 新規作成されたChecklistのSharing_Settingを非共有（プライベート）に初期設定する（Should要件）
10. THE Checklist_App SHALL Checklist詳細画面またはチェックリスト一覧画面からChecklistごとにSharing_Settingの共有/非共有を切り替える機能を提供する（Should要件）
11. WHILE ChecklistのSharing_Settingが非共有に設定されている間, THE Checklist_App SHALL 該当ChecklistをFamily_Groupのメンバーに公開せずLocal_Databaseのみに保存する（Should要件）

### Requirement 11: チェック状態のリアルタイム同期

**User Story:** As a User, I want 家族がチェックを入れたら自分の画面にもリアルタイムで反映されてほしい, so that 家族間で準備状況を即座に共有できる

#### Acceptance Criteria

1. WHEN Family_Groupのメンバーが共有ChecklistのChecklist_Itemのチェック状態を変更した時, THE Checklist_App SHALL 変更内容をRemote_Databaseに送信する
2. WHILE User が共有Checklistの詳細画面を表示している間, THE Checklist_App SHALL Remote_Databaseからの変更通知を受信し画面に反映する
3. IF ネットワーク接続が切断された場合, THEN THE Checklist_App SHALL ローカルでの変更操作を許可しLocal_Databaseに保存する
4. WHEN ネットワーク接続が復旧した時, THE Checklist_App SHALL ローカルに保存された未同期の変更をRemote_Databaseに送信し同期する
5. IF 同一のChecklist_Itemに対して複数のUserが同時に変更を行った場合, THEN THE Checklist_App SHALL 最新のタイムスタンプを持つ変更を優先して適用する
