# Учебная платформа: проект по ORM

REST-сервис с 15 JPA-сущностями на Java 21, Spring Boot, Spring Data JPA и Hibernate. Основная БД — PostgreSQL. В учебном и тестовом профилях используется H2. Сервис моделирует публикацию курса, запись студента, выполнение заданий и прохождение тестов.

## Запуск

**Быстрый запуск с демоданными (JDK 21):**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
curl http://localhost:8080/api/courses
```

Профиль `dev` создаёт временную БД H2; данные исчезают при остановке приложения.

**PostgreSQL через Docker Compose:**

```bash
cp .env.example .env
# Задайте свой DB_PASSWORD в .env
docker compose up --build
curl http://localhost:8080/api/courses
```

Приложение доступно на `127.0.0.1:8080`. Данные PostgreSQL сохраняются в отдельном томе. Для запуска без Docker задайте `DB_URL`, `DB_USER` и `DB_PASSWORD` и выполните `./mvnw spring-boot:run`. База должна существовать. По умолчанию Hibernate обновляет учебную схему; режим настраивается через `DB_SCHEMA_MODE`. Для реального развёртывания схему лучше версионировать миграциями. `APP_DEMO_ENABLED=true` включает демонстрационные данные вне профиля `dev`; в Compose они включены по умолчанию.

## Модель и поведение

| Часть | Сущности и связи |
|---|---|
| Пользователи | User ↔ Profile (1:1); роли STUDENT, TEACHER и ADMIN |
| Каталог | Category → Course (1:N), User → преподаваемые Course (1:N), Course → Module → Lesson (1:N), Course ↔ Tag (M:N) |
| Участие | Enrollment связывает User и Course, CourseReview хранит отзыв студента о курсе |
| Задания | Lesson → Assignment → Submission (1:N); студент может отправить только одну работу на задание |
| Тесты | Module ↔ Quiz (1:1); Quiz → Question → AnswerOption (1:N); QuizSubmission связывает студента с тестом и хранит результат |

REST-контроллеры принимают и возвращают DTO. Правила предметной области реализованы в транзакционных сервисах, доступ к данным — через Spring Data. Open Session in View выключен. Связанные коллекции загружаются лениво; необходимые данные DTO читаются в сервисной транзакции либо загружаются явным запросом.

Это учебный API без аутентификации: идентификаторы преподавателя/студента в отдельных операциях обозначают участников сценария, а не доказывают их личность. Не открывайте сервис для публичного доступа. Ограничения целостности не позволяют удалить запись, если на неё ещё ссылаются другие сценарии.

## Тестирование

```bash
./mvnw verify
```

Интеграционные тесты используют отдельную БД H2 профиля `test`. GitHub Actions запускает Maven-проверку и сборку Docker-образа.

## API и пример сценария

Префикс всех маршрутов — `/api`. Создание обычно возвращает `201`, чтение/изменение — `200`, удаление — `204`. При ошибке возвращается `application/problem+json`: `400` для некорректного запроса, `403` при несовпадении владельца, `404` если запись не найдена, `409` при дубле или конфликтующих зависимостях. В ошибках проверки полей есть массив `errors`.

| Задача | Метод и путь после `/api` |
|---|---|
| Пользователи, категории, теги, курсы | `GET/POST /users`, `/categories`, `/tags`, `/courses`; `GET/PUT/DELETE` на тех же маршрутах с `/{id}` |
| Профиль | `GET/POST/PUT/DELETE /users/{id}/profile` |
| Модули курса | `GET/POST /courses/{id}/modules`; `GET/PUT/DELETE /modules/{id}` |
| Уроки модуля | `GET/POST /modules/{id}/lessons`; `GET/PUT/DELETE /lessons/{id}` |
| Курс с модулями | `GET /courses/{id}/details` |
| Запись студента и отписка | `GET/POST /courses/{id}/enrollments`, `DELETE /courses/{id}/enrollments/{studentId}`, `GET /students/{id}/enrollments` |
| Отзывы | `GET/POST /courses/{id}/reviews`, `PUT/DELETE /reviews/{id}` |
| Задания | `GET/POST /lessons/{id}/assignments`, `GET/PUT/DELETE /assignments/{id}` |
| Работы | `GET/POST /assignments/{id}/submissions`, `GET /students/{id}/submissions`, `GET /submissions/{id}`, `PUT /submissions/{id}/grade` |
| Тесты | `POST /modules/{id}/quizzes`, `GET /quizzes`, `GET/PUT/DELETE /quizzes/{id}` |
| Результаты тестов | `GET/POST /quizzes/{id}/submissions`, `GET /students/{id}/quiz-results`, `GET /courses/{id}/quiz-results`, `GET /modules/{id}/quiz-results` |

Для `DELETE /assignments/{id}` и `DELETE /quizzes/{id}` передайте параметр `teacherId`, для `DELETE /reviews/{id}` — `studentId`. Примеры ниже используют идентификаторы чистой демобазы; в существующей базе подставляйте значения из GET-запросов.

```bash
api=http://localhost:8080/api
curl "$api/users"
curl "$api/courses"
curl "$api/courses/1/details"
curl "$api/lessons/1/assignments"
curl -X POST "$api/assignments/1/submissions" -H 'Content-Type: application/json' \
  -d '{"studentId":2,"answer":"В одном курсе может быть несколько модулей."}'
curl -X PUT "$api/submissions/1/grade" -H 'Content-Type: application/json' \
  -d '{"teacherId":1,"grade":90,"feedback":"Добавьте пример связи многие-ко-многим."}'
curl "$api/quizzes/1"             # Идентификаторы вопросов и вариантов, без ключа ответов
curl -X POST "$api/quizzes/1/submissions" -H 'Content-Type: application/json' \
  -d '{"studentId":2,"answers":{"1":1}}'
curl "$api/courses/1/quiz-results"
```

В тесте каждому вопросу соответствует один правильный вариант, балл равен числу правильных ответов. Клиент передаёт пары «идентификатор вопроса → идентификатор выбранного варианта». В учебном сценарии связь студента с курсом проверяется перед отправкой работы или теста. Отписка не удаляет историю результатов.

## Что показать на проверке

1. Запустите демопрофиль и откройте карточку курса через `GET /api/courses/1/details`.
2. Покажите запись студента, отправку работы и выставление оценки. Повторная отправка возвращает `409 Conflict`.
3. Пройдите тест и проверьте, что результат доступен по студенту, курсу и модулю.
4. Запустите `./mvnw verify`. Тест `CatalogIntegrationTest.detachedUnfetchedCollectionFailsButEntityGraphKeepsModulesAvailable` показывает, почему нельзя обращаться к ленивой коллекции после закрытия сессии и как граф сущностей позволяет загрузить её заранее.
