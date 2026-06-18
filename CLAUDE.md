# Attendance Demo Project

勤怠管理デモアプリ。モノレポ構成。

- `packages/backend/` — Spring Boot 3.x (Java 21)
- `packages/frontend/` — Next.js (TypeScript)
- `packages/infra/` — AWS CDK (TypeScript)。dev/prod の2環境
- `docs/path/` — デモの過程ドキュメント（番号付きファイル）
- `docs/working/` — 要件定義・設計の作業用ドキュメント（Q&A 形式）

## 全般

- backend の Gradle コマンドは `packages/backend/` ディレクトリから実行する
- ルートの `package.json` に各パッケージのコマンドをまとめてある（`npm run boot`, `npm run dev` 等）

## docs/path ルール

デモの過程を `docs/path/` 以下に番号付きファイルで記録する。

- ファイル名: `00-xxx.md`, `01-xxx.md`, ... の連番
- 明確に次のステップに進んだと判断したら新しいファイルを作る
- 各ファイルに含める内容:
  - **プロンプト**: そのステップで実際に出された指示（引用形式で原文を残す）
  - **選択肢への回答**: AI からの質問に対するユーザーの選択
  - **やったこと**: 手順・コマンド・設定変更の詳細
  - **つまずき**: エラーや問題が起きた場合はその原因と解決方法
  - **最終構成**: ディレクトリ構造やファイル一覧（変更があった場合）

## Backend — Spring Boot

Spring Boot 3.x で開発しつつ、Spring Boot 4.x への移行が最小限で済む実装を徹底する。

## Spring Boot 4.x 互換ルール

### Security

- `WebSecurityConfigurerAdapter` は使用禁止。`SecurityFilterChain` Bean 方式のみ使う
- `antMatchers()` は使用禁止。`requestMatchers()` を使う
- `authorizeRequests()` は使用禁止。`authorizeHttpRequests()` を使う

### テスト

- `@MockBean` は使用禁止。`@MockitoBean` を使う
- `@SpyBean` は使用禁止。`@MockitoSpyBean` を使う
- `@SpringBootTest` で MockMvc を使う場合は `@AutoConfigureMockMvc` を明示的に付与する

### Jackson

- カスタム Serializer/Deserializer を書く場合、Jackson 3 への移行を意識する（極力カスタムを避ける）

### Nullable

- `org.springframework.lang.Nullable` は使用禁止。`org.jspecify.annotations.Nullable` を使う

## Java モダン化ルール

### DTO には record を使う

- DTO, リクエスト/レスポンスクラスは `record` で定義する（Lombok `@Data` は使わない）
- JPA Entity は mutable 必須のため `record` 不可。Lombok `@Data` / `@Builder` を使ってよい

### Lombok の使用範囲

- DTO: 使わない（record で代替）
- Entity: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor` は使ってよい
- `@Slf4j`: どこでも使ってよい

### MapStruct

- 使わない。record のコンストラクタで手動マッピングする

## アーキテクチャ

- ドメイン分割レイヤードを予定（ドメイン分けは要件定義・設計で決める）
- Service は interface + impl パターン
- `@Version` による楽観ロック
- `ddl-auto` は禁止。Flyway でマイグレーション管理
