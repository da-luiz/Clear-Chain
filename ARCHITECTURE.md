# Vendor Management System - Architecture Documentation

## Project Overview

The Vendor Management System is a comprehensive enterprise application designed to manage vendor relationships, contracts, purchase orders, and vendor approval workflows. The system follows a **layered architecture** (also known as Clean Architecture or Hexagonal Architecture) to ensure separation of concerns, maintainability, and testability.

---

## Technology Stack

### Core Framework
- **Spring Boot 3.5.7** - Modern Java framework providing auto-configuration, dependency injection, and production-ready features
- **Java 17** - Latest LTS version providing modern language features and performance improvements

### Data Layer
- **Spring Data JPA** - Simplifies database access with repository pattern
- **Hibernate** - JPA implementation for ORM (Object-Relational Mapping)
- **PostgreSQL** - Production database (primary)
- **H2 Database** - In-memory database for development and testing
- **Flyway** - Database migration tool for version-controlled schema management

### Validation & Utilities
- **Jakarta Validation API 3.0.2** - Bean validation framework
- **Lombok** - Reduces boilerplate code with annotations

### Development Tools
- **Spring Boot DevTools** - Hot reload and development utilities
- **Gradle** - Build automation and dependency management

---

## Architectural Layers (Branches)

The system is organized into **three primary architectural layers**, each with distinct responsibilities:

```
┌─────────────────────────────────────────┐
│     PRESENTATION LAYER                   │
│     (REST Controllers, DTOs)             │
├─────────────────────────────────────────┤
│     APPLICATION LAYER                    │
│     (Use Cases, Application Services)    │
├─────────────────────────────────────────┤
│     DOMAIN LAYER                         │
│     (Entities, Business Logic, Repos)   │
└─────────────────────────────────────────┘
```

### 1. Presentation Layer (`presentation/`)

**Location**: `com.vms.vendor_management_system.presentation`

**Purpose**: Handles HTTP requests/responses and external API contracts

**Components**:
- **Controllers** (`controller/`): REST endpoints that receive HTTP requests
  - `VendorController` - Vendor CRUD operations
  - `VendorCreationRequestController` - Vendor request workflow
  - `UserController` - User management
  - `ContractController` - Contract management
  - `DashboardController` - Dashboard aggregations
- **Exception Handling** (`exception/`): Centralized error handling via `GlobalExceptionHandler`

**Responsibilities**:
- ✅ Validate incoming HTTP requests
- ✅ Transform HTTP requests to application DTOs
- ✅ Transform application responses to HTTP responses
- ✅ Handle HTTP status codes and error responses
- ✅ Provide RESTful API contracts

**Why This Layer Exists**:
- **Separation of Concerns**: Keeps HTTP-specific logic separate from business logic
- **Flexibility**: Can swap REST for GraphQL, gRPC, or messaging without touching business logic
- **Testability**: Controllers can be tested independently with mock services
- **API Versioning**: Easy to version APIs without affecting core logic

**Key Design Decisions**:
- Uses `@Validated` and `@Valid` for request validation
- Returns appropriate HTTP status codes (201 for creation, 204 for no content)
- Centralized exception handling prevents code duplication

---

### 2. Application Layer (`application/`)

**Location**: `com.vms.vendor_management_system.application`

**Purpose**: Orchestrates use cases and coordinates between presentation and domain layers

**Components**:
- **Application Services** (`service/`): Orchestrate business workflows
  - `VendorApplicationService` - Vendor use cases
  - `VendorCreationRequestApplicationService` - Request workflow orchestration
  - `UserApplicationService` - User management use cases
  - `ContractApplicationService` - Contract use cases
  - `DashboardApplicationService` - Dashboard aggregations
- **DTOs** (`dto/`): Data Transfer Objects for API contracts
  - Request DTOs (e.g., `CreateVendorRequest`)
  - Response DTOs (e.g., `VendorResponse`)
- **Mappers** (`mapper/`): Transform between DTOs and domain entities
  - `VendorMapper`, `UserMapper`, `ContractMapper`, etc.

