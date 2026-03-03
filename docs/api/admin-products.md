📦 Product API Spec (v1.0)
공통 규칙
JSON field: camelCase
enum은 대문자 문자열
날짜: ISO-8601 (2026-03-03T11:30:00)
목록 응답은 pagination meta 포함

GET /api/admin/products (목록)
POST /api/admin/products (등록)
PATCH /api/admin/products/{id} (수정)
DELETE /api/admin/products/{id} (삭제)
POST /api/admin/products/{id}/images (이미지 업로드)
POST /api/admin/products/{id}/skus (옵션 생성)

1) GET /api/admin/products

Query: brandId, keyword, status, page, size

Response
```json
{
"items": [
{
"id": 1203,
"brand": { "id": 3, "name": "YONEX", "slug": "yonex" },
"title": "VCORE 100 2023",
"price": 289000,
"status": "ACTIVE",
"slug": "vcore-100-2023",
"categoryId": 100,
"imageUrl": "/uploads/products/1203/main.jpg",
"createdAt": "2026-03-03T11:00:00",
"updatedAt": "2026-03-03T11:30:00"
}
],
"page": 0,
"size": 20,
"totalElements": 1,
"totalPages": 1,
"sort": "LATEST"
}
```

2) DELETE /api/admin/products/{id}

Response
```json
{ "ok": true }
```

3) POST /api/admin/products/{id}/images


Request (multipart)
files: 이미지 파일들
sortOrders(optional): [1,2,3]
mainIndex(optional): 0

Response
```json
[
{ "id": 9001, "url": "/uploads/products/1203/main.jpg", "sortOrder": 1, "isMain": true }
]
```

4) POST /api/admin/products/{id}/skus

Request
```json
[
{
"skuCode": "VCORE100-2023-G2",
"gripSize": "G2",
"price": 289000,
"isActive": true,
"stockQty": 10,
"safetyStockQty": 2
}
]
```

Response
```json```
[
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
]
```