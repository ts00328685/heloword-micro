# Heloword Microservices Backend

## Stack
- Spring Boot 2.2.6, Java 11, Maven multi-module
- Local dev gateway runs on port 9487

## Modules
| Module | Purpose |
|---|---|
| `service-auth` | Login, session, Google OAuth |
| `service-user` | User profile, roles |
| `service-word` | Vocabulary data (EN/DE/JP words & sentences) |
| `service-record` | Quiz history and records |
| `service-scraper` | Sentence scraping utilities |
| `frontend-api` | Aggregation layer the React frontend calls directly |
| `infra-gateway` | Routes `/k8s/*` to individual services (strips `/k8s` prefix) |
| `infra-discovery` | Eureka service discovery |
| `common-shared` | Shared entities, DTOs, base classes |

## Response Wrapper
All responses use `HeloResponse<T>` (`common-shared/.../base/dto/HeloResponse.java`):
```json
{ "code": "0000", "message": "...", "timestamp": "...", "data": { ... } }
```
- `"0000"` = success
- `"9403"` = auth error (session expired / unauthorized)

---

## service-auth — AuthRestController
**Base path:** `/auth`

### POST `/auth/init-cookie`
Initializes an HTTP-only session cookie.
- **Request body:** none
- **Response data:** none

### POST `/auth/init-cipher`
Returns the AES key and IV used to encrypt the `cv` security header.
- **Request:** `HttpServletRequest` (reads session)
- **Response data:** `{ aesKey: string, aesIv: string }`

### POST `/auth/verify-google-id`
Verifies a Google ID token and logs the user in (creates account if first time).
- **Request body:** `{ credential: string, provider: "GOOGLE", idToken: string }`
- **Response data:** `UserDto` (see Models section)

### POST `/auth/check-login-status`
Checks if the current session is valid.
- **Request:** `HttpServletRequest`
- **Response data:** none

### POST `/auth/logout`
Logs out the user by clearing cookies and session.
- **Request:** `HttpServletRequest`, `HttpServletResponse`
- **Response data:** none

---

## service-user — UserRestController
**Base path:** `/user`

### POST `/user`
Creates a new member.
- **Request body:** `MemberEntity`

### GET `/user`
Returns all members.

### GET `/user/{id}`
Returns member by ID.
- **Path variable:** `Long id`

### PUT `/user`
Updates multiple members.
- **Request body:** `List<MemberEntity>`

### DELETE `/user`
Deletes member by ID.
- **Request body:** `Long id`

### GET `/user/email/{email}`
Finds a member by email address.
- **Path variable:** `String email`

---

## service-user — RoleRestController
**Base path:** `/role`

Standard CRUD: `POST /role`, `GET /role`, `GET /role/{id}`, `PUT /role`, `DELETE /role`
- **Request/response body:** `RoleEntity` (`{ id, role, name }`)

---

## service-word — Word Controllers
All three word controllers follow the same pattern.

### WordEnglishRestController — `/word-english`
### WordJapaneseRestController — `/word-japanese`
### WordGermanRestController — `/word-german`

**Standard CRUD** (each):
- `POST /{base}` — create word
- `GET /{base}` — get all
- `GET /{base}/{id}` — get by id
- `PUT /{base}` — update multiple
- `DELETE /{base}` — delete by id

**Custom:**
- `GET /{base}/example/{word}` — fuzzy search across `word`, `translateEn`, `translateCh` fields

**Word entity fields:** `id, word, phonetics, translateEn, translateCh, level, tag, type, note, info, language, tableName, createDate, updateDate, status`

---

## service-word — Sentence Controllers
All three follow the same pattern as word controllers plus a `sentence` field.

### SentenceEnglishRestController — `/sentence-english`
### SentenceJapaneseRestController — `/sentence-japanese`
### SentenceGermanRestController — `/sentence-german`

**Extra field on entity:** `sentence` (2048 chars) — the example sentence text.
Japanese sentences use `漢字[かな]` notation and `<b>...</b>` HTML for emphasis.

---

## service-record — RecordQuizRestController
**Base path:** `/record-quiz`

Standard CRUD plus:

### POST `/record-quiz/get-by-setting-ids`
Gets all quiz records for given setting IDs and username.
- **Request body:** `List<Long> settingIds`
- **Request header:** `username`
- **Response data:** list of `RecordQuizEntity`

### POST `/record-quiz/get-latest-finished-time-by-setting-ids`
Gets the latest finished timestamp per setting.
- **Request body:** `List<Long> settingIds`
- **Request header:** `username`
- **Response data:** map of `settingId → latestFinishedTime`

---

## service-record — RecordQuizSettingRestController
**Base path:** `/record-quiz-setting`

Standard CRUD plus:

