📦 Product API Spec (v1.0)
공통 규칙
JSON field: camelCase
enum은 대문자 문자열
날짜: ISO-8601 (2026-03-03T11:30:00)
목록 응답은 pagination meta 포함

# Shop API (v1)

## 공통 규칙
- JSON field: camelCase
- enum: 대문자 문자열 (ACTIVE, HIDDEN, DRAFT, PENDING 등)
- 날짜/시간: ISO-8601 (예: 2026-03-03T11:30:00)
- 목록 응답: pagination meta 포함

## 응답 공통 필드(권장)
- message: CREATED / UPDATED 등
- errors: 검증 실패 시 필드 단위 에러 목록(추후 확정)
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid request",
  "fieldErrors": [
  { "field": "price", "reason": "must be >= 0" }
  ]
  }
 ```


## 공통 Enum 표
-ProductStatus: DRAFT/ACTIVE/HIDDEN
-OrderStatus: PENDING/PAID/SHIPPED/DELIVERED/CANCELLED
-GripSize: G1~G5

## 정렬(sort)
- LATEST
- PRICE_ASC
- PRICE_DESC