# 🎓 Complete Learning Guide - Vendor Management System

## Table of Contents
1. [What This Project Does](#what-this-project-does)
2. [Technology Stack Explained](#technology-stack-explained)
3. [Architecture Overview](#architecture-overview)
4. [How Everything Connects](#how-everything-connects)
5. [Code Walkthrough](#code-walkthrough)
6. [API Design](#api-design)
7. [Database Design](#database-design)
8. [Key Concepts](#key-concepts)

---

## What This Project Does

### Business Purpose
This is a **Vendor Management System (VMS)** - software that helps companies manage their relationships with suppliers/vendors. Think of it like a CRM (Customer Relationship Management) but for vendors instead of customers.

### What It Manages:
1. **Vendors** - Company information, contact details, status (active/suspended/terminated)
2. **Vendor Requests** - Workflow for requesting new vendors (create → submit → approve/reject)
3. **Contracts** - Legal agreements with vendors (start date, end date, value, terms)
4. **Purchase Orders** - Orders placed with vendors (items, amounts, delivery dates)
5. **Vendor Ratings** - Performance evaluation of vendors (quality, delivery time, etc.)
6. **Users** - People who use the system (admins, procurement staff, etc.)
7. **Departments** - Organizational units (IT, Finance, etc.)

### Real-World Example:
Imagine you work at a company that needs office supplies. Instead of calling suppliers manually:
- You create a "vendor request" for "Office Depot"
- Manager approves it
- System creates the vendor
- You create a contract with them
- You place purchase orders
- You rate their performance

---

## Technology Stack Explained

### 1. **Java 17**
**What it is**: Programming language
**Why we use it**: 
- Enterprise standard
- Strong typing (catches errors early)
- Object-oriented (organizes code well)
- Long-term support version

### 2. **Spring Boot 3.5.7**
**What it is**: Framework that makes building Java applications easier
**Why we use it**:
- **Auto-configuration**: Sets up database, web server, etc. automatically
- **Dependency Injection**: Manages object creation (you don't write `new` everywhere)
- **Embedded Server**: Runs web server inside the app (no need for separate Tomcat)
- **Production Ready**: Built-in security, monitoring, error handling

**Key Concept**: Spring Boot is like a "starter kit" - it gives you everything you need to build a web application quickly.

### 3. **Spring Data JPA**
**What it is**: Makes database operations easier
**Why we use it**:
- **Repository Pattern**: Instead of writing SQL, you write methods like `findById()`
- **Automatic Queries**: Spring generates SQL from method names
- **Transaction Management**: Handles database transactions automatically

**Example**:
```java
// Instead of writing SQL:
// SELECT * FROM vendors WHERE id = 1

// You write:
vendorRepository.findById(1L);
```

### 4. **Hibernate (JPA Implementation)**
**What it is**: ORM (Object-Relational Mapping) tool
**Why we use it**:
- Converts Java objects to database rows automatically
- Handles relationships (one vendor has many contracts)
- Manages database schema

**Key Concept**: You work with Java objects, Hibernate converts them to/from database tables.

### 5. **H2 Database (Development)**
**What it is**: In-memory database (data stored in RAM)
**Why we use it**:
- No installation needed
- Perfect for development/testing
- Fast
- **Note**: Data is lost when app stops (that's OK for development)

### 6. **PostgreSQL (Production)**
**What it is**: Real database that stores data permanently
**Why we use it**:
- Production-ready
- Handles large amounts of data
- ACID compliant (data integrity)
- Open source

### 7. **Flyway**
**What it is**: Database migration tool
**Why we use it**:
- Version controls database schema
- Automatically runs SQL scripts on startup
- Tracks which migrations have run
- Prevents schema drift

**Example**: When you add a new field to Vendor, Flyway runs a SQL script to add that column.

### 8. **Lombok**
**What it is**: Code generation library
**Why we use it**:
- Reduces boilerplate code
- `@Getter` `@Setter` - auto-generates getters/setters
- `@NoArgsConstructor` - auto-generates constructors
- Makes code cleaner

**Example**:
```java
// Without Lombok (verbose):
private String name;
public String getName() { return name; }
public void setName(String name) { this.name = name; }

// With Lombok (clean):
@Getter @Setter
private String name;
```

### 9. **Jakarta Validation**
**What it is**: Validation framework
**Why we use it**:
- Validates data before processing
- `@NotNull`, `@Email`, `@Size` annotations
- Automatic validation in controllers

**Example**:
```java
@Email
@NotBlank
private String email; // Automatically validates email format
```

### 10. **Gradle**
**What it is**: Build tool (like Maven, but more modern)
**Why we use it**:
- Manages dependencies (downloads libraries)
- Compiles code
- Runs tests
- Packages application into JAR file

---

## Architecture Overview

### The 3-Layer Architecture

```
┌─────────────────────────────────────────┐
│   PRESENTATION LAYER                     │
│   (What users interact with)             │
│   - Controllers (REST endpoints)         │
│   - Exception Handling                   │
├─────────────────────────────────────────┤
│   APPLICATION LAYER                      │
│   (Orchestrates business logic)          │
│   - Application Services                 │
│   - DTOs (Data Transfer Objects)        │
│   - Mappers                              │
├─────────────────────────────────────────┤
│   DOMAIN LAYER                           │
│   (Core business logic)                  │
│   - Entities (Vendor, User, etc.)      │
│   - Repositories (Database access)      │
│   - Domain Services                     │
│   - Value Objects                        │
└─────────────────────────────────────────┘
```

### Why This Architecture?

**Separation of Concerns**: Each layer has one job
- Presentation: Handle HTTP
- Application: Coordinate workflows
- Domain: Business rules

**Benefits**:
1. **Easy to Test**: Test each layer independently
2. **Easy to Change**: Change database without changing controllers
3. **Easy to Understand**: Clear structure
4. **Scalable**: Can add new features without breaking existing code

---

## How Everything Connects

### Request Flow Example: Creating a Vendor

```
1. HTTP Request
   ↓
   POST /api/vendors
   Body: {"companyName": "Tech Corp", "email": "..."}
   ↓
2. VendorController (Presentation Layer)
   - Receives HTTP request
   - Validates JSON format
   - Calls Application Service
   ↓
3. VendorApplicationService (Application Layer)
   - Validates business rules
   - Loads related entities (Category)
   - Calls Mapper to convert DTO → Entity
   - Saves to database via Repository
   - Calls Mapper to convert Entity → Response DTO
   ↓
4. VendorRepository (Domain Layer)
   - Saves Vendor entity to database
   - Uses Hibernate/JPA to generate SQL
   ↓
5. Database (H2 or PostgreSQL)
   - Stores data
   ↓
6. Response flows back up
   Entity → DTO → JSON → HTTP Response
```

### Code Flow Example:

```java
// 1. Controller receives request
@PostMapping
public ResponseEntity<VendorResponse> createVendor(
    @Valid @RequestBody CreateVendorRequest request) {
    
    // 2. Delegates to Application Service
    VendorResponse response = vendorApplicationService.createVendor(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

// 3. Application Service orchestrates
public VendorResponse createVendor(CreateVendorRequest request) {
    // Load category
    VendorCategory category = categoryRepository.findById(request.getCategoryId());
    
    // Convert DTO to Entity
    Vendor vendor = VendorMapper.toEntity(request, category);
    
    // Save to database
    Vendor saved = vendorRepository.save(vendor);
    
    // Convert Entity to Response DTO
    return VendorMapper.toResponse(saved);
}

// 4. Repository (auto-generated by Spring Data JPA)
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    // Spring automatically provides: save(), findById(), findAll(), etc.
}
```

---

## Code Walkthrough

### 1. Entity (Domain Layer)

**File**: `src/main/java/.../domain/entity/Vendor.java`

```java
@Entity  // Tells Hibernate this is a database table
@Table(name = "vendors")  // Table name
@Getter @Setter  // Lombok generates getters/setters
public class Vendor {
    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;
    
    @Column(name = "company_name", nullable = false)  // Database column
    private String companyName;
    
    @Embedded  // Value object (Email is stored in same table)
    private Email email;
    
    @ManyToOne  // Relationship: Many vendors → One category
    @JoinColumn(name = "category_id")
    private VendorCategory category;
    
    // Business logic methods
    public void activate() {
        this.status = VendorStatus.ACTIVE;
    }
}
```

**Key Points**:
- `@Entity` = Database table
- `@Id` = Primary key
- `@ManyToOne` = Relationship (many vendors belong to one category)
- Business methods (like `activate()`) = Domain logic

### 2. Repository (Domain Layer)

**File**: `src/main/java/.../domain/repository/VendorRepository.java`

```java
@Repository  // Spring component
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    // Spring Data JPA automatically provides:
    // - save(Vendor) → INSERT or UPDATE
    // - findById(Long) → SELECT WHERE id = ?
    // - findAll() → SELECT * FROM vendors
    // - deleteById(Long) → DELETE WHERE id = ?
    
    // Custom query (Spring generates SQL from method name)
    List<Vendor> findByCompanyName(String name);
    
    // Custom query with @Query annotation
    @Query("SELECT v FROM Vendor v WHERE v.status = :status")
    List<Vendor> findByStatus(@Param("status") VendorStatus status);
}
```

**Key Points**:
- Interface only (no implementation needed!)
- Spring Data JPA generates implementation automatically
- Method names = SQL queries
- `JpaRepository<Vendor, Long>` = CRUD operations for Vendor with Long ID

### 3. DTO (Application Layer)

**File**: `src/main/java/.../application/dto/vendor/CreateVendorRequest.java`

```java
@Getter @Setter  // Lombok
public class CreateVendorRequest {
    @NotBlank  // Validation: cannot be empty
    @Size(max = 255)  // Validation: max length
    private String companyName;
    
    @Email  // Validation: must be valid email
    private String email;
    
    private Long categoryId;  // Reference to category
}
```

**Key Points**:
- DTO = Data Transfer Object (data structure for API)
- Separate from Entity (allows API to evolve independently)
- Validation annotations (`@NotBlank`, `@Email`)
- No business logic (just data)

### 4. Mapper (Application Layer)

**File**: `src/main/java/.../application/mapper/VendorMapper.java`

```java
public final class VendorMapper {
    // Convert DTO → Entity
    public static Vendor toEntity(CreateVendorRequest request, VendorCategory category) {
        Vendor vendor = new Vendor();
        vendor.setCompanyName(request.getCompanyName());
        vendor.setEmail(new Email(request.getEmail()));  // Convert String → Email value object
        vendor.setCategory(category);
        return vendor;
    }
    
    // Convert Entity → Response DTO
    public static VendorResponse toResponse(Vendor vendor) {
        return VendorResponse.builder()
            .id(vendor.getId())
            .companyName(vendor.getCompanyName())
            .email(vendor.getEmail().getValue())  // Extract value from Email object
            .categoryId(vendor.getCategory().getId())
            .build();
    }
}
```

**Key Points**:
- Static utility class (no dependencies)
- Converts between layers (DTO ↔ Entity)
- Handles value objects (Email, Address)
- Denormalizes data for responses (includes category name, not just ID)

### 5. Application Service (Application Layer)

**File**: `src/main/java/.../application/service/VendorApplicationService.java`

```java
@Service  // Spring component
@Transactional  // All methods run in database transaction
public class VendorApplicationService {
    private final VendorRepository vendorRepository;
    private final VendorCategoryRepository categoryRepository;
    
    // Constructor injection (Spring provides dependencies)
    public VendorApplicationService(VendorRepository vendorRepository, ...) {
        this.vendorRepository = vendorRepository;
        // ...
    }
    
    public VendorResponse createVendor(CreateVendorRequest request) {
        // 1. Load related entity
        VendorCategory category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
        
        // 2. Convert DTO to Entity
        Vendor vendor = VendorMapper.toEntity(request, category);
        
        // 3. Generate vendor code (business logic)
        vendor.setVendorCode(generateVendorCode(request.getCompanyName()));
        
        // 4. Save to database
        Vendor saved = vendorRepository.save(vendor);
        
        // 5. Convert Entity to Response DTO
        return VendorMapper.toResponse(saved);
    }
}
```

**Key Points**:
- `@Service` = Spring component (auto-detected)
- `@Transactional` = Database transaction (all or nothing)
- Constructor injection = Dependencies provided by Spring
- Orchestrates workflow (load → convert → save → convert)

### 6. Controller (Presentation Layer)

**File**: `src/main/java/.../presentation/controller/VendorController.java`

```java
@RestController  // Spring component + handles HTTP
@RequestMapping("/api/vendors")  // Base URL
@Validated  // Enable validation
public class VendorController {
    private final VendorApplicationService vendorApplicationService;
    
    // Constructor injection
    public VendorController(VendorApplicationService vendorApplicationService) {
        this.vendorApplicationService = vendorApplicationService;
    }
    
    @PostMapping  // HTTP POST
    public ResponseEntity<VendorResponse> createVendor(
        @Valid @RequestBody CreateVendorRequest request) {  // @Valid triggers validation
        
        VendorResponse response = vendorApplicationService.createVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")  // HTTP GET /api/vendors/1
    public VendorResponse getVendor(@PathVariable Long id) {
        return vendorApplicationService.getVendor(id);
    }
}
```

**Key Points**:
- `@RestController` = Handles HTTP requests/responses
- `@RequestMapping` = Base URL for all methods
- `@PostMapping`, `@GetMapping` = HTTP methods
- `@Valid` = Triggers validation on request
- `@PathVariable` = URL parameter (`/vendors/1` → id = 1)
- `@RequestBody` = JSON body → Java object
- Returns appropriate HTTP status codes

---

## API Design

### RESTful Principles

**REST** = Representational State Transfer
- Uses HTTP methods (GET, POST, PUT, DELETE)
- Stateless (each request is independent)
- Resource-based URLs

### URL Patterns

```
GET    /api/vendors           → List all vendors
GET    /api/vendors/{id}      → Get one vendor
POST   /api/vendors           → Create vendor
PUT    /api/vendors/{id}      → Update vendor
DELETE /api/vendors/{id}      → Delete vendor (if implemented)

POST   /api/vendors/{id}/activate  → Action (not a resource)
```

### HTTP Status Codes

- `200 OK` - Success (GET, PUT)
- `201 Created` - Resource created (POST)
- `204 No Content` - Success, no body (DELETE, actions)
- `400 Bad Request` - Invalid input
- `404 Not Found` - Resource doesn't exist
- `500 Internal Server Error` - Server error

### Request/Response Format

**Request** (POST /api/vendors):
```json
{
  "companyName": "Tech Corp",
  "email": "contact@techcorp.com",
  "categoryId": 1
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "vendorCode": "TECHCORP-AA1234",
  "companyName": "Tech Corp",
  "email": "contact@techcorp.com",
  "status": "PENDING_CREATION",
  "categoryId": 1,
  "categoryName": "IT Services",
  "createdAt": "2024-01-15T10:30:00"
}
```

---

## Database Design

### Entity Relationships

```
Department (1) ──→ (Many) User
VendorCategory (1) ──→ (Many) Vendor
Vendor (1) ──→ (Many) Contract
Vendor (1) ──→ (Many) PurchaseOrder
Vendor (1) ──→ (Many) VendorRating
VendorPerformanceCriteria (1) ──→ (Many) VendorRating
```

### Database Tables

**vendors**:
- id (Primary Key)
- vendor_code
- company_name
- email_value (from Email value object)
- category_id (Foreign Key → vendor_categories)
- status
- created_at, updated_at

**contracts**:
- id (Primary Key)
- vendor_id (Foreign Key → vendors)
- contract_number
- title
- start_date, end_date
- created_by_user_id (Foreign Key → users)

**purchase_orders**:
- id (Primary Key)
- vendor_id (Foreign Key → vendors)
- po_number
- total_amount
- status (DRAFT, PENDING_APPROVAL, APPROVED, SENT, RECEIVED)
- created_by_user_id (Foreign Key → users)

### Flyway Migrations

**File**: `src/main/resources/db/migration/V1__initial_schema.sql`

- Creates all tables
- Defines relationships (Foreign Keys)
- Sets up indexes
- Runs automatically on startup

---

## Key Concepts

### 1. Dependency Injection (DI)

**What**: Spring creates objects and provides dependencies automatically

**Example**:
```java
// Without DI (manual):
VendorRepository repo = new VendorRepository();
VendorService service = new VendorService(repo);

// With DI (Spring):
@Service
public class VendorService {
    private final VendorRepository repo;
    
    // Spring automatically provides VendorRepository
    public VendorService(VendorRepository repo) {
        this.repo = repo;
    }
}
```

**Benefits**: 
- Loose coupling
- Easy to test (can inject mocks)
- Spring manages object lifecycle

### 2. Value Objects

**What**: Immutable objects representing domain concepts

**Example**: `Email` class
- Validates email format
- Encapsulates business rules
- Immutable (can't change after creation)

**Why**: 
- Type safety (String vs Email)
- Validation in one place
- Business rules enforced

### 3. DTOs vs Entities

**Entity**: 
- Represents database table
- Contains business logic
- Used in domain layer

**DTO**: 
- Represents API contract
- No business logic
- Used in presentation/application layers

**Why Separate**:
- API can evolve independently
- Don't expose internal structure
- Can combine data from multiple entities

### 4. Transactions

**What**: All database operations succeed or fail together

**Example**:
```java
@Transactional
public void createVendorAndContract(...) {
    vendorRepository.save(vendor);      // Operation 1
    contractRepository.save(contract);  // Operation 2
    // If either fails, both are rolled back
}
```

**Benefits**: Data consistency

### 5. Validation

**Where**: 
- DTOs (using `@NotBlank`, `@Email`, etc.)
- Controllers (using `@Valid`)

**Flow**:
1. Request arrives
2. `@Valid` triggers validation
3. If invalid → 400 Bad Request
4. If valid → Process request

---

## Summary

### The Big Picture

1. **User sends HTTP request** → Controller receives it
2. **Controller validates** → Calls Application Service
3. **Application Service orchestrates** → Loads entities, converts DTOs, saves data
4. **Repository saves** → Hibernate generates SQL
5. **Database stores** → Data persisted
6. **Response flows back** → Entity → DTO → JSON → HTTP Response

### Key Technologies

- **Spring Boot**: Framework (handles HTTP, database, configuration)
- **JPA/Hibernate**: ORM (Java objects ↔ Database)
- **Spring Data JPA**: Repository pattern (easy database access)
- **Lombok**: Code generation (reduces boilerplate)
- **Jakarta Validation**: Input validation

### Architecture Benefits

- **Separation of Concerns**: Each layer has one job
- **Testability**: Test layers independently
- **Maintainability**: Easy to find and fix bugs
- **Scalability**: Easy to add features

---

## Next Steps

1. **Read the code**: Start with a simple endpoint (like GET /api/vendors)
2. **Trace the flow**: Follow the request from Controller → Service → Repository
3. **Modify something**: Try adding a new field to Vendor
4. **Test it**: Use Postman to test your changes
5. **Read Spring Boot docs**: Understand annotations better

You now understand the project! 🎉

