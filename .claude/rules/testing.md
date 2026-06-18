---
description: テストのルール。テストコードを書く・修正するときに適用する。
globs: packages/**/*{Test,test,spec}*.{java,ts,tsx}
---

# テストルール

## 基本方針

- テストカバレッジ 80% 以上を目標とする
- テストは AAA パターンで書く: Arrange（準備）→ Act（実行）→ Assert（検証）
- テスト名は「何を」「どういう条件で」「どうなるか」を表現する

## Backend (Java / JUnit 5)

### テストの種類と使い分け

| アノテーション | 用途 | 速度 |
|---|---|---|
| `@Test` (JUnit のみ) | ロジックのユニットテスト | 高速 |
| `@WebMvcTest` | Controller 層のテスト（HTTP リクエスト/レスポンス） | 中速 |
| `@DataJpaTest` | Repository 層のテスト（DB アクセス） | 中速 |
| `@SpringBootTest` | 統合テスト（アプリ全体） | 低速 |

### ルール

- テストクラスには `@ActiveProfiles("test")` を付与する
- モックは `@MockitoBean` を使う（`@MockBean` は Spring Boot 4 で削除済み）
- `@SpringBootTest` で MockMvc を使う場合は `@AutoConfigureMockMvc` を明示する
- ユニットテストでは Spring コンテキストを起動しない（コンストラクタで依存を渡す）
- 1テストメソッド 1アサーション（関連するアサーションのグループは許容）
- テストデータはテストメソッド内で作る。テスト間で状態を共有しない

### テストメソッドの命名

```java
@Test
@DisplayName("存在する社員IDで検索すると社員情報が返される")
void findById_existingId_returnsEmployee() {
    // Arrange
    var employee = new Employee("田中太郎", "tanaka@example.com");
    when(repository.findById(1L)).thenReturn(Optional.of(employee));

    // Act
    var result = service.findById(1L);

    // Assert
    assertThat(result.name()).isEqualTo("田中太郎");
}
```

### ArchUnit

- レイヤー違反を検出するテストを書く
  - Controller は Service のみ依存可能（Repository 直接アクセス禁止）
  - Service は Repository に依存可能（Controller には依存しない）
  - Entity は他のレイヤーに依存しない

## Frontend (TypeScript)

### テストの種類

| 種類 | ツール | 対象 |
|---|---|---|
| ユニットテスト | Vitest | ユーティリティ関数、hooks |
| コンポーネントテスト | Vitest + Testing Library | UI コンポーネント |
| E2E テスト | Playwright | ユーザーフロー全体 |

### ルール

- コンポーネントテストはユーザーの操作をシミュレートする（内部実装をテストしない）
- `getByRole`, `getByLabelText` を優先する（`getByTestId` は最終手段）
- API 呼び出しはモックする（MSW 推奨）
- スナップショットテストは避ける（壊れやすく、レビューしにくい）

## 共通

- テストが落ちたらすぐ直す。落ちたままにしない
- フレイキー（不安定な）テストは原因を特定して修正する。リトライで誤魔化さない
- テストコードも本番コードと同じ品質で書く（DRY、命名、可読性）