### GET `/record-quiz-setting/get-quiz-settings`
Returns all quiz settings for a username.
- **Request header:** `username`
- **Response data:** list of `RecordQuizSettingEntity`

### GET `/record-quiz-setting/get-finished-count`
Returns finished count per quiz setting for a username.
- **Request header:** `username`
- **Response data:** map of `settingId → finishedCount`

---

## frontend-api — DashboardRestController
**Base path:** `/fe/home`

### POST `/fe/home/dashboard`
Returns all vocabulary data for the home page. Auth: MEMBER role required.
- **Request body:** none
- **Response data:**
```json
{
  "wordEnglishList": [ WordEnglishEntity ],
  "wordGermanList":  [ WordGermanEntity ],
  "wordJapaneseList": [ WordJapaneseEntity ],
  "sentenceEnglishList": [ SentenceEnglishEntity ],
  "sentenceGermanList":  [ SentenceGermanEntity ],
  "sentenceJapaneseList": [ SentenceJapaneseEntity ]
}
```

---

## frontend-api — UserRestController
**Base path:** `/fe/user`

### POST `/fe/user`
Returns the currently logged-in user. Auth: MEMBER role required.
- **Request body:** none
- **Response data:** `UserDto`

---

## frontend-api — QuizRestController
**Base path:** `/fe/quiz`
All endpoints require MEMBER role.

### POST `/fe/quiz/save-single-record`
Saves one quiz answer record.
- **Request body:** `RecordQuizDto`
  ```json
  {
    "answerId": 123,
    "answerTableName": "word_english",
    "quizIndex": 0,
    "timeSpent": 5,
    "startTime": "...",
    "finishedTime": "...",
    "pronounceCount": 1,
    "deleteCount": 2,
    "wrongCount": 0,
    "recordQuizSettingId": 456
  }
  ```

### POST `/fe/quiz/save-setting-records`
Saves quiz configuration (called at start of a quiz session).
- **Request body:** `List<RecordQuizSettingDto>`
  ```json
  [{ "timestamp": "...", "type": "wordEnglishList", "min": 1, "max": 100, "total": 200, "isSelected": true }]
  ```
- **Response data:** `{ ids: [Long] }` — assigned setting IDs

### POST `/fe/quiz/get-quiz-settings`
Returns all past quiz settings for the logged-in user.
- **Request body:** none
- **Response data:** `Map<String, List<RecordQuizSettingDto>>` keyed by date string

### POST `/fe/quiz/get-record-ids-by-setting-ids`
Returns already-finished answer IDs to skip in a resumed quiz.
- **Request body:** `List<Long> settingIds`
- **Response data:** `Map<Long, List<Long>>` — `settingId → [answerId]`

---

## Models

### UserDto
```
username, fullname, nickname, picture, locale, email, googleToken, facebookToken
roles: [{ role, name }]
```

### RecordQuizDto
```
username, answerId, quizIndex, answerTableName, timeSpent,
startTime, finishedTime, pronounceCount, deleteCount, wrongCount, recordQuizSettingId
```

### RecordQuizSettingDto
```
id, timestamp, createTime, latestFinishedTime, type,
min, max, total, isSelected, finishedCount
```

---

---

## frontend-api — NotificationRestController
**Base path:** `/fe/notifications`
All endpoints require MEMBER role.

### POST `/fe/notifications/due-for-review`
Returns all words due for review based on the Ebbinghaus forgetting curve.
- **Request body:** none
- **Response data:** `List<DueWordDto>`
  ```json
  [{ "answerId": 123, "answerTableName": "word_english", "lastReviewTime": "...", "nextReviewTime": "...", "reviewCount": 5, "correctCount": 3 }]
  ```

**Forgetting curve intervals by `correctCount`:**
| Correct reviews | Next review after |
|---|---|
| 0 or 1 | 1 day |
| 2 | 3 days |
| 3 | 7 days |
| 4 | 14 days |
| 5 | 30 days |
| 6+ | 90 days |

---

## frontend-api — StatsRestController
**Base path:** `/fe/stats`
All endpoints require MEMBER role.

### POST `/fe/stats/daily-summary`
Returns per-day quiz stats for the past 7 days (including days with 0 reviews).
- **Request body:** none
- **Response data:** `List<DailyStatDto>`
  ```json
  [{ "date": "2026-03-15", "total": 42, "wrongCount": 5, "timeSpent": 180 }]
  ```
  - `date` — `yyyy-MM-dd` string, oldest day first
  - `total` — number of quiz answers submitted that day
  - `wrongCount` — sum of `wrongCount` field across all records
  - `timeSpent` — total seconds spent across all records

---

## Frontend location
`~/ryan/projects_ryan/workspace/heloword-ng-frontend/heloword-react`
