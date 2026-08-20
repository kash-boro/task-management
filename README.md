# Task Management System (Thymeleaf MVC)

A Spring Boot MVC project (College Assignment) with a browser-based UI — built with
Java 17, Spring MVC, Spring Data JPA (Hibernate), Spring Security, Thymeleaf, and MySQL.

## 1. Prerequisites

- Java 17+ (`java -version`)
- Maven (`mvn -version`) — or your IDE's built-in Maven
- MySQL running locally

## 2. Database Setup

```sql
CREATE DATABASE task_management_db;
```

(Optional — `application.properties` also has `createDatabaseIfNotExist=true`, but
creating it yourself is good practice.)

## 3. Configure Credentials

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=yourpassword
```

## 4. Run the Project

```bash
mvn spring-boot:run
```

Or run `TaskManagementApplication.java` directly from your IDE.

Hibernate auto-creates the `users` and `tasks` tables on first run
(`spring.jpa.hibernate.ddl-auto=update`).

On startup, a `CommandLineRunner` (see `TaskManagementApplication.java`) automatically
seeds two demo accounts so you can log in immediately:

| Username | Password | Role  |
|----------|----------|-------|
| admin    | admin123 | ADMIN |
| user     | user123  | USER  |

## 5. Try It in the Browser

1. Go to `http://localhost:8080` → redirects to `/login`
2. Log in with `admin` / `admin123`
3. You'll land on `/dashboard` showing task counts
4. Click **Add New Task** → fill the form → **Save**
5. Click **Tasks** in the nav to see the list, **Edit** or **Delete** any task
6. As `admin`, you'll also see a **Manage Users** link (visible only to ADMIN role)
7. Click **Logout** to end the session

## 6. Project Structure

```
src/main/java/com/taskmanager/
├── TaskManagementApplication.java   -> main class + demo user seeding
├── entity/        -> User, Task, Role, TaskStatus (Hibernate entities)
├── repository/     -> UserRepository, TaskRepository (Spring Data JPA)
├── service/        -> UserService, TaskService (business logic)
├── controller/     -> LoginController, TaskController, UserController (@Controller, return view names)
├── dto/            -> TaskDTO (used for form binding)
├── exception/      -> ResourceNotFoundException, GlobalExceptionHandler (@ControllerAdvice)
└── security/       -> SecurityConfig, CustomUserDetailsService

src/main/resources/
├── templates/       -> login.html, dashboard.html, tasks.html, add-task.html,
│                       edit-task.html, users.html, error.html
├── static/css/      -> style.css
├── static/js/       -> script.js (delete confirmation)
└── application.properties
```

## 7. MVC Flow (this is the core thing to explain in viva)

```
Browser  -->  Controller (@Controller)  -->  Service  -->  Repository (JpaRepository)  -->  Hibernate  -->  MySQL
                     |
                     v
              returns a view name (e.g. "tasks")
                     |
                     v
        Thymeleaf renders templates/tasks.html using data from the Model
```

Compare this to a `@RestController`, which returns JSON directly instead of a view name —
that's the key difference examiners often probe on.

## 8. Notes for Viva

- **Why `@Controller` and not `@RestController`?** `@RestController` = `@Controller` + `@ResponseBody`,
  which serializes return values straight to JSON. Since our methods return **view names**
  (strings like `"tasks"`) that Thymeleaf resolves to HTML templates, we use plain `@Controller`.
- **DTO instead of binding the form directly to the entity**: `TaskDTO` only exposes the
  fields the user should be able to edit (title, description, status, dueDate). Binding
  the form directly to the `Task` entity would risk exposing internal fields like `user`
  to tampering via the request.
- **Passwords** are hashed with `BCryptPasswordEncoder` before being saved — never stored
  in plain text. Compare with `passwordEncoder.matches()` — this happens automatically
  inside Spring Security's `DaoAuthenticationProvider`, you never call it directly.
- **`Authentication` object**: once a request is authenticated, Spring Security exposes an
  `Authentication` object to your controller methods. `authentication.getName()` gives you
  the logged-in username, which we use to look up the full `User` entity.
- **Task ownership**: every task is linked to a `User` via `@ManyToOne`. `TaskService` only
  allows a user to see/edit/delete their own tasks, using `findByIdAndUser(...)` — this
  prevents one user from tampering with another user's tasks just by guessing a task ID in the URL.
- **`GlobalExceptionHandler`** uses `@ControllerAdvice` (not `@RestControllerAdvice`) because
  it needs to return view names too — it shows a friendly `error.html` page instead of a
  raw stack trace or Whitelabel Error Page.
- **Role-based access**: `SecurityConfig` restricts `/users/**` to `hasRole("ADMIN")`.
  Spring Security automatically expects authorities prefixed with `ROLE_`, so
  `CustomUserDetailsService` converts our stored `"ADMIN"` into `"ROLE_ADMIN"`.
- **`ddl-auto=update`**: Hibernate creates/updates tables automatically from your `@Entity`
  classes — convenient for development, not recommended for production (you'd use a
  migration tool like Flyway there instead).
- **No registration page** — this project seeds two demo accounts via `CommandLineRunner`
  on startup instead, since the required pages are Login / Dashboard / Tasks / Add / Edit only.
