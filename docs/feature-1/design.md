# 打刻メモ入力機能 — 設計

確定仕様: [qa.md](./qa.md)

---

## 1. ドメインモデル

### Entity

- **AttendanceMemo** — 打刻メモ（新規）
  - 1つの `AttendanceRecord` に対して、出勤メモ・退勤メモの最大 2 つが紐づく
  - `memo_type` で出勤(`CLOCK_IN`) / 退勤(`CLOCK_OUT`) を区別する

### Value Object / Enum

- **MemoType** — `CLOCK_IN` / `CLOCK_OUT`
- **MemoCategory** — カテゴリ enum。出勤/退勤で使用可能なものが異なる

| カテゴリ値 | 表示名 | 出勤時 | 退勤時 |
|-----------|--------|:------:|:------:|
| DIRECT_GO | 直行 | o | - |
| TRAIN_DELAY | 電車遅延 | o | - |
| REMOTE | 在宅 | o | o |
| INCIDENT | 障害対応 | o | o |
| OTHER | その他 | o | o |
| DIRECT_RETURN | 直帰 | - | o |
| EARLY_LEAVE | 早退 | - | o |
| OUT_OF_OFFICE | 外出 | - | o |

### リレーション

```
AttendanceRecord 1 --- 0..2 AttendanceMemo
```

---

## 2. DB 設計

### attendance_memos テーブル（新規）

| カラム | 型 | 制約 | 説明 |
|--------|-----|------|------|
| id | uuid | PK | UUID v7 |
| attendance_record_id | uuid | FK → attendance_records(id), NOT NULL | 紐づく打刻レコード |
| memo_type | varchar(20) | NOT NULL, CHECK | CLOCK_IN / CLOCK_OUT |
| category | varchar(30) | NOT NULL | カテゴリ enum 値 |
| note | varchar(200) | | 自由テキスト（任意。「その他」時は必須） |
| version | bigint | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | timestamp with time zone | NOT NULL | 作成日時 |
| updated_at | timestamp with time zone | NOT NULL | 更新日時 |

```sql
CREATE TABLE attendance_memos (
    id UUID PRIMARY KEY,
    attendance_record_id UUID NOT NULL REFERENCES attendance_records(id),
    memo_type VARCHAR(20) NOT NULL CHECK (memo_type IN ('CLOCK_IN', 'CLOCK_OUT')),
    category VARCHAR(30) NOT NULL,
    note VARCHAR(200),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX idx_attendance_memos_record_type
    ON attendance_memos(attendance_record_id, memo_type);
```

- ユニークインデックスで「1レコードに同一タイプのメモは1つ」を保証
- category の CHECK 制約は enum 値が多いため、アプリ層でバリデーション

---

## 3. API 設計

### 既存 API の変更

#### POST /api/attendance/clock-in

Request body を追加（従来はボディなし）:

```json
{
  "memo": {
    "category": "DIRECT_GO",
    "note": "客先に直接訪問"
  }
}
```

- `memo` フィールドは任意（null / 省略 = メモなし打刻）
- `memo` がある場合、`category` は必須
- `category` が `OTHER` の場合、`note` は必須

Response に memo を追加:

```json
{
  "id": "...",
  "workDate": "2026-07-13",
  "clockIn": "2026-07-13T00:00:00Z",
  "clockOut": null,
  "corrected": false,
  "clockInMemo": {
    "id": "...",
    "category": "DIRECT_GO",
    "categoryLabel": "直行",
    "note": "客先に直接訪問"
  },
  "clockOutMemo": null
}
```

#### POST /api/attendance/clock-out

同様に Request body を追加:

```json
{
  "memo": {
    "category": "DIRECT_RETURN",
    "note": ""
  }
}
```

### 新規 API

#### PUT /api/attendance/{recordId}/memo/{memoType}

メモの編集（追加・更新）。

Path params:
- `recordId`: attendance_record の UUID
- `memoType`: `clock-in` / `clock-out`

