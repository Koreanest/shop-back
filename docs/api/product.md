📦 Product API Spec (v1.0)
공통 규칙
JSON field: camelCase
enum은 대문자 문자열
날짜: ISO-8601 (2026-03-03T11:30:00)
목록 응답은 pagination meta 포함

1️⃣ GET /api/products
Query Params
brandIds (optional)
minPrice (optional)
maxPrice (optional)
weightMin (optional)
weightMax (optional)
headSize (optional)
pattern (optional)
page (default 0)
size (default 20)
sort (LATEST | PRICE_ASC | PRICE_DESC)

Response
```json
{
"items": [
{
"id": 1203,
"brand": {
"id": 3,
"name": "YONEX",
"slug": "yonex"
},
"title": "VCORE 100 2023",
"series": "VCORE",
"price": 289000,
"status": "ACTIVE",
"slug": "vcore-100-2023",
"categoryId": 100,
"imageUrl": "/uploads/products/1203/main.jpg",
"specSummary": {
"headSizeSqIn": 100,
"unstrungWeightG": 300,
"pattern": "16x19"
},
"variantSummary": {
"minPrice": 289000,
"maxPrice": 289000,
"totalStockQty": 15
}
}
],
"page": 0,
"size": 20,
"totalElements": 1,
"totalPages": 1,
"sort": "LATEST"
}
```

2️⃣ GET /api/products/{id}
Response

```json
{
"id": 1203,
"brand": {
"id": 3,
"name": "YONEX",
"slug": "yonex"
},
"title": "VCORE 100 2023",
"series": "VCORE",
"description": "...",
"price": 289000,
"status": "ACTIVE",
"slug": "vcore-100-2023",
"categoryId": 100,
"imageUrl": "/uploads/products/1203/main.jpg",
"imagePath": "D:/uploads/.../main.jpg",

"images": [
{
"id": 9001,
"url": "/uploads/products/1203/main.jpg",
"sortOrder": 1,
"isMain": true
}
],

"spec": {
"headSizeSqIn": 100,
"unstrungWeightG": 300,
"balanceMm": 320,
"lengthIn": 27.0,
"patternMain": 16,
"patternCross": 19,
"stiffnessRa": 65
},

"skus": [
{
"id": 2001,
"productId": 1203,
"skuCode": "VCORE100-2023-G2",
"gripSize": "G2",
"price": 289000,
"isActive": true,
"stockQty": 10,
"safetyStockQty": 2
}
],

"createdAt": "2026-03-03T11:00:00",
"updatedAt": "2026-03-03T11:30:00"
}
```

3️⃣ POST /api/admin/products
Request
```json
{
"brandId": 3,
"categoryId": 100,
"title": "VCORE 100 2023",
"series": "VCORE",
"description": "...",
"price": 289000,
"status": "DRAFT",
"slug": "vcore-100-2023",
"imageUrl": "/uploads/products/placeholder.jpg",
"imagePath": "D:/uploads/.../placeholder.jpg",
"spec": {
"headSizeSqIn": 100,
"unstrungWeightG": 300
}
}
```

Response
```json
{
"id": 1203
}
```

4️⃣ PATCH /api/admin/products/{id}
규칙

누락된 필드 = 변경 없음

null 허용 필드만 null 가능

Request
```json
{
"price": 279000,
"status": "ACTIVE",
"description": null,
"spec": {
"stiffnessRa": 66
}
}
```

Response
```json
{
"id": 1203
}
```