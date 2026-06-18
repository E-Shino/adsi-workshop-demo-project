---
description: セキュリティルール。認証・認可・入力処理・シークレット管理に関わるコードを書くときに適用する。
globs: packages/**/*.{java,ts,tsx,yaml,yml}
---

# セキュリティルール

## シークレット管理

- パスワード・API キー・トークンをコードにハードコードしない
- 機密情報は環境変数 (`${DATABASE_PASSWORD}`, `process.env.API_KEY`) で外部化する
- 環境変数が未設定の場合は起動時にエラーで落とす（サイレントに空文字で動かさない）
- `.env` ファイルは `.gitignore` に含める

## 入力バリデーション

- ユーザー入力はシステム境界で必ずバリデーションする
  - Backend: Bean Validation (`@NotNull`, `@Size`, `@Pattern`)
  - Frontend: Zod 等でランタイムバリデーション
- バリデーションエラーは具体的なフィールド名とメッセージを返す

## SQL インジェクション防止

- SQL は必ずパラメータ化クエリを使う
- Spring Data JPA の `@Query` で JPQL を書く場合もパラメータバインディングを使う
- ユーザー入力を文字列結合で SQL に埋め込むことは絶対にしない

```java
// OK
@Query("SELECT e FROM Employee e WHERE e.name = :name")
List<Employee> findByName(@Param("name") String name);

// NG
@Query("SELECT e FROM Employee e WHERE e.name = '" + name + "'")
```

## XSS 防止

- React/Next.js ではデフォルトでエスケープされるが、`dangerouslySetInnerHTML` は使わない
- ユーザー入力を HTML として描画する必要がある場合はサニタイズライブラリを使う

## 認証・認可

- Spring Security の `SecurityFilterChain` Bean 方式で設定する
- パスワードは `BCryptPasswordEncoder` でハッシュ化する（平文保存禁止）
- API エンドポイントはデフォルト拒否。許可するパスを明示的にホワイトリストする
- CORS は許可するオリジンを明示的に指定する（`*` 禁止）

## エラーレスポンス

- 内部エラーの詳細（スタックトレース、SQL エラー等）をクライアントに返さない
- クライアントには汎用的なエラーメッセージを返し、詳細はサーバーログに記録する
- 認証エラーは「ユーザー名またはパスワードが正しくありません」のように、どちらが間違っているか特定できないメッセージにする

## 依存関係

- 既知の脆弱性がある依存を放置しない
- `npm audit` / Gradle の dependency check を定期的に実行する
