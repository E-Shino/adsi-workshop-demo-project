---
description: Java / Spring Boot のコーディング規約。backend の Java ファイルを編集するときに適用する。
globs: packages/backend/**/*.java
---

# Java / Spring Boot コーディング規約

## イミュータビリティ

- フィールドは `final` をデフォルトにする
- コレクションを返すときは防御コピーする: `List.copyOf()`, `Map.copyOf()`, `Set.copyOf()`
- DTO は `record` で定義する（イミュータブルが保証される）
- Entity のコレクションフィールドは `Collections.unmodifiableList()` 等で返す

## DI（依存性注入）

- コンストラクタインジェクションのみ使う。`@Autowired` のフィールドインジェクション禁止
- コンストラクタが1つならば `@Autowired` アノテーション自体も不要

```java
// OK
@Service
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository repository;

    public EmployeeServiceImpl(EmployeeRepository repository) {
        this.repository = repository;
    }
}

// NG
@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeRepository repository;
}
```

## 命名規則

- クラス名: PascalCase (`EmployeeService`)
- メソッド名: camelCase (`findById`)
- 定数: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)
- パッケージ: すべて小文字 (`com.example.attendance`)
- テストメソッド: `メソッド名_シナリオ_期待結果` (`findById_existingId_returnsEmployee`)

## メソッド・クラスのサイズ

- 1メソッド 30行以内
- 1クラス 500行以内
- パラメータ 5個以内（超える場合は record でまとめる）

## Spring Boot 固有

- `@RestController` で REST API を定義する
- バリデーションは Bean Validation (`@NotNull`, `@Size` 等) で行う
- 例外ハンドリングは `@RestControllerAdvice` に集約する
- レスポンスには適切な HTTP ステータスコードを返す
- `Optional` を返すメソッドのチェーンでは `orElseThrow()` で明示的に例外を投げる

## JPA / DB

- Entity と DTO は必ず分離する（Entity を API レスポンスに直接使わない）
- N+1 問題を意識する。必要に応じて `@EntityGraph` や `JOIN FETCH` を使う
- `@Transactional` は Service 層に付与する（Controller には付けない）
- 楽観ロック (`@Version`) を標準で使う

## ログ

- `@Slf4j` (Lombok) でロガーを取得する
- ログレベルの使い分け: ERROR=障害, WARN=想定外だが継続可能, INFO=業務イベント, DEBUG=開発用
- 例外ログには必ずスタックトレースを含める: `log.error("message", exception)`
- 個人情報・パスワード等の機密情報をログに出力しない
