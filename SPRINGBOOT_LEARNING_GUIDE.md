# 🚀 Spring Boot Master Learning & Revision Guide
### *Everything You Need to Know, Explained Simply from Scratch*
*Based on real-world implementation in the **WorkFlowX** Enterprise Platform*

---

## 📑 Table of Contents
1. [Core Fundamentals: What is Spring Boot?](#1-core-fundamentals-what-is-spring-boot)
2. [The Core Philosophy: IoC and Dependency Injection](#2-the-core-philosophy-ioc-and-dependency-injection)
3. [Bean Scopes & The Complete Bean Lifecycle](#3-bean-scopes--the-complete-bean-lifecycle)
4. [Project Anatomy & Application Bootstrap](#4-project-anatomy--application-bootstrap)
5. [Configuration & Profiles (`application.yml`)](#5-configuration--profiles-applicationyml)
6. [The 3-Tier Layered Architecture](#6-the-3-tier-layered-architecture)
7. [Data Layer: Spring Data JPA & Hibernate](#7-data-layer-spring-data-jpa--hibernate)
8. [Transaction Management Deep Dive (`@Transactional`)](#8-transaction-management-deep-dive-transactional)
9. [Aspect-Oriented Programming (Spring AOP)](#9-aspect-oriented-programming-spring-aop)
10. [Security & Authentication (Spring Security 6 + JWT)](#10-security--authentication-spring-security-6--jwt)
11. [Filters vs Interceptors: The Key Differences](#11-filters-vs-interceptors-the-key-differences)
12. [DTOs & Request Validation](#12-dtos--request-validation)
13. [Global Exception Handling (`@RestControllerAdvice`)](#13-global-exception-handling-restcontrolleradvice)
14. [Asynchronous Processing (`@Async`) & Scheduling](#14-asynchronous-processing-async--scheduling)
15. [Spring Boot Actuator: Production Observability](#15-spring-boot-actuator-production-observability)
16. [Spring Boot Caching (`@Cacheable`)](#16-spring-boot-caching-cacheable)
17. [REST API Best Practices & HTTP Status Codes](#17-rest-api-best-practices--http-status-codes)
18. [Lombok Annotations Cheat Sheet](#18-lombok-annotations-cheat-sheet)
19. [End-to-End Request Lifecycle (Trace a Real API Call)](#19-end-to-end-request-lifecycle-trace-a-real-api-call)
20. [🔥 Top 25 Most Asked Spring Boot Interview Questions](#20--top-25-most-asked-spring-boot-interview-questions)

---

## 1. Core Fundamentals: What is Spring Boot?

### The Problem Before Spring Boot
In traditional Java Enterprise (Java EE / pure Spring Framework):
- You had to write hundreds of lines of complex XML files (`web.xml`, `applicationContext.xml`).
- You had to manually install an external web server (like Apache Tomcat), package your code as a `.war` file, and deploy it.
- Resolving conflicting jar versions was a nightmare.

### The Spring Boot Solution
**Spring Boot** makes it easy to create stand-alone, production-grade Spring-based applications:
1. **Embedded Web Server**: It includes Tomcat inside the jar. You run your app by simply executing `public static void main()`.
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
> **Interview Tip**: Never use `@Autowired` on private fields in modern Spring. Use `private final` fields with `@RequiredArgsConstructor` (Constructor Injection) — it makes code immutable, easier to test, and avoids null pointer errors.

---

## 3. Bean Scopes & The Complete Bean Lifecycle

### Bean Scopes
A scope determines the lifespan and visibility of a bean:

| Scope | Description | Use Case |
| :--- | :--- | :--- |
| **`singleton`** *(Default)* | Only **one instance** is created per Spring IoC container. Shared by all injection points. | Services, Repositories, Controllers (stateless classes). |
| **`prototype`** | A **new instance** is created every time the bean is requested. | Stateful beans, temporary buffers. |
| **`request`** | One instance per HTTP request lifecycle. | User-specific request telemetry, tenant context. |
| **`session`** | One instance per HTTP session. | User shopping cart, web user session state. |
| **`application`** | One instance per `ServletContext`. | Global web application metrics. |
| **`websocket`** | One instance per WebSocket session. | Real-time chat session state. |

### The Complete Bean Lifecycle
When Spring starts up and manages a Bean, it follows this exact order:

```
1. Instantiation (Spring runs constructor `new MyBean()`)
   │
2. Populate Properties (Spring injects dependencies/fields)
   │
3. Aware Interfaces Callbacks (BeanNameAware, ApplicationContextAware)
   │
4. BeanPostProcessor - Before Initialization (`postProcessBeforeInitialization`)
   │
5. Initialization Callbacks:
   -> Method annotated with `@PostConstruct`
   -> `afterPropertiesSet()` of `InitializingBean` interface
   -> Custom `init-method`
   │
6. BeanPostProcessor - After Initialization (`postProcessAfterInitialization`)
   │  (This is where Spring creates AOP proxies, e.g. for @Transactional or @Security)
   │
7. BEAN IS READY FOR USE IN THE APPLICATION
   │
8. Container Shutdown:
   -> Method annotated with `@PreDestroy`
   -> `destroy()` of `DisposableBean` interface
   -> Custom `destroy-method`
```

---

## 4. Project Anatomy & Application Bootstrap

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

## 5. Configuration & Profiles (`application.yml`)

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

## 6. The 3-Tier Layered Architecture

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

## 7. Data Layer: Spring Data JPA & Hibernate

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
Spring Data JPA reads the method name `findByProjectId` and automatically generates:
```sql
SELECT * FROM tasks WHERE project_id = ?;
```

---

## 8. Transaction Management Deep Dive (`@Transactional`)

### What does `@Transactional` do?
It guarantees the **ACID** properties of database transactions:
- **Atomicity**: Either all operations in the method succeed, or everything is rolled back.
- **Consistency**: Database invariants remain valid.
- **Isolation**: Concurrent transactions don't interfere with each other.
- **Durability**: Committed data is saved permanently.

### How it works under the hood (AOP Proxy):
When you annotate a method with `@Transactional`:
1. Spring creates a dynamic **Proxy** wrapping your Service bean.
2. The proxy calls `connection.setAutoCommit(false)` before entering your method.
3. If the method completes normally $\rightarrow$ proxy calls `connection.commit()`.
4. If an exception occurs $\rightarrow$ proxy calls `connection.rollback()`.

### Propagation Levels (Top Interview Question):
- **`REQUIRED`** *(Default)*: Joins an existing transaction if one exists; if not, starts a new one.
- **`REQUIRES_NEW`**: Always suspends the current transaction and starts a completely independent new transaction.
- **`SUPPORTS`**: Executes inside a transaction if one exists, but runs non-transactionally if none exists.
- **`NOT_SUPPORTED`**: Executes non-transactionally, suspending any existing transaction.
- **`MANDATORY`**: Must run inside an existing transaction. Throws an exception if none exists.
- **`NEVER`**: Throws an exception if an active transaction exists.

### Two Critical `@Transactional` Pitfalls:
1. **The Rollback Gotcha**: By default, Spring only rolls back on **Unchecked Exceptions** (`RuntimeException` and `Error`). It will **NOT** roll back on checked exceptions (like `IOException` or `SQLException`) unless you explicitly declare:
   ```java
   @Transactional(rollbackFor = Exception.class)
   ```
2. **The Self-Invocation Gotcha**: If method A calls method B in the *same class*, and method B has `@Transactional`, the transaction will **NOT** start! Why? Because method A is calling method B directly on `this`, bypassing Spring's generated proxy.

---

## 9. Aspect-Oriented Programming (Spring AOP)

### What is AOP?
AOP allows you to separate **cross-cutting concerns** (things like logging, security checks, metrics, and transaction management) from your core business logic.

### Core Terminology:
- **Aspect**: The module containing your cross-cutting code (e.g., `LoggingAspect`).
- **Join Point**: A point during program execution, such as a method call.
- **Pointcut**: An expression that specifies which methods the advice should target (e.g. `execution(* com.workflowx.service.*.*(..))`).
- **Advice**: The action taken at a join point:
  - `@Before`: Runs before method execution.
  - `@After`: Runs after method execution (regardless of outcome).
  - `@AfterReturning`: Runs only if method completes successfully.
  - `@AfterThrowing`: Runs only if method throws an exception.
  - `@Around`: The most powerful advice; wraps the method, can measure execution time, or even abort execution.

---

## 10. Security & Authentication (Spring Security 6 + JWT)

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

## 11. Filters vs Interceptors: The Key Differences

Both intercept requests, but they operate at very different layers:

```
Incoming Request
       │
       ▼
┌─────────────────────────────────┐
│ Servlet Filters (e.g. JWT)      │  <-- Servlet container level (Tomcat).
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│ DispatcherServlet               │  <-- Spring MVC front controller.
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│ HandlerInterceptors             │  <-- Spring MVC level (has access to Controller metadata).
└──────────────┬──────────────────┘
               │
               ▼
┌─────────────────────────────────┐
│ @RestController Method          │
└─────────────────────────────────┘
```

| Feature | Filter (`OncePerRequestFilter`) | Interceptor (`HandlerInterceptor`) |
| :--- | :--- | :--- |
| **Origin** | Standard Java EE / Servlet API | Spring Framework |
| **Execution Point** | Before `DispatcherServlet` | Between `DispatcherServlet` and Controller |
| **Aware of Spring MVC?** | No. Sees raw `HttpServletRequest` | Yes. Has access to the target `HandlerMethod` |
| **Best Used For** | Authentication (JWT), CORS, gzip compression, request logging | Controller execution timing, checking controller annotations, audit logging |

---

## 12. DTOs & Request Validation

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

---

## 13. Global Exception Handling (`@RestControllerAdvice`)

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

---

## 14. Asynchronous Processing (`@Async`) & Scheduling

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

By annotating background tasks with `@Async`, the client receives a fast `200 OK` in ~20ms while background threads do the heavy lifting.

### Scheduling Tasks (`@Scheduled`):
To run scheduled background jobs (e.g. archiving completed sprints every midnight):
1. Add `@EnableScheduling` on a configuration class.
2. Annotate any void method with `@Scheduled`:
   ```java
   @Scheduled(cron = "0 0 0 * * ?") // Runs every night at 12:00 AM
   public void cleanupArchivedTasks() {
       taskRepository.deleteOldArchivedTasks();
   }
   ```

---

## 15. Spring Boot Actuator: Production Observability

Spring Boot Actuator provides production-ready endpoints to inspect your application at runtime.

### Common Endpoints:
- `/actuator/health`: Returns UP/DOWN status of the app, database, and disk space.
- `/actuator/metrics`: CPU usage, memory heap consumption, JVM thread counts.
- `/actuator/info`: Git commit info, application version.
- `/actuator/env`: Active configuration properties and environment variables.

### Kubernetes Probes:
Actuator automatically exposes:
- **Liveness Probe** (`/actuator/health/liveness`): Tells Kubernetes if the app is alive or needs a restart.
- **Readiness Probe** (`/actuator/health/readiness`): Tells Kubernetes if the app is ready to accept incoming traffic (e.g. DB connection is established).

---

## 16. Spring Boot Caching (`@Cacheable`)

Repeated database queries for static data (like user permissions, workspace settings) waste CPU and database connections.

```java
@Service
@RequiredArgsConstructor
public class ProjectService {

    // If key 'projectId' is in cache, return it immediately without querying DB!
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectDTO getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .map(ProjectDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Not found"));
    }

    // Evicts the cached project when it gets updated
    @CacheEvict(value = "projects", key = "#projectId")
    public void updateProject(Long projectId, UpdateProjectRequest req) { ... }
}
```

---

## 17. REST API Best Practices & HTTP Status Codes

| Code | Meaning | When to use in WorkFlowX |
| :--- | :--- | :--- |
| **`200 OK`** | Request succeeded. | Returning list of tasks, user profile, project details. |
| **`201 Created`** | New resource successfully created. | Task created, Project created, Workspace created. |
| **`204 No Content`** | Succeeded, but no response body. | Deleting a task or workspace. |
| **`400 Bad Request`** | Invalid input or validation failed. | Blank task title, negative sprint duration. |
| **`401 Unauthorized`** | Missing or invalid authentication token. | Expired JWT, missing `Bearer` header. |
| **`403 Forbidden`** | Authenticated, but lacks permission. | Regular MEMBER trying to delete an OWNER's workspace. |
| **`404 Not Found`** | Resource does not exist. | Task ID `999` not found in database. |
| **`409 Conflict`** | Request conflicts with current state. | Registering an email that already exists. |
| **`500 Internal Server Error`** | Unhandled server bug. | Database connection pool failure, unexpected exception. |

### Idempotency:
- **Idempotent** (`GET`, `PUT`, `DELETE`): Making the request 1 time or 100 times produces the exact same result on the server.
- **Non-Idempotent** (`POST`): Calling `POST /tasks` 5 times creates 5 separate tasks!

---

## 18. Lombok Annotations Cheat Sheet

| Annotation | What it does |
| :--- | :--- |
| `@Getter` / `@Setter` | Generates `getId()`, `setTitle()`, etc. |
| `@NoArgsConstructor` | Generates a 0-argument constructor (required by Hibernate/JPA). |
| `@AllArgsConstructor`| Generates a constructor with all parameters. |
| `@RequiredArgsConstructor` | Generates a constructor for all `final` fields (used for Dependency Injection). |
| `@Builder` | Implements the Builder Pattern: `Task.builder().title("Bug").build()`. |
| `@Slf4j` | Creates a logging instance: `log.info("Task created with id {}", id)`. |

---

## 19. End-to-End Request Lifecycle (Trace a Real API Call)

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

## 20. 🔥 Top 25 Most Asked Spring Boot Interview Questions

### Category 1: Core Spring & IoC

#### Q1: What is the difference between Spring and Spring Boot?
**Answer**: Spring is an extensive dependency injection framework requiring substantial XML or Java configuration and an external servlet container (like Tomcat). Spring Boot is an opinionated extension of Spring that provides embedded servers (Tomcat/Jetty), starter dependencies, and auto-configuration to build production-ready applications with zero manual XML configuration.

#### Q2: What is the Spring IoC Container and what are its primary types?
**Answer**: The IoC (Inversion of Control) container manages the lifecycle, configuration, and assembly of Spring beans. The two primary types are:
1. `BeanFactory`: The basic container providing lightweight dependency injection.
2. `ApplicationContext`: An enterprise extension of `BeanFactory` that adds internationalization (i18n), AOP integration, and event propagation.

#### Q3: Why is Constructor Injection preferred over Field Injection (`@Autowired`)?
**Answer**:
1. **Immutability**: Dependencies can be declared `final`.
2. **Safety**: Prevents `NullPointerException` because objects cannot be instantiated without their dependencies.
3. **Testability**: Makes unit testing trivial with mock objects without needing reflection or Spring testing context.
4. **Circular Dependency Detection**: Catches circular dependencies at application startup rather than at runtime.

#### Q4: What are the different Bean Scopes in Spring?
**Answer**: Six scopes: `singleton` (default, 1 per container), `prototype` (new instance every request), and four web-aware scopes: `request` (1 per HTTP request), `session` (1 per HTTP session), `application` (1 per ServletContext), and `websocket` (1 per WebSocket session).

#### Q5: What is the difference between `@Component`, `@Service`, and `@Repository`?
**Answer**: Functionally, they are all `@Component` stereotypes. However:
- `@Component`: Generic stereotype for any Spring-managed component.
- `@Service`: Marks business logic classes; carries semantic clarity.
- `@Repository`: Marks the data access layer and automatically translates low-level database exceptions (like `SQLException`) into Spring's unified `DataAccessException` hierarchy.

---

### Category 2: Spring Boot Internals & Auto-Configuration

#### Q6: How does Spring Boot's `@EnableAutoConfiguration` work internally?
**Answer**: It reads the `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` file. It evaluates conditional annotations like `@ConditionalOnClass`, `@ConditionalOnMissingBean`, and `@ConditionalOnProperty`. If the condition matches (e.g. `DataSource.class` is on classpath), it automatically registers the corresponding configuration bean.

#### Q7: What are Spring Boot Starters?
**Answer**: Starters are dependency descriptors that bundle compatible libraries together. For example, `spring-boot-starter-web` bundles Spring MVC, Jackson, validation, and embedded Tomcat into a single import, eliminating version mismatches.

#### Q8: What is the difference between `application.properties` and `application.yml`?
**Answer**: YAML supports hierarchical tree structures, list structures, and profile grouping without repeating prefix keys, making it significantly cleaner and more readable than flat key-value `.properties` files.

#### Q9: What is Spring Boot Actuator used for?
**Answer**: Actuator provides built-in production-ready HTTP endpoints (like `/health`, `/metrics`, `/info`) for monitoring application health, CPU/memory telemetry, database connectivity, and Kubernetes liveness/readiness probes.

#### Q10: How do you run code immediately when a Spring Boot application starts?
**Answer**: By implementing either `CommandLineRunner` (accepts raw string arguments) or `ApplicationRunner` (accepts structured `ApplicationArguments`), or using `@EventListener(ApplicationReadyEvent.class)`.

---

### Category 3: Spring Data JPA & Database Transactions

#### Q11: What is the difference between `JpaRepository` and `CrudRepository`?
**Answer**: `CrudRepository` provides basic CRUD methods (`save`, `findById`, `delete`). `JpaRepository` extends `PagingAndSortingRepository` (which extends `CrudRepository`) and adds JPA-specific operations such as `flush()`, batch deletions, and pagination/sorting out of the box.

#### Q12: What is the N+1 SELECT Problem in Hibernate and how do you solve it?
**Answer**: It occurs when fetching $N$ parent entities triggers 1 SQL query for the parents and $N$ additional queries to fetch their child relationships (e.g., fetching 100 projects causes 101 queries).
**Solutions**:
1. Use `JOIN FETCH` in JPQL: `SELECT p FROM Project p JOIN FETCH p.tasks`.
2. Use `@EntityGraph(attributePaths = {"tasks"})`.
3. Use Hibernate batch fetching: `default_batch_fetch_size: 25`.

#### Q13: What is the difference between `FetchType.LAZY` and `FetchType.EAGER`?
**Answer**: `EAGER` loads the associated relation immediately along with the parent. `LAZY` loads the relation only when it is explicitly accessed via its getter. `LAZY` should always be preferred to prevent pulling massive unneeded data graphs into memory.

#### Q14: How does `@Transactional` work in Spring and what are its default rollback rules?
**Answer**: Spring creates an AOP proxy around the class that manages `commit()` and `rollback()` on the database connection. By default, it rolls back **only on unchecked exceptions** (`RuntimeException` and `Error`). To roll back on checked exceptions, you must specify `@Transactional(rollbackFor = Exception.class)`.

#### Q15: What happens if a method calls another `@Transactional` method in the same class (Self-Invocation)?
**Answer**: The transaction on the called method is **ignored**. This happens because the call is executed on `this` rather than through the Spring-generated AOP proxy. To fix it, move the method to another service or inject the service into itself via self-injection.

---

### Category 4: Spring Security & REST APIs

#### Q16: How does Spring Security authenticate a request using JWT?
**Answer**: A custom `OncePerRequestFilter` intercepts the HTTP request, reads the `Authorization: Bearer <token>` header, verifies the token's cryptographic signature and expiration via `JwtService`, and populates the `SecurityContextHolder` with an `Authentication` object (`UsernamePasswordAuthenticationToken`).

#### Q17: Why do we disable CSRF in Spring Security for REST APIs with JWT?
**Answer**: CSRF (Cross-Site Request Forgery) attacks exploit automatic browser cookie transmission. Since REST APIs with JWT are stateless and store tokens in custom headers (e.g. `Authorization: Bearer`), browsers never attach them automatically to cross-origin requests, rendering CSRF protection unnecessary.

#### Q18: What is the difference between `401 Unauthorized` and `403 Forbidden`?
**Answer**:
- `401 Unauthorized`: Authentication is missing or invalid (the server doesn't know who you are).
- `403 Forbidden`: Authentication succeeded, but the user does not possess the required role or permission to access the resource (the server knows who you are, but says "no").

#### Q19: What is the difference between `@Controller` and `@RestController`?
**Answer**: `@RestController` is a convenience annotation combining `@Controller` and `@ResponseBody`. It automatically serializes return values directly into JSON/XML HTTP response bodies instead of resolving them to an HTML view template.

#### Q20: How do you handle exceptions globally in a Spring Boot REST API?
**Answer**: By creating a class annotated with `@RestControllerAdvice` containing methods annotated with `@ExceptionHandler(CustomException.class)` that return standardized error response DTOs with appropriate HTTP status codes.

---

### Category 5: Architecture, Performance, & Advanced

#### Q21: What is the purpose of the DTO (Data Transfer Object) pattern?
**Answer**: It decouples the internal database schema from the external API contract, prevents sensitive data leakage (like password hashes), stops infinite JSON serialization loops in bidirectional relations, and enables custom validation constraints per request type.

#### Q22: What is the difference between `@RequestParam`, `@PathVariable`, and `@RequestBody`?
**Answer**:
- `@PathVariable`: Extracts values embedded in the URI path (e.g., `/tasks/{id}` $\rightarrow$ `/tasks/42`).
- `@RequestParam`: Extracts query parameters from the URL (e.g., `/tasks?status=DONE`).
- `@RequestBody`: Deserializes the JSON/XML payload from the HTTP request body into a Java object.

#### Q23: How does Spring Boot handle asynchronous tasks with `@Async`?
**Answer**: When a method annotated with `@Async` is called, Spring intercepts the call using an AOP proxy and submits the method execution to a configured `TaskExecutor` (thread pool), returning immediately to the caller without blocking the main execution thread.

#### Q24: What is the difference between `@Mock` and `@MockBean` in Spring Testing?
**Answer**:
- `@Mock`: Standard Mockito annotation that creates a mock object without touching the Spring context (used for fast, isolated unit tests).
- `@MockBean`: Spring Boot test annotation that creates a mock object and replaces the corresponding real bean inside the `ApplicationContext` (used for integration tests like `@WebMvcTest`).

#### Q25: How do you handle database schema migrations in production Spring Boot applications?
**Answer**: Never use `ddl-auto: update` in production! Instead, use version-controlled database migration tools like **Flyway** or **Liquibase**. Migration SQL scripts (`V1__init.sql`, `V2__add_index.sql`) are committed to Git and executed automatically on startup, ensuring predictable, reproducible schema changes across environments.

---

### 💡 Golden Advice for Spring Boot Interviews:
1. Always mention **proxies** when asked how `@Transactional`, `@Async`, or `@Security` work under the hood.
2. Emphasize **statelessness** and **separation of concerns** (Controller $\rightarrow$ Service $\rightarrow$ Repository).
3. Be ready to explain the difference between **compile-time** dependencies, **runtime** proxies, and **database transaction** boundaries.
