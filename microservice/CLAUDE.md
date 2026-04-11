# Heloword Microservices Backend

## Stack
Spring Boot 2.2.6 · Java 11 · Maven multi-module · gateway port 9487

## Modules
| Module | Purpose |
|---|---|
| `service-auth` | Login, session, Google OAuth |
| `service-user` | User profile, roles |
| `service-word` | Vocab data (EN/DE/JP words & sentences) |
| `service-record` | Quiz history and records |
| `service-scraper` | Sentence scraping |
| `frontend-api` | Aggregation layer for React frontend |
| `infra-gateway` | Routes `/k8s/*` → services (strips `/k8s`) |
| `infra-discovery` | Eureka service discovery |
| `common-shared` | Shared entities, DTOs, base classes |

## Response Wrapper
All responses: `HeloResponse<T>` (`common-shared/.../base/dto/HeloResponse.java`)
`{ "code": "0000", "message": "...", "timestamp": "...", "data": {...} }`
`"0000"` = success · `"9403"` = auth error

## Frontend location
`~/ryan/projects_ryan/workspace/heloword-ng-frontend/heloword-react`

---

## service-auth `/auth`
- `POST /init-cookie` — init HTTP-only session cookie
- `POST /init-cipher` → `{ aesKey, aesIv }` — AES key/IV for `cv` security header
- `POST /verify-google-id` body:`{ credential, provider:"GOOGLE", idToken }` → `UserDto`
- `POST /check-login-status` — validate session
- `POST /logout` — clear cookies/session

## service-user `/user`
Standard CRUD on `MemberEntity`. Extra: `GET /user/email/{email}`

## service-user `/role`
Standard CRUD on `RoleEntity` `{ id, role, name }`

---

## service-word
Three word controllers, identical pattern:
`/word-english` · `/word-japanese` · `/word-german`
- Standard CRUD + `GET /{base}/example/{word}` — fuzzy search on `word`, `translateEn`, `translateCh`
- Entity fields: `id, word, phonetics, translateEn, translateCh, level, tag, type, note, info, language, tableName, createDate, updateDate, status`

Three sentence controllers, same pattern + `sentence` field (2048 chars):
`/sentence-english` · `/sentence-japanese` · `/sentence-german`
- JP uses `漢字[かな]` notation and `<b>...</b>` for emphasis

---

## service-record `/record-quiz`
Standard CRUD plus:
- `POST /get-by-setting-ids` body:`List<Long> settingIds` header:`username` → `List<RecordQuizEntity>`
- `POST /get-latest-finished-time-by-setting-ids` same inputs → `Map<settingId, latestFinishedTime>`

## service-record `/record-quiz-setting`
Standard CRUD plus:
- `GET /get-quiz-settings` header:`username` → `List<RecordQuizSettingEntity>`
- `GET /get-finished-count` header:`username` → `Map<settingId, finishedCount>`

---

## frontend-api (all require MEMBER role)

### `/fe/home`
- `POST /dashboard` → `{ wordEnglishList, wordGermanList, wordJapaneseList, sentenceEnglishList, sentenceGermanList, sentenceJapaneseList }`

### `/fe/user`
- `POST /` → `UserDto`

### `/fe/quiz`
- `POST /save-single-record` body:`RecordQuizDto`
- `POST /save-setting-records` body:`List<RecordQuizSettingDto>` → `{ ids:[Long] }`
- `POST /get-quiz-settings` → `Map<dateString, List<RecordQuizSettingDto>>`
- `POST /get-record-ids-by-setting-ids` body:`List<Long>` → `Map<settingId, [answerId]>`

### `/fe/notifications`
- `POST /due-for-review` → `List<DueWordDto>` `{ answerId, answerTableName, lastReviewTime, nextReviewTime, reviewCount, correctCount }`
- Intervals by `correctCount`: 0-1→1d · 2→3d · 3→7d · 4→14d · 5→30d · 6+→90d

### `/fe/stats`
- `POST /daily-summary` → `List<DailyStatDto>` `{ date(yyyy-MM-dd), total, wrongCount, timeSpent(sec) }` — past 7 days, oldest first, includes 0-review days

---

## Models

**UserDto:** `username, fullname, nickname, picture, locale, email, googleToken, facebookToken, roles:[{role,name}]`

**RecordQuizDto:** `username, answerId, quizIndex, answerTableName, timeSpent, startTime, finishedTime, pronounceCount, deleteCount, wrongCount, recordQuizSettingId`

**RecordQuizSettingDto:** `id, timestamp, createTime, latestFinishedTime, type, min, max, total, isSelected, finishedCount`
