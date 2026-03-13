# Development Setup Guide

## 1. Clone Repository

Clone the repository and move into the project directory.

```bash
git clone <repository-url>
cd shop-back
```
## 2. Start Docker

Start the services using Docker Compose.

```bash
docker-compose up -d
```
## 3. Verify Tables

Enter the MySQL container:

```bash
docker exec -it shop-mysql mysql -u root -p
```

## 4. Verify Seed Data

Run the following queries to verify seed data:

```sql
SELECT * FROM brands;
SELECT * FROM products;
SELECT * FROM skus;
```

## 5. Reset Database

Stop and remove containers and volumes:

```bash
docker-compose down -v
```