# Библиотека — Spring Boot 3, Spring Data JPA

Веб-приложение для учёта **читателей** и **книг**: выдача и возврат, поиск по подстроке названия, пагинация и сортировка. **Spring Boot 3**, **Spring Data JPA**, **Hibernate 6**, **Thymeleaf**, **PostgreSQL**; миграции **Flyway**; **Spring Security** включён с **CSRF** для форм; тесты на **JUnit 5**, **Mockito**, **H2**.

---

## Требования

- **JDK 17+** (для Spring Boot 3; на JDK 24 тесты используют `-Dnet.bytebuddy.experimental=true` в Surefire, см. `pom.xml`).
- PostgreSQL с базой `library` (или своё имя — в конфиге).
- Maven Wrapper: **`./mvnw`** (отдельно Maven не обязателен).

---

## Настройка базы и секретов

1. Создайте БД, например:

   ```sql
   CREATE DATABASE library;
   ```

2. Скопируйте пример локальных свойств:

   ```bash
   cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
   ```

   Задайте пароль в файле или через **`LIBRARY_DB_PASSWORD`**. Файл `application-local.yml` рекомендуется добавить в `.gitignore` (секреты).

3. Запуск с профилем **`local`** подхватит `application-local.yml`:

   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
   ```

4. Схема БД создаётся и обновляется **Flyway** (`src/main/resources/db/migration`). JPA в основном профиле: **`spring.jpa.hibernate.ddl-auto=validate`** (сущности и миграции должны совпадать).  
   Если база уже существовала с кастомной схемой без истории Flyway, выполните **`baseline`** или используйте чистую БД — см. [документацию Flyway](https://documentation.red-gate.com/flyway).

---

## Сборка и тесты

```bash
./mvnw clean package    # WAR: target/com.springDataJPA.library.war
./mvnw test
```

Через **Makefile**: `make package`, `make test`, `make clean` (по умолчанию `./mvnw`).

---

## Запуск

- **Встроенный Tomcat (Executable WAR / Jar):** после добавления зависимости и настройки можно использовать `java -jar`; для классического **WAR** с внешним Tomcat разверните **`target/com.springDataJPA.library.war`** и задайте переменные окружения или `application-local.yml` на сервере.
- Откройте **`http://localhost:8080/people`**, **`http://localhost:8080/books`** (порт по умолчанию у Spring Boot — **8080**).

---

## Возможности и изменения (по сравнению с классическим Spring MVC)

| Область | Описание |
|--------|----------|
| **Стек** | Spring Boot 3.3, **jakarta.*** , Hibernate 6, Spring Security (CSRF в формах). |
| **Книги** | Список с **`Page<Book>`** и ссылками «Назад / Вперёд»; поиск **`Containing`** по названию; карточка книги без лишнего второго запроса (**`JOIN FETCH`** владельца). |
| **Читатели** | **`getBooksByPersonId`** при отсутствии читателя даёт **404**, как и остальные операции. |
| **Обновление книги** | Поля обновляются на **управляемой** сущности (без «слепого» `save` копии). |
| **Выдача** | **`personId`** с **`@Min(1)`** на параметре + проверка в сервисе. |
| **Ошибки** | `MethodArgumentNotValidException`, `BindException`, `ConstraintViolationException`, `HandlerMethodValidationException` ведут на страницы ошибок. |

Шаблоны: **`src/main/resources/templates/`** (не `webapp/WEB-INF/views`).

---

## Полезные URL

| Путь | Описание |
|------|----------|
| `/people` | Список читателей |
| `/people/new` | Новый читатель |
| `/books` | Список; `?page=1&books_per_page=10&sort_by_year=true` |
| `/books/search` | Поиск по названию |
| `/books/new` | Новая книга |

---

## Структура (выжимка)

```
src/main/java/com/springDataJPA/library/
├── LibraryApplication.java   # SpringBootServletInitializer для WAR
├── config/SecurityConfig.java
├── controllers/
├── services/
├── repositories/
├── models/
└── exception/

src/main/resources/
├── application.yml
├── application-test.yml      # H2, без Flyway — для @DataJpaTest
├── db/migration/             # Flyway
└── templates/                # Thymeleaf

src/test/java/                # Mockito + @DataJpaTest
```

---

## Лицензия

Учебный проект; при публикации добавьте лицензию при необходимости.
