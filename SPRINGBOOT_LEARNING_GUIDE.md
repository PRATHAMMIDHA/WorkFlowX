# 🚀 Spring Boot Master Learning & Revision Guide
### *Everything You Need to Know, Explained Simply from Scratch*
*Based on real-world implementation in the **WorkFlowX** Enterprise Platform*

---

## 📑 Table of Contents
1. [Core Fundamentals: What is Spring Boot?](#1-core-fundamentals-what-is-spring-boot)
2. [The Core Philosophy: IoC and Dependency Injection](#2-the-core-philosophy-ioc-and-dependency-injection)
3. [Project Anatomy & Application Bootstrap](#3-project-anatomy--application-bootstrap)
4. [Configuration & Profiles (`application.yml`)](#4-configuration--profiles-applicationyml)
5. [The 3-Tier Layered Architecture](#5-the-3-tier-layered-architecture)
6. [Data Layer: Spring Data JPA & Hibernate](#6-data-layer-spring-data-jpa--hibernate)
7. [Security & Authentication (Spring Security 6 + JWT)](#7-security--authentication-spring-security-6--jwt)
8. [DTOs & Request Validation](#8-dtos--request-validation)
9. [Global Exception Handling (`@RestControllerAdvice`)](#9-global-exception-handling-restcontrolleradvice)
10. [Asynchronous Processing (`@Async`)](#10-asynchronous-processing-async)
11. [Lombok Annotations Cheat Sheet](#11-lombok-annotations-cheat-sheet)
12. [End-to-End Request Lifecycle (Trace a Real API Call)](#12-end-to-end-request-lifecycle-trace-a-real-api-call)

---

## 1. Core Fundamentals: What is Spring Boot?

### The Problem Before Spring Boot
In traditional Java Enterprise (Java EE / pure Spring Framework):
- You had to write hundreds of lines of complex XML files (`web.xml`, `applicationContext.xml`).
- You had to manually install an external web server (like Apache Tomcat), package your code as a `.war` file, and deploy it.
- Resolving conflicting jar versions was a nightmare.

### The Spring Boot Solution
**Spring Boot** makes it easy to create stand-alone, production-grade Spring-based applications:
1. **Embedded Web Server**: It includes Tomcat inside the jar. You run your app by simply running `public static void main()`.
2. **Starter Dependencies**: Instead of guessing compatible library versions, you import one starter (e.g. `spring-boot-starter-web`), and Maven pulls all tested, compatible libraries.
3. **Auto-Configuration**: Spring Boot scans your classpath. If it sees `h2` or `postgresql`, it automatically sets up database connection pools for you.

---

## 2. The Core Philosophy: IoC and Dependency Injection

### The Everyday Analogy: Building a Car
- **Without Spring (Manual way)**: Every time you need a Car, you manually create the Engine, the Tires, and the Transmission inside the Car constructor using `new Engine()`. If you want to change the engine type, you have to rewrite the Car class.
- **With Spring (Dependency Injection)**: You tell the factory (Spring): *"I need an Engine and Tires."* Spring creates them in its central container and **injects** them into your Car.

### Key Terms:
- **Bean**: Any Java object managed, instantiated, and configured by the Spring container.
- **ApplicationContext (IoC Container)**: The "brain" of Spring where all your beans live.
- **Dependency Injection (DI)**: Giving an object the things it needs instead of making the object create them itself.

### How we do it in WorkFlowX (Constructor Injection with Lombok):
```java
@Service
@RequiredArgsConstructor // Lombok generates a constructor with all 'final' fields
public class TaskService {

    // Spring finds the TaskRepository bean in the IoC container and injects it here
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
}
```
> **Rule of thumb**: Never use `@Autowired` on private fields in modern Spring. Use `private final` fields with `@RequiredArgsConstructor` (Constructor Injection) — it makes code immutable, easier to test, and avoids null pointer errors.

---

## 3. Project Anatomy & Application Bootstrap

Look at our root class: [`WorkFlowXApplication.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/WorkFlowXApplication.java)

```java
package com.workflowx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkFlowXApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkFlowXApplication.class, args);
    }
}
```

### What does `@SpringBootApplication` actually do?
It is a 3-in-1 combo annotation:
1. **`@Configuration`**: Declares that this class can define Spring `@Bean` methods.
2. **`@EnableAutoConfiguration`**: Tells Spring Boot to start adding beans based on classpath settings.
3. **`@ComponentScan`**: Tells Spring to scan the current package (`com.workflowx`) and **all its sub-packages** (`com.workflowx.task`, `com.workflowx.security`, etc.) for classes annotated with `@Component`, `@Service`, `@Repository`, or `@RestController`.

---

## 4. Configuration & Profiles (`application.yml`)

Spring Boot reads settings from YAML files inside `src/main/resources/`.

### Why YAML over `.properties`?
YAML supports hierarchical, readable formatting without repeating prefixes.

### In WorkFlowX:
- [`application.yml`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/resources/application.yml): Common settings (JWT secrets, token expiry, server port `8080`).
- [`application-local.yml`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/resources/application-local.yml): Local development settings:
  ```yaml
  spring:
    datasource:
      url: jdbc:h2:mem:workflowxdb    # Fast in-memory database
      driver-class-name: org.h2.Driver
    jpa:
      hibernate:
        ddl-auto: update               # Auto-creates/updates SQL tables from Java entity classes
      show-sql: true                   # Prints generated SQL to console
  ```

### How to activate a profile?
```bash
-Dspring.profiles.active=local
```
Spring Boot loads `application.yml` first, and then overrides properties with `application-local.yml`.

---

## 5. The 3-Tier Layered Architecture

WorkFlowX strictly separates responsibilities into three distinct layers:

```
┌────────────────────────────────────────┐
│ 1. Controller Layer (@RestController)   │ -> Handles HTTP routing, JSON inputs/outputs
└───────────────────┬────────────────────┘
                    │ calls
┌───────────────────▼────────────────────┐
│ 2. Service Layer (@Service)             │ -> Business logic, validations, transactions
└───────────────────┬────────────────────┘
                    │ calls
┌───────────────────▼────────────────────┐
│ 3. Repository Layer (@Repository)       │ -> Communicates directly with Database via SQL/JPA
└───────────────────┬────────────────────┘
                    │ interacts with
┌───────────────────▼────────────────────┐
│ 4. Database Entities (@Entity)          │ -> Java representations of database tables
└────────────────────────────────────────┘
```

---

## 6. Data Layer: Spring Data JPA & Hibernate

### What is ORM (Object-Relational Mapping)?
Databases store data in **tables and rows**. Java programs work with **objects and classes**.
- **Hibernate** is the translator between Java objects and SQL tables.
- **Spring Data JPA** is a library built on top of Hibernate that removes almost all boilerplate code.

### 1. Base Entity & Auditing: [`Auditable.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/common/Auditable.java)
Instead of adding `createdAt` and `updatedAt` to every table manually:
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class Auditable {

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```
Every entity extending `Auditable` automatically has timestamp tracking managed by Spring JPA!

### 2. Entity Relationships in WorkFlowX
Look at [`Task.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/task/entity/Task.java):
```java
@Entity
@Table(name = "tasks")
public class Task extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // Many tasks belong to One Project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Many tasks can be assigned to One User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;
}
```
- `@Entity`: Tells Hibernate this class maps to a database table.
- `@Table(name = "tasks")`: Specifies the SQL table name.
- `fetch = FetchType.LAZY`: **Crucial for performance!** It tells Hibernate: *don't load the entire project data into memory until someone explicitly asks for `task.getProject()`*.

### 3. Magic Queries with `JpaRepository`: [`TaskRepository.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/task/repository/TaskRepository.java)
```java
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findBySprintId(Long sprintId);
    Long countByStatus(TaskStatus status);
}
```
Notice you didn't write a single line of SQL! 
Spring Data JPA reads the method name `findByProjectId` and automatically generates:
```sql
SELECT * FROM tasks WHERE project_id = ?;
```

---

## 7. Security & Authentication (Spring Security 6 + JWT)

Look at your [`SecurityConfig.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/security/SecurityConfig.java):

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF (Cross-Site Request Forgery)
            // Why? We use stateless JWT tokens stored in headers, not cookies. CSRF attacks cannot occur.
            .csrf(AbstractHttpConfigurer::disable)

            // 2. Enable CORS (Cross-Origin Resource Sharing)
            // Allows React frontend running on port 5173 to call this backend on port 8080.
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 3. Make Session Stateless
            // Spring will NOT store user sessions in memory. Every request must bring its own JWT.
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 4. Define Endpoint Access Rules
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**", "/api/health").permitAll() // Public: Register, Login
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()      // Pre-flight CORS checks
                    .anyRequest().authenticated()                               // All other APIs require JWT!
            )

            // 5. Custom 401 & 403 JSON responses
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, e) -> {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"UNAUTHORIZED\"}");
                    })
            )

            // 6. Put our JWT filter BEFORE UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

### How the JWT Filter Works: [`JwtAuthFilter.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/security/JwtAuthFilter.java)
For every incoming HTTP request:
1. Intercept request before it reaches the controller.
2. Read the `Authorization` header: `Bearer eyJhbGciOiJIUzI1NiJ9...`
3. If header starts with `Bearer `, extract the token string.
4. Call [`JwtService.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/security/JwtService.java) to verify signature and extract user email.
5. If valid, build a `UsernamePasswordAuthenticationToken` and place it in the `SecurityContextHolder`.
6. Now Spring knows: *"This user is authenticated as user@example.com with role ROLE_MEMBER!"*

---

## 8. DTOs & Request Validation

### Why never return Entities directly in Controller APIs?
1. **Security leak**: Entities contain password hashes, internal DB IDs, and private details.
2. **Infinite JSON loop**: If `Project` has a list of `Task`s, and `Task` has a `Project`, Jackson JSON serializer will crash with `StackOverflowError` trying to serialize an endless loop.
3. **API Contract Stability**: Database tables can change without breaking client frontend contracts.

### Solution: DTOs (Data Transfer Objects)
- Inbound: [`CreateTaskRequest.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/task/dto/CreateTaskRequest.java)
- Outbound: [`TaskDTO.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/task/dto/TaskDTO.java)

### Jakarta Validation in Action:
```java
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private TaskPriority priority;
}
```
In the controller:
```java
@PostMapping
public ResponseEntity<ApiResponse<TaskDTO>> createTask(
        @Valid @RequestBody CreateTaskRequest request) { // <-- @Valid triggers automatic validation!
    ...
}
```
If `title` is blank, Spring blocks execution before the service runs and throws `MethodArgumentNotValidException`.

---

## 9. Global Exception Handling (`@RestControllerAdvice`)

Instead of writing `try-catch` inside every single controller endpoint:
Look at [`GlobalExceptionHandler.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/exception/GlobalExceptionHandler.java):

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Catch 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "NOT_FOUND",
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Catch validation failures (e.g. Blank title)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(new ErrorResponse(400, "VALIDATION_FAILED", msg, null));
    }
}
```

Whenever any service throws `throw new ResourceNotFoundException("Task not found")`, this central class catches it and formats it into a uniform JSON response!

---

## 10. Asynchronous Processing (`@Async`)

Look at [`AsyncConfig.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/config/AsyncConfig.java):

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("WorkflowX-Async-");
        executor.initialize();
        return executor;
    }
}
```

### Why use `@Async`?
When a user updates a task status:
1. Save status to Database (critical - user is waiting).
2. Record activity in audit log (background task).
3. Send in-app notification to assignee (background task).

By annotating the notification method with `@Async`:
- The client receives a fast `200 OK` in ~20ms.
- Background threads in the `ThreadPoolTaskExecutor` execute the non-critical logging and notifications without delaying the user!

---

## 11. Lombok Annotations Cheat Sheet

| Annotation | What it does |
| :--- | :--- |
| `@Getter` / `@Setter` | Generates `getId()`, `setTitle()`, etc. |
| `@NoArgsConstructor` | Generates a 0-argument constructor (required by Hibernate/JPA). |
| `@AllArgsConstructor`| Generates a constructor with all parameters. |
| `@RequiredArgsConstructor` | Generates a constructor for all `final` fields (used for Dependency Injection). |
| `@Builder` | Implements the Builder Pattern: `Task.builder().title("Bug").build()`. |
| `@Slf4j` | Creates a logging instance: `log.info("Task created with id {}", id)`. |

---

## 12. End-to-End Request Lifecycle (Trace a Real API Call)

### Scenario: User creates a Task: `POST /api/v1/tasks`

```
1. Client sends HTTP POST:
   Header: Authorization: Bearer eyJhbGciOi...
   Body:   {"title": "Fix login bug", "projectId": 5}

2. Filter Chain:
   -> CorsFilter checks if http://localhost:5173 is allowed (YES).
   -> JwtAuthFilter extracts token -> validates secret & expiry -> extracts user email.
   -> Sets Authentication in SecurityContextHolder.

3. Controller Layer (TaskController.java):
   -> @Valid checks if title is not blank.
   -> Calls taskService.createTask(request, currentUser).

4. Service Layer (TaskService.java):
   -> @Transactional opens a database transaction.
   -> projectRepository.findById(5) fetches the Project.
   -> Task entity is constructed using Task.builder().
   -> taskRepository.save(task) saves the record to DB.
   -> Asynchronously calls notificationService.notifyAssignee().
   -> Converts Task entity to TaskDTO.

5. Database:
   -> Hibernate generates SQL: INSERT INTO tasks (...) VALUES (...);
   -> Database assigns auto-increment ID = 42.

6. Response:
   -> Controller wraps TaskDTO into ApiResponse<TaskDTO>.
   -> Jackson serializes Java object to JSON:
      HTTP 201 CREATED
      {"success": true, "data": {"id": 42, "title": "Fix login bug", ...}}
```

---

### 💡 Pro Tips for Your Revision:
1. Always start reading a feature from its **Entity** (`Task.java`), then **Repository** (`TaskRepository.java`), then **Service** (`TaskService.java`), and finally **Controller** (`TaskController.java`).
2. If you see an error `401 Unauthorized`, look at [`JwtAuthFilter.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/security/JwtAuthFilter.java).
3. If you see an error `403 Forbidden`, look at permissions in [`SecurityConfig.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/security/SecurityConfig.java).
4. If you see an unexpected JSON error, look at [`GlobalExceptionHandler.java`](file:///c:/Users/midha/Downloads/PROJECTS/WorkFlowX/workflowx-backend/src/main/java/com/workflowx/exception/GlobalExceptionHandler.java).