Request:
```json
{
  "category": "REMOTE",
  "note": "午後から在宅に切替"
}
```

Response (200):
```json
{
  "id": "...",
  "category": "REMOTE",
  "categoryLabel": "在宅",
  "note": "午後から在宅に切替"
}
```

Error:
- 404: 打刻レコードが存在しない
- 400: カテゴリが memo_type に対して無効 / 「その他」で note 空
- 403: 他人のレコード（本人以外はアクセス不可）

#### DELETE /api/attendance/{recordId}/memo/{memoType}

メモの削除。

Response (204): No Content

Error:
- 404: メモが存在しない
- 403: 他人のレコード

### 既存レスポンスへの影響

`GET /api/attendance/today`, `GET /api/attendance/history` のレスポンスにも `clockInMemo`, `clockOutMemo` を含める（後方互換: null が入るだけ）。

---

## 4. 画面設計

### 4.1 打刻ダイアログ（新規）

打刻ボタン押下後に表示されるモーダル:

```
┌──────────────────────────────────┐
│        出勤打刻                    │
│                                  │
│  カテゴリ:                        │
│  ┌────────────────────────────┐  │
│  │ [直行] [電車遅延] [在宅]    │  │
│  │ [障害対応] [その他]         │  │
│  └────────────────────────────┘  │
│                                  │
│  メモ (任意):                     │
│  ┌────────────────────────────┐  │
│  │                            │  │
│  │                            │  │
│  └────────────────────────────┘  │
│                       0/200文字   │
│                                  │
│  [メモなしで打刻]  [出勤を確定]    │
└──────────────────────────────────┘
```

- 「メモなしで打刻」→ memo=null で打刻 API を呼ぶ
- 「出勤を確定」→ カテゴリ必須バリデーション → 打刻 API を memo 付きで呼ぶ
- 「その他」選択時に note が空なら確定ボタンを disabled にする

### 4.2 打刻確認表示（変更）

打刻後の TodayRecords / TodayStatus にメモ（カテゴリ + note）を表示:

```
出勤 09:00  [直行] 客先に直接訪問
退勤 18:00  [直帰]
```

### 4.3 勤怠履歴一覧（変更）

テーブルの各行にメモアイコン（メモあり時）を表示。行展開 or ホバーでメモ内容を確認可能。
編集ボタンからメモ編集ダイアログを開く。

### 4.4 メモ編集ダイアログ（新規）

勤怠履歴一覧の編集ボタンから開く:

```
┌──────────────────────────────────┐
│    メモ編集 (2026/07/13 出勤)     │
│                                  │
│  カテゴリ:                        │
│  ┌────────────────────────────┐  │
│  │ [直行] [電車遅延] [在宅]    │  │
│  │ [障害対応] [その他]         │  │
│  └────────────────────────────┘  │
│                                  │
│  メモ:                           │
│  ┌────────────────────────────┐  │
│  │ 客先に直接訪問              │  │
│  └────────────────────────────┘  │
│                       8/200文字   │
│                                  │
│  [削除]  [キャンセル]  [保存]     │
└──────────────────────────────────┘
```

---

## 5. 権限

| 操作 | 本人 | 上長 | 管理者 |
|------|:----:|:----:|:------:|
| メモ付き打刻 | o | - | - |
| メモ閲覧（自分） | o | - | - |
| メモ閲覧（部下） | - | o | - |
| メモ編集 | o | - | - |
| メモ削除 | o | - | - |

- 上長はチーム勤怠画面でメモを閲覧できるが、編集・削除はできない
- 管理者の全社員勤怠画面でもメモは表示される（既存の閲覧権限に従う）

---

## 6. バリデーションルール

| 条件 | ルール |
|------|--------|
| memo を付ける場合 | category 必須 |
| category が OTHER | note 必須（1文字以上） |
| note | 最大200文字 |
| category × memoType | 出勤カテゴリは CLOCK_IN のみ、退勤カテゴリは CLOCK_OUT のみ |
| ユニーク | 1つの attendance_record に同一 memo_type は1つまで |
