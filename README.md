# MSA Board Project

Microservices Architecture (MSA) implementation for a bulletin board system.

## Environment Setup

### MySQL with Docker

1. **Pull MySQL Image**
   ```bash
   docker pull mysql:8.0.38
   ```

2. **Run MySQL Container**
   ```bash
   docker run --name msa-board-mysql -e MYSQL_ROOT_PASSWORD=root -d -p 3306:3306 mysql:8.0.38
   ```

3. **Connect to MySQL**
   ```bash
   docker exec -it msa-board-mysql bash
   mysql -u root -p
   # password: root
   ```

### Database Schema

1. **Create Database**
   ```sql
   create database article;
   use article;
   ```

2. **Create Article Table**
   ```sql
   create table article (
       article_id bigint not null primary key,
       title varchar(100) not null,
       content varchar(3000) not null,
       board_id bigint not null,
       writer_id bigint not null,
       created_at datetime not null,
       modified_at datetime not null
   );
   ```

3. **Create Indexes**
   ```sql
   create index idx_board_id_article_id on article(board_id asc, article_id desc);
   ```

## Getting Started

### Prerequisites
- JDK 21
- Docker

### Build Commands
```bash
./gradlew build
```

### Running Services
- **Article Service:** `./gradlew :service:article:bootRun`
- **Comment Service:** `./gradlew :service:comment:bootRun`
