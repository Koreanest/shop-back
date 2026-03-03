📦 Product API Spec (v1.0)
공통 규칙
JSON field: camelCase
enum은 대문자 문자열
날짜: ISO-8601 (2026-03-03T11:30:00)
목록 응답은 pagination meta 포함

# Brands

## GET /api/brands
브랜드 목록 조회

### Response 200
```json
[
  {
    "id": 3,
    "name": "YONEX",
    "slug": "yonex",
    "logoUrl": "/uploads/brands/yonex.png"
  }
]


---

## docs/api/products.md
```md
# Products

## GET /api/products
상품 목록 조회

### Query Params
- brandIds (optional)
- minPrice (optional)
- maxPrice (optional)
- weightMin (optional)
- weightMax (optional)
- headSize (optional)
- pattern (optional) // 예: "16x19"
- page (default 0)
- size (default 20)
- sort (LATEST | PRICE_ASC | PRICE_DESC)

### Response 200
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
        "totalStockQty": 10
      }
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "sort": "LATEST"
}