**Responsibilities**:
- ✅ Orchestrate complex use cases involving multiple domain entities
- ✅ Coordinate transactions across multiple repositories
- ✅ Transform between domain models and DTOs
- ✅ Handle application-level validation
- ✅ Manage transaction boundaries

**Why This Layer Exists**:
- **Use Case Encapsulation**: Each service method represents a complete business use case
- **Transaction Management**: `@Transactional` ensures data consistency
- **DTO Isolation**: Prevents domain entities from leaking to presentation layer
- **Business Workflow**: Coordinates complex workflows (e.g., vendor approval process)

**Key Design Decisions**:
- Services are stateless and transactional
- Mappers are static utility classes (no dependencies, easy to test)
- DTOs are separate from domain entities (allows API evolution independent of domain)
- Application services delegate complex business logic to domain services

**Example Flow**:
```java
// Application Service orchestrates:
1. Validate request
2. Load domain entities from repositories
3. Call domain service for business logic
4. Save entities
5. Map to response DTO
6. Return to controller
```

---

### 3. Domain Layer (`domain/`)

**Location**: `com.vms.vendor_management_system.domain`

**Purpose**: Contains core business logic, entities, and domain rules

**Components**:
- **Entities** (`entity/`): Rich domain models with business behavior
  - `Vendor`, `User`, `Contract`, `PurchaseOrder`, `VendorCreationRequest`, etc.
- **Repositories** (`repository/`): Data access interfaces (Spring Data JPA)
  - `VendorRepository`, `UserRepository`, `ContractRepository`, etc.
- **Domain Services** (`service/`): Complex business logic that doesn't fit in a single entity
  - `VendorManagementService` - Vendor lifecycle management
  - `UserManagementService` - User-related business rules
- **Value Objects** (`valueobjects/`): Immutable objects representing domain concepts
  - `Email` - Validated email with business rules
  - `Address` - Structured address information
- **Enums** (`enums/`): Domain enumerations
  - `VendorStatus`, `RequestStatus`, `UserRole`

**Responsibilities**:
- ✅ Encapsulate business rules and invariants
- ✅ Provide rich domain models with behavior (not just data)
- ✅ Ensure data integrity through entity methods
- ✅ Define domain concepts and relationships

**Why This Layer Exists**:
- **Business Logic Centralization**: All business rules in one place
- **Rich Domain Model**: Entities have behavior, not just getters/setters
- **Testability**: Domain logic can be tested without infrastructure
- **Independence**: Domain layer doesn't depend on frameworks (except JPA annotations)

**Key Design Decisions**:
- **Rich Entities**: Entities contain business methods (e.g., `vendor.activate()`, `vendor.suspend()`)
- **Value Objects**: `Email` and `Address` encapsulate validation and business rules
- **Domain Services**: Complex operations that span multiple entities
- **Repository Pattern**: Abstracts data access, enables easy testing with mocks
- **Enum-Driven State Management**: Status transitions are type-safe

**Example - Rich Domain Model**:
```java
// Vendor entity has behavior, not just data
vendor.activate();  // Business rule: only APPROVED vendors can activate
vendor.suspend();   // Business rule: only ACTIVE vendors can suspend
vendor.getAverageRating(); // Calculated business value
```

---

## How Layers Relate to Each Other

### Dependency Flow (Unidirectional)

```
Presentation → Application → Domain
     ↓              ↓            ↓
  (HTTP)      (Use Cases)   (Business Logic)
```

**Rule**: Dependencies flow **downward only**
- Presentation depends on Application
- Application depends on Domain
- Domain depends on **nothing** (except JPA for persistence)

### Communication Patterns

1. **Presentation → Application**:
   - Controllers call Application Services
   - Pass DTOs (not domain entities)
   - Receive DTOs (not domain entities)

2. **Application → Domain**:
   - Application Services call Domain Services
   - Application Services use Repositories
   - Application Services work with domain entities
   - Mappers convert between DTOs and entities

3. **Domain → Infrastructure** (via Spring):
   - Repositories are interfaces, implemented by Spring Data JPA
   - Entities use JPA annotations for persistence
   - Domain services use repositories

### Example: Creating a Vendor

