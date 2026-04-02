# Библиотека — Spring Boot 3, Spring Data JPA

Веб-приложение для учёта **читателей** и **книг**: выдача и возврат, **поиск в шапке** (в разделе книг — по каталогу, в разделе читателей — по читателям), отдельные страницы **`/books/search`** и **`/people/search`**, пагинация и **множественная сортировка** списка книг (на странице `/books` — форма с чекбоксами; порядок ключей сортировки на бэкенде: название → автор → жанр → год). У каждой книги задаётся **жанр** из фиксированного списка (**`Genre`**, JPA `ENUM` строкой); при создании и редактировании жанр выбирается в форме, в списке, поиске и карточке жанр показывается текстом. При добавлении читателя (**`/people/new`**) по настройкам можно отправить **приветственное письмо** с логином, паролем и номером читательского билета (**Spring Mail**, см. раздел «Почта»). **Spring Boot 3**, **Spring Data JPA**, **Hibernate 6**, **Thymeleaf**, **PostgreSQL**; миграции **Flyway**; **Spring Security** включён с **CSRF** для форм; тесты на **JUnit 5**, **Mockito**, **H2**.

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
   Миграция **`V5__book_genre.sql`** добавляет колонку **`genre`** в таблицу **`book`** (значение по умолчанию **`OTHER`** для уже существующих записей).  
   Если база уже существовала с кастомной схемой без истории Flyway, выполните **`baseline`** или используйте чистую БД — см. [документацию Flyway](https://documentation.red-gate.com/flyway).

---

## Почта: письмо при создании читателя

По умолчанию отправка **выключена** (`library.mail.welcome-reader.enabled: false` в `application.yml`). Чтобы после сохранения нового читателя (**`/people/new`**) на его email уходило письмо с логином, паролем и номером билета, задайте **SMTP** и включите флаг (удобно в **`application-local.yml`**, не коммитить пароли):

```yaml
spring:
  mail:
    host: smtp.example.com
    port: 587
    username: your-user
    password: your-secret
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

library:
  mail:
    welcome-reader:
      enabled: true
      # Необязательно: адрес From; если пусто — используется spring.mail.username
      from: "Библиотека <noreply@example.com>"
```

Письмо ставится в очередь **после успешного коммита** транзакции создания читателя и учётной записи каталога. Без настроенного **`spring.mail.host`** бин `JavaMailSender` не создаётся: при `enabled: true` в лог попадёт предупреждение, письмо не отправится.

Страница **`/register`** создаёт только пользователя каталога **без** карточки читателя и номера билета; приветственная рассылка туда не подключена.

Пароль в письме передаётся открытым текстом — в публичных системах обычно предпочитают ссылку на одноразовую установку пароля.

---

## Сборка и тесты

```bash
./mvnw clean package    # JAR: target/com.springdatajpa.library.jar
./mvnw test
```

Через **Makefile**: `make package`, `make test`, `make clean` (по умолчанию `./mvnw`).

---

## Запуск

- **Исполняемый JAR:** после `./mvnw clean package` запуск:  
  `java -jar target/com.springdatajpa.library.jar`  
  Переменные БД (`SPRING_DATASOURCE_*` или `LIBRARY_DB_PASSWORD`) задайте в окружении или через профиль (см. ниже).
- **Профиль `prod`:** сужает детализацию health-ответа и уровни логов (см. `application-prod.yml`):  
  `--spring.profiles.active=prod`
- **Actuator:** по HTTP открыты **`/actuator/health`** (GET без авторизации — для проб/load balancer) и **`/actuator/info`** (только для аутентифицированных пользователей вместе с остальными actuator-путями). При необходимости ограничьте доступ на уровне reverse proxy.
- Откройте **`http://localhost:8080/people`**, **`http://localhost:8080/books`** (порт по умолчанию — **8080**).

> Проект изначально учебный; для публичного сервиса дополнительно настраивают HTTPS, резервное копирование БД, централизованные логи и мониторинг на стороне инфраструктуры.

---

## Возможности и изменения (по сравнению с классическим Spring MVC)

| Область | Описание |
|--------|----------|
| **Стек** | Spring Boot 3.3, **jakarta.*** , Hibernate 6, Spring Security (CSRF в формах), опционально **Spring Mail** для приветственного письма читателю. |
| **Шаблон layout** | Данные для шапки (параметр **`q`**, путь запроса, цель поиска) приходят из **`LayoutModelAdvice`** (`@ControllerAdvice`), чтобы не использовать в Thymeleaf 3.1+ отключённые по умолчанию объекты **`#request`**. |
| **Поиск в шапке** | У вошедшего пользователя форма на **`sticky`**-панели: **`/books/search`** или **`/people/search`** в зависимости от текущего раздела (**`/people…`** vs остальное). Плейсхолдер и подпись для скринридеров переключаются (книги / читатели). |
| **Книги** | Список с **`Page<Book>`** и ссылками «Назад / Вперёд»; **сортировка** — форма на странице: можно отметить сразу несколько критериев (параметры `sort_by_year`, `sort_by_genre`, `sort_by_title`, `sort_by_author`; отсутствие в query = `false`). Фильтр по выдаче: **`availability_preset`** = `all` \| `free` \| `issued`; если он есть в запросе, он важнее сочетания `sort_by_availability` и `availability_issued_first`. Поиск **`Containing`** по названию и автору; на странице результатов — ссылки в **карточку** и **редактирование**; карточка книги без лишнего второго запроса (**`JOIN FETCH`** владельца). Поле **`genre`** (`EnumType.STRING`); в **`/books/new`** и **`/books/{id}/edit`** — выпадающий список жанров (модель **`genres`**). |
| **Читатели** | Поиск по подстроке **имени, фамилии, отчества, email или номера читательского билета** (`/people/search`, **`q`**). Создание на **`/people/new`**: запись в БД, учётная запись каталога с паролем из формы, опционально **email с данными для входа** (см. раздел про почту). **`getBooksByPersonId`** при отсутствии читателя даёт **404**, как и остальные операции. |
| **Обновление книги** | Поля обновляются на **управляемой** сущности (без «слепого» `save` копии). |
| **Выдача** | **`personId`** с **`@Min(1)`** на параметре + проверка в сервисе. |
| **Ошибки** | `MethodArgumentNotValidException`, `BindException`, `ConstraintViolationException`, `HandlerMethodValidationException` ведут на страницы ошибок. |

Шаблоны: **`src/main/resources/templates/`** (не `webapp/WEB-INF/views`).

---

## Полезные URL

| Путь | Описание |
|------|----------|
| `/people` | Список читателей |
| `/people/search` | Поиск по имени, фамилии, отчеству, email или номеру билета (`q=`); карточка / правка (роль библиотекаря) |
| `/people/new` | Новый читатель |
| `/books` | Список; пример query: `?page=1&books_per_page=10&sort_by_year=true&sort_by_author=true&availability_preset=free` (несколько сортировок и фильтр совместимы) |
| `/books/search` | Поиск по названию или автору (`q=`); у каждой позиции — переход в карточку / правка |
| `/books/new` | Новая книга (название, автор, год, жанр из списка) |
| `GET /actuator/health` | Проверка живости приложения (без входа) |

---

## Структура (выжимка)

```
src/main/java/com/springdatajpa/library/
├── LibraryApplication.java
├── config/
│   ├── SecurityConfig.java
│   └── LayoutModelAdvice.java   # модель для шапки (поиск, путь URI)
├── controllers/
├── services/                 # в т.ч. ReaderWelcomeMailService (welcome-письмо читателю)
├── repositories/
├── models/
└── exception/

src/main/resources/
├── application.yml
├── application-prod.yml      # профиль prod: логи, health без деталей
├── application-test.yml      # H2, без Flyway — для @DataJpaTest
├── db/migration/             # Flyway
└── templates/                # Thymeleaf

src/test/java/                # Mockito + @DataJpaTest
```

---

## Лицензия

Учебный проект; при публикации добавьте лицензию при необходимости.
