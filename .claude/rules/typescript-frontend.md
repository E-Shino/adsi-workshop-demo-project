---
description: TypeScript / Next.js のコーディング規約。frontend の TS/TSX ファイルを編集するときに適用する。
globs: packages/frontend/**/*.{ts,tsx}
---

# TypeScript / Next.js コーディング規約

## 型安全

- `any` 禁止。信頼できない入力には `unknown` を使い、型ガードで絞り込む
- 公開 API（関数の引数・戻り値）には明示的に型をつける
- オブジェクトの形状は `interface`、ユニオンやユーティリティ型は `type` で定義する
- API レスポンスの型は Zod 等でランタイムバリデーションする

## イミュータビリティ

- オブジェクトを直接変更しない。スプレッド構文で新しいオブジェクトを作る
- 配列操作は `map`, `filter`, `reduce` を使う。`push`, `splice` 等の破壊的操作を避ける
- `const` をデフォルトにする。`let` は再代入が必要な場合のみ

## コンポーネント設計

- ファイルはフィーチャー（機能）ベースで整理する。ファイル種別ベース（components/, hooks/ 等に全部入れる）にしない
- コンポーネントの Props 型は `interface` で定義する
- サーバーコンポーネントをデフォルトにし、インタラクションが必要な部分だけ `"use client"` にする
- 1ファイル 300行以内。超えたらコンポーネントを分割する

## 状態管理

- サーバーステート（API データ）はフェッチライブラリ (SWR / TanStack Query) で管理する。ローカルの state にコピーしない
- クライアントステートは最小限にする。URL パラメータで表現できるものは URL に持たせる

## API 通信

- fetch のラッパーを用意して、ベース URL・エラーハンドリング・型変換を統一する
- API パスは `/api/...` にする（Next.js rewrites でバックエンドに転送される）
- エラーレスポンスはユーザーに適切なメッセージとして表示する（生のエラーメッセージを出さない）

## スタイリング

- Tailwind CSS を使う
- デザイントークン（色・フォント・spacing 等）は CSS カスタムプロパティで定義する
- アニメーションは `transform`, `opacity` のみ使う（レイアウトプロパティを避ける）

## 命名規則

- コンポーネント: PascalCase (`EmployeeList`)
- hooks: `use` プレフィックス (`useEmployees`)
- 変数・関数: camelCase (`fetchEmployees`)
- 定数: UPPER_SNAKE_CASE (`API_BASE_URL`)
- ファイル名: コンポーネントは PascalCase、それ以外は kebab-case
