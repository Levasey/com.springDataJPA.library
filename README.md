# 📚 Библиотека — Spring Data JPA

Учебное веб-приложение для учёта **читателей** и **книг**: выдача и возврат, поиск, пагинация и сортировка. Классический стек **Spring MVC** (без Boot), **Spring Data JPA**, **Hibernate**, **Thymeleaf**, **PostgreSQL**; тесты на **JUnit 5** и **H2**.

---

## Возможности

| Область | Что есть |
|--------|----------|
| **Люди** | Список, карточка с книгами на руках, создание, редактирование, удаление |
| **Книги** | Список, детальная страница, CRUD, **поиск по названию**, **пагинация** (`page`, `books_per_page`), **сортировка по году** |
| **Выдача** | Назначение книги читателю, возврат (`assign` / `release`) |
| **Качество** | Bean Validation на сущностях, глобальный обработчик ошибок, юнит-тесты сервисов, репозиториев и контроллеров |

---

## Стек

- Java, Maven (`packaging: war`)
- Spring Framework 5.3 · Spring Data JPA 2.4 · Spring MVC
- Hibernate 5 · HikariCP
- Thymeleaf (Spring5)
- PostgreSQL (прод/разработка) · H2 (тесты)
- JUnit Jupiter · Mockito · Hamcrest

---

## Архитектура (кратко)

```mermaid
flowchart LR
  subgraph web [Web]
    C[Controllers]
    V[Thymeleaf views]
  end
  subgraph domain [Domain]
    S[Services]
    R[Spring Data repositories]
  end
  DB[(PostgreSQL)]
  V --> C --> S --> R --> DB
```

Маршруты в основном под префиксами `/people` и `/books` (см. `PeopleController`, `BookController`).

---

## Требования

- JDK 8+ (совместимо с версией Spring в проекте)
- Maven 3.6+
- PostgreSQL с базой данных (по умолчанию имя БД — `library`, см. пример конфигурации ниже)
- Сервер приложений с поддержкой Servlet API 4 (например, Apache Tomcat 9+) для деплоя **WAR**

---

## Настройка базы данных

1. Создайте БД, например:

   ```sql
   CREATE DATABASE library;
   ```

2. Скопируйте пример свойств и подставьте свои значения:

   ```bash
   cp src/main/resources/hibernate.properties.example src/main/resources/hibernate.properties
   ```

   Пароль можно не дублировать в файле: если `hibernate.connection.password` пустой или отсутствует, приложение возьмёт его из переменной окружения **`LIBRARY_DB_PASSWORD`** (см. `SpringConfig#dataSource`).

3. Убедитесь, что в `hibernate.properties` указаны корректные `hibernate.connection.url`, `username`, при необходимости `password` (или задайте только `LIBRARY_DB_PASSWORD`), а также `hibernate.dialect` для PostgreSQL.

Схему таблиц нужно подготовить самостоятельно (SQL-скрипт или миграции): в текущем конфиге JPA **нет** `hibernate.hbm2ddl.auto` — поведение «создать таблицы из сущностей» по умолчанию не включено.

<details>
<summary><strong>Пример DDL для PostgreSQL</strong> (раскрыть)</summary>

```sql
CREATE TABLE person (
  person_id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  surname VARCHAR(255) NOT NULL,
  age INTEGER NOT NULL,
  email VARCHAR(255) NOT NULL,
  address VARCHAR(255),
  date_of_birth DATE
);

CREATE TABLE book (
  book_id SERIAL PRIMARY KEY,
  book_name VARCHAR(255) NOT NULL,
  author VARCHAR(255) NOT NULL,
  year_published INTEGER NOT NULL,
  taken_at TIMESTAMP,
  person_id INTEGER REFERENCES person (person_id)
);
```

</details>

---

## Сборка и тесты

```bash
# Сборка WAR
mvn clean package

# Только тесты
mvn test
```

Через **Makefile**:

| Команда | Действие |
|---------|----------|
| `make package` | Сборка `target/com.springDataJPA.library.war` |
| `make test` | Запуск тестов |
| `make clean` | Очистка `target/` |
| `make skip-tests` | Сборка без тестов |

После `mvn package` артефакт: **`target/com.springDataJPA.library.war`** — разверните его в Tomcat (или другом контейнере) и откройте приложение по контексту, заданному при деплое.

---

## Полезные URL (после деплоя)

| Путь | Описание |
|------|----------|
| `/people` | Список читателей |
| `/people/new` | Новый читатель |
| `/books` | Список книг; опционально `?page=1&books_per_page=10&sort_by_year=true` |
| `/books/search` | Поиск по названию |
| `/books/new` | Новая книга |

---

## Структура проекта (выжимка)

```
src/main/java/com/springDataJPA/library/
├── config/          # Spring / JPA / MVC
├── controllers/     # PeopleController, BookController
├── services/
├── repositories/    # Spring Data JPA
├── models/          # Person, Book
└── exception/       # ResourceNotFoundException, GlobalExceptionHandler

src/main/webapp/WEB-INF/views/
├── people/
└── books/

src/test/java/       # тесты с TestJpaConfig и H2
```

---

## Лицензия

Проект учебный; при публикации добавьте выбранную лицензию при необходимости.
