---
description: AI 駆動 + TDD の開発プロセス。新機能の実装やタスクの進め方に関わるときに適用する。
globs: packages/**/*.{java,ts,tsx}
---

# 開発プロセス

AI-DLC (AWS) のフローをベースに、TDD を組み合わせた開発プロセス。

## フロー

```
1. 要件定義（Inception）
2. 基本設計（AI と壁打ち）
3. Unit of Work 分割
4. 各 Unit を Plan → 承認 → TDD で実装（Construction）
```

## 1. 要件定義（Inception）

- ユーザーとの対話で機能要件を決める
- ユーザーストーリーとして記述する
- 確定した要件は `docs/requirements/` に記録する

### 作業ドキュメント（`docs/working/`）

要件定義や設計の過程で使う作業用ドキュメントは `docs/working/` 以下に配置する:

- `docs/working/requirements/` — 要件定義の Q&A ドキュメント
- `docs/working/design/` — 設計検討の作業ドキュメント

作業ドキュメントでは `[Question]` / `[Answer]` タグを使い、ユーザーが `[Answer]` を埋めながら仕様を決めていく:

```
[Question] 社員IDは自動採番ですか？それとも手動入力？
[Answer]   （ユーザーが回答を記入）
```

- AI は未回答の `[Answer]` がある状態で、その仕様に依存する実装や設計に進んではいけない
- 全ての `[Answer]` が埋まったら、確定した仕様を `docs/requirements/` や `docs/design/` に整理する

## 2. 基本設計（AI と壁打ち）

以下のドキュメントを AI との対話で作成し、`docs/design/` に配置する:

- **ドメイン分析**: Entity, Value Object, 関連図（ライト DDD）
- **API 設計**: OpenAPI (YAML) で定義。Swagger UI で確認可能な形式
- **DB 設計**: テーブル定義 + ER 図。Flyway マイグレーションとして実装
- **画面設計**: 必要に応じてワイヤーフレームやコンポーネント一覧

### ドメインモデリング（ライト DDD）

| 使う | 使わない（必要になったら導入） |
|------|---------------------------|
| Entity — DB テーブル対応。ビジネスルールも持つ | Aggregate Root — Entity がそのまま担う |
| Value Object — 自然なもの（勤務時間、時刻等） | Domain Event — 必要になったら |
| Repository — interface で定義 | 仕様パターン |
| Service — ドメインロジックの調整役 | |

### [Question]/[Answer] タグ

AI が仮定してはいけない仕様上の判断は、明示的に質問する:

```
[Question] 社員IDは自動採番ですか？それとも手動入力？
[Answer]   自動採番（UUID）
```

AI はこのタグが未回答のまま実装に進んではいけない。

## 3. Unit of Work 分割

基本設計をもとに、**独立して実装可能な Unit of Work** に分解する:

- DDD の Bounded Context に相当する凝集度の高い単位で切る
- 各 Unit 内のユーザーストーリー・API・テーブルを明示する
- Unit 間の依存関係を明示する（依存がない Unit は並列実装可能）
- `docs/units/` に Unit ごとのファイルを配置する（`unit_*.md`）

## 4. 各 Unit の実装（Construction）

### Phase 1: Plan（AI が提案 → 人間が承認）

AI が実装計画を提示する:
- 作成するファイル一覧（パス + 役割）
- テストケース一覧
- 実装順序

**人間が承認してから Phase 2 に進む。**

### Phase 2: Implement（TDD）

承認された計画に沿って TDD で実装する:

```
Red:     テストを書く（仕様をテストとして記述。この時点では失敗する）
Green:   テストが通る最小限の実装を書く
Refactor: コードを整理する（テストは通ったまま）
```

## TDD の詳細

### Backend の実装順序

1. **Flyway マイグレーション** — テーブル定義を書く
2. **Entity** — テーブルに対応するクラスを書く
3. **Value Object** — 自然な値オブジェクトがあれば作る
4. **Repository テスト** → Repository 実装 (interface)
5. **Service テスト** → Service インターフェース → Service 実装
6. **Controller テスト** → Controller 実装
7. **統合テスト** — API エンドポイントを通しで確認

各ステップで必ずテストを先に書く。

### Frontend の実装順序

1. **API クライアントの型定義** — OpenAPI から型を生成 or 手書き
2. **コンポーネントテスト** → コンポーネント実装
3. **API 連携テスト** → API 呼び出し実装
4. **E2E テスト** — ユーザーフローの確認

### テストが仕様書になる

SIer の詳細設計書は「入力 X に対して出力 Y を返す」を記述する。
TDD ではそれをテストコードとして記述する:

```java
@Test
@DisplayName("出勤打刻: 当日未打刻の社員が打刻すると出勤時刻が記録される")
void clockIn_notYetClockedIn_recordsClockInTime() {
    // これが「詳細設計」に相当する
}
```

テストが通れば仕様を満たしていることが証明される。Excel の設計書と違い、実装との乖離が起きない。

## AI への実装依頼のしかた

- Phase 1 で計画を出してもらい、承認する
- Phase 2 でテストを書いた後、「このテストが通る実装を書いて」と依頼する
- AI が生成したコードを人間がレビューする
- テストが通らなければ AI に修正を依頼する
- テストが通ったらリファクタリングを依頼する
- **AI が仕様を仮定しそうな場面では [Question] タグで確認を求める**
