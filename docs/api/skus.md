📦 Product API Spec (v1.0)
공통 규칙
JSON field: camelCase
enum은 대문자 문자열
날짜: ISO-8601 (2026-03-03T11:30:00)
목록 응답은 pagination meta 포함

PATCH /api/admin/skus/{id}/inventory

Request
```json
{ "stockQty": 8, "safetyStockQty": 2 }
```
Response
```json
{ "skuId": 2001, "stockQty": 8, "safetyStockQty": 2, "updatedAt": "2026-03-03T12:00:00" }
```