```
1. HTTP POST /api/vendors
   ↓
2. VendorController.createVendor()
   - Validates request
   - Calls VendorApplicationService
   ↓
3. VendorApplicationService.createVendor()
   - Maps DTO to Entity (via VendorMapper)
   - Generates vendor code
   - Calls VendorRepository.save()
   ↓
4. VendorRepository.save() (Spring Data JPA)
   - Persists to database
   ↓
5. VendorApplicationService
   - Maps Entity to Response DTO
   - Returns to Controller
   ↓
6. VendorController
   - Returns HTTP 201 with response
```

---

## Why This Architecture?

### 1. **Separation of Concerns**
Each layer has a single, well-defined responsibility:
- Presentation = HTTP/API concerns
- Application = Use case orchestration
- Domain = Business logic

### 2. **Maintainability**
- Changes to API don't affect business logic
- Business rules are centralized and easy to find
- Clear boundaries make code easier to understand

### 3. **Testability**
- Controllers can be tested with mock application services
- Application services can be tested with mock repositories
- Domain logic can be tested in isolation

### 4. **Flexibility**
- Can change database without affecting business logic
- Can change API (REST → GraphQL) without touching domain
- Can add new use cases without modifying existing code

### 5. **Scalability**
- Each layer can be scaled independently
- Clear boundaries enable microservices migration if needed
- Domain layer can be reused across different interfaces

### 6. **Domain-Driven Design (DDD) Principles**
- Rich domain models with behavior
- Value objects for domain concepts
- Domain services for complex operations
- Repository pattern for data access

---

## Database Design

### Schema Management
- **Flyway** for version-controlled migrations
- Initial schema in `V1__initial_schema.sql`
- Supports both H2 (dev) and PostgreSQL (production)

### Key Tables
- `vendors` - Core vendor information
- `vendor_creation_requests` - Approval workflow
- `vendor_approvals` - Multi-level approval tracking
- `contracts` - Vendor contracts
- `purchase_orders` - Purchase order management
- `vendor_ratings` - Performance evaluation
- `users` - System users
- `departments` - Organizational structure

### Relationships
- Vendors have many Contracts, Purchase Orders, Ratings
- Vendor Creation Requests link to Vendors (after approval)
- Users belong to Departments
- Multi-level approvals tracked via VendorApproval

---

## Configuration

### Application Properties
- Environment-based configuration (H2 for dev, PostgreSQL for prod)
- JPA/Hibernate settings for SQL logging and formatting
- Connection pooling via HikariCP

### Persistence Configuration
- JPA Auditing enabled for `@CreatedDate` and `@LastModifiedDate`
- Ready for `@CreatedBy` / `@LastModifiedBy` when security is added

---

## Future Enhancements

The architecture is designed to support:
- **Security Layer**: Spring Security integration (auditor provider ready)
- **Caching**: Can add caching at application service level
- **Event Sourcing**: Domain events can be added to domain layer
- **Microservices**: Each layer can be extracted to separate services
- **API Versioning**: Easy to add versioned controllers
- **GraphQL**: Can add GraphQL resolvers alongside REST controllers

---

## Best Practices Followed

1. ✅ **Single Responsibility Principle**: Each class has one reason to change
2. ✅ **Dependency Inversion**: Depend on abstractions (repositories, services)
3. ✅ **Don't Repeat Yourself (DRY)**: Mappers, exception handlers reduce duplication
4. ✅ **Fail Fast**: Validation at multiple layers (presentation, application, domain)
5. ✅ **Immutable Value Objects**: Email and Address are immutable
6. ✅ **Rich Domain Models**: Entities contain business behavior
7. ✅ **Transaction Boundaries**: Properly scoped at application service level
8. ✅ **Error Handling**: Centralized exception handling

---

## Summary

This architecture provides:
- **Clear separation** between API, use cases, and business logic
- **Maintainable codebase** with well-defined boundaries
- **Testable components** at every layer
- **Flexible foundation** for future growth
- **Domain-driven design** principles for business-focused development

The three-layer architecture ensures that changes in one area don't ripple through the entire system, making the codebase robust, maintainable, and ready for enterprise-scale requirements.

