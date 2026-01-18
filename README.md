 Vendor Management System (VMS)

A comprehensive enterprise-grade Vendor Management System built with Spring Boot, designed to manage vendor relationships, contracts, purchase orders, and vendor approval workflows.

Table of Contents

- [What This Project Does](what-this-project-does)
- [Features](features)
- [Technology Stack](technology-stack)
- [Architecture](architecture)
- [Prerequisites](prerequisites)
- [Installation & Setup](installation--setup)
- [Running the Application](running-the-application)
- [API Documentation](api-documentation)
- [Project Structure](project-structure)
- [Configuration](configuration)
- [Testing](testing)
- [Database](database)
- [Development](development)
- [Production Deployment](production-deployment)
- [Contributing](contributing)
- [License](license)


 What This Project Does

The Vendor Management System is a backend application that helps organizations manage their relationships with suppliers and vendors. It provides a complete workflow for:

- Vendor Management: Create, update, and manage vendor information
- Vendor Approval Workflow: Request → Submit → Approve/Reject new vendors
- Contract Management: Create and manage contracts with vendors
- Purchase Order Management: Create, approve, and track purchase orders
- Vendor Performance: Rate and evaluate vendor performance
- User Management: Manage system users and departments
- Dashboard: View system summaries and statistics

 Real-World Use Case

Imagine a company that needs to manage multiple suppliers:
1. An employee creates a request to add a new vendor
2. The request goes through an approval workflow
3. Once approved, the vendor is added to the system
4. Contracts are created with the vendor
5. Purchase orders are placed and tracked
6. Vendor performance is rated and monitored

This system automates and manages all these processes.


Features

 Core Features

- Vendor Management: CRUD operations for vendors with status management (Active, Suspended, Terminated)
- Vendor Creation Requests: Complete approval workflow (Draft → Submitted → Approved/Rejected)
- Contract Management: Create contracts, approve, and terminate
- Purchase Order Management: Full lifecycle (Draft → Pending Approval → Approved → Sent → Received)
- Vendor Ratings: Rate vendors based on performance criteria
- Performance Criteria: Define and manage evaluation criteria
- Category Management: Organize vendors by categories
- User Management: Manage users, departments, and roles
- Dashboard: System-wide statistics and summaries

 Technical Features
- RESTful API design
- Input validation
- Error handling
- Database migrations (Flyway)
- Transaction management
- Layered architecture (Clean Architecture)
- Value objects for type safety
- Automatic code generation (vendor codes, PO numbers)


Technology Stack

 Core Framework
- Java 17 - Programming language (LTS version)
- Spring Boot 3.5.7 - Application framework
- Spring Data JPA - Database access layer
- Hibernate - ORM (Object-Relational Mapping)

 Database
- H2 Database - In-memory database (development/testing)
- PostgreSQL - Production database
- Flyway - Database migration tool

 Libraries & Tools
- Lombok - Reduces boilerplate code
- Jakarta Validation - Input validation framework
- Gradle - Build automation tool
- Spring Boot DevTools - Development utilities

 Development Tools
- Spring Boot DevTools - Hot reload
- Gradle Wrapper - Consistent builds


 Architecture

The system follows a 3-layer Clean Architecture pattern:

┌─────────────────────────────────────────┐
│   PRESENTATION LAYER                    │
│   - REST Controllers                    │
│   - Exception Handlers                  │
│   - HTTP Request/Response handling      │
├─────────────────────────────────────────┤
│   APPLICATION LAYER                     │
│   - Application Services                │
│   - DTOs (Data Transfer Objects)        │
│   - Mappers (Entity ↔ DTO)              │
├─────────────────────────────────────────┤
│   DOMAIN LAYER                          │
│   - Entities (Business Models)          │
│   - Repositories (Data Access)          │
│   - Domain Services                     │
│   - Value Objects                       │
└─────────────────────────────────────────┘

 Layer Responsibilities

Presentation Layer (presentation):
- Handles HTTP requests/responses
- Validates input
- Maps HTTP to application layer

Application Layer (application):
- Orchestrates business workflows
- Manages transactions
- Converts between DTOs and entities

Domain Layer (domain):
- Contains business logic
- Defines entities and relationships
- Data access interfaces

 Benefits
- Separation of Concerns: Each layer has a single responsibility
- Testability: Layers can be tested independently
- Maintainabilit: Easy to locate and fix issues
- Scalability: Easy to add new features
- Flexibility: Can change database or API without affecting other layers

 
Prerequisites

Before you begin, ensure you have the following installed:

- Java 17 or higher - [Download Java](https://adoptium.net/)
- Gradle 8.x (or use Gradle Wrapper included in project)
- PostgreSQL 12+ (optional, for production; H2 is used by default)
- Git (for cloning the repository)

Verify Installation

```bash
# Check Java version
java -version  # Should show Java 17 or higher

# Check Gradle (if installed)
gradle -v

# Or use the wrapper
./gradlew -v


## Installation & Setup

 1. Clone the Repository

```bash
git clone <repository-url>
cd Clear-Chain
```

 2. Set Up Java Environment

```bash
# Set Java 21 (adjust path as needed)
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH

# Verify Java version
java -version  # Should show Java 21 or higher
```

 3. Build the Backend

```bash
# Using Gradle Wrapper (recommended)
./gradlew build

# Or using installed Gradle
gradle build
```

This will:
- Download dependencies
- Compile the code
- Run tests
- Create the JAR file

 4. Set Up the Frontend

```bash
cd frontend

# Install dependencies (first time only)
npm install

# Start frontend development server
npm run dev
```

The frontend will be available at **http://localhost:3000**

 5. Configure Database (Optional)

By default, the application uses **H2 in-memory database** (no setup needed).

For **PostgreSQL** (production):

1. Create a PostgreSQL database:
```sql
CREATE DATABASE vendor_db;
```

2. Set environment variables:
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/vendor_db
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
export SPRING_DATASOURCE_DRIVER=org.postgresql.Driver
export SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
```

Or edit `src/main/resources/application.properties` directly.



 Quick Start Guide

 First-Time Setup

IMPORTANT: The system uses bootstrap registration - no preset users exist!

1. Start the Backend:
   ```bash
   export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
   ./gradlew bootRun --no-daemon
   ```
   Wait for: `Started VendorManagementSystemApplication in X.XXX seconds`

2. Start the Frontend (in a new terminal):
   ```bash
   cd frontend
   npm install  # First time only
   npm run dev
   ```
   Wait for: `✓ Ready` and `Local: http://localhost:3000`

3. Create the First Admin:
   - Open browser: `http://localhost:3000`
   - Click "Create the first admin account" link on the login page
   - Fill in the registration form (username, name, email, password)
   - Submit and login with your new credentials

4. After Login:
   - Admin can create additional users from the Users page (`/users`)
   - Admin can manage all system features

 Running the Application

 Development Mode

Backend:
```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew bootRun --no-daemon
```

The backend API will start on **http://localhost:8080**

Frontend:
```bash
cd frontend
npm run dev
```

The frontend will start on **http://localhost:3000**

 Production Mode

```bash
# Build the JAR
./gradlew build

# Run the JAR
java -jar build/libs/vendor-management-system-0.0.1-SNAPSHOT.jar
```

 Verify It's Running

Open your browser or use curl:

```bash
curl http://localhost:8080/api/dashboard
```

You should see a JSON response with dashboard data.

---

 API Documentation

 Base URL
```
http://localhost:8080/api
```

 Main Endpoints

 Vendors
- `GET /api/vendors` - List vendors
- `GET /api/vendors/{id}` - Get vendor by ID
- `POST /api/vendors` - Create vendor
- `PUT /api/vendors/{id}` - Update vendor
- `POST /api/vendors/{id}/activate` - Activate vendor
- `POST /api/vendors/{id}/suspend` - Suspend vendor
- `POST /api/vendors/{id}/terminate` - Terminate vendor

 Purchase Orders
- `GET /api/purchase-orders/{id}` - Get purchase order
- `GET /api/purchase-orders/vendor/{vendorId}` - Get orders for vendor
- `POST /api/purchase-orders` - Create purchase order
- `POST /api/purchase-orders/{id}/submit` - Submit for approval
- `POST /api/purchase-orders/{id}/approve?approverId={id}` - Approve
- `POST /api/purchase-orders/{id}/reject?approverId={id}&rejectionReason={reason}` - Reject
- `POST /api/purchase-orders/{id}/send` - Send to vendor
- `POST /api/purchase-orders/{id}/receive` - Mark as received

 Contracts
- `GET /api/contracts/{id}` - Get contract
- `GET /api/contracts/vendor/{vendorId}` - Get contracts for vendor
- `POST /api/contracts` - Create contract
- `POST /api/contracts/{id}/approve?approverId={id}` - Approve contract
- `POST /api/contracts/{id}/terminate` - Terminate contract

 Vendor Requests
- `GET /api/vendor-requests/pending` - Get pending requests
- `GET /api/vendor-requests/{id}` - Get request
- `POST /api/vendor-requests` - Create request
- `POST /api/vendor-requests/{id}/submit` - Submit for approval
- `POST /api/vendor-requests/{id}/approve` - Approve request
- `POST /api/vendor-requests/{id}/reject` - Reject request

 Users
- `GET /api/users` - List active users
- `GET /api/users/{id}` - Get user
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `POST /api/users/{id}/activate` - Activate user
- `POST /api/users/{id}/deactivate` - Deactivate user

 Departments
- `GET /api/departments` - List all departments
- `GET /api/departments/active` - List active departments
- `POST /api/departments` - Create department

 Dashboard
- `GET /api/dashboard` - Get system summary

 Authentication

The system supports two authentication methods:

1. Username/Password Login (Default)
   - Use the credentials you created during bootstrap registration
   - Admin users can create additional users from the Users page

2. OAuth 2.0 Login (Optional - Requires Setup)
   - Sign in with Google or GitHub
   - Only administrators need to configure OAuth credentials
   - End users simply click "Sign in with Google/GitHub"
   - See `OAUTH_SETUP.md` for configuration instructions (optional)



 Project Structure

```
Clear-Chain/
├── src/                              # Backend (Spring Boot)
│   ├── main/
│   │   ├── java/com/vms/vendor_management_system/
│   │   │   ├── application/          # Application Layer
│   │   │   │   ├── dto/              # Data Transfer Objects
│   │   │   │   ├── mapper/           # Entity ↔ DTO converters
│   │   │   │   └── service/          # Application Services
│   │   │   ├── config/               # Configuration classes
│   │   │   ├── domain/               # Domain Layer
│   │   │   │   ├── entity/           # Business entities
│   │   │   │   ├── repository/      # Data access interfaces
│   │   │   │   ├── service/          # Domain services
│   │   │   │   └── valueobjects/    # Value objects
│   │   │   ├── presentation/         # Presentation Layer
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   └── exception/       # Exception handlers
│   │   │   └── VendorManagementSystemApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/        # Flyway migrations
│   └── test/                         # Test files
├── frontend/                         # Frontend (Next.js + React)
│   ├── app/                          # Next.js app directory
│   ├── components/                   # React components
│   ├── lib/                          # Utility functions
│   └── package.json
├── build.gradle                      # Build configuration
├── gradlew                          # Gradle wrapper
├── README.md                        # This file
├── ARCHITECTURE.md                  # Architecture documentation
└── LEARNING_GUIDE.md                # Learning resources
```



 Configuration

 Application Properties

Main configuration file: `src/main/resources/application.properties`

Key Settings:

```properties
 Database (default: H2 in-memory)
spring.datasource.url=jdbc:h2:mem:vendor_db
spring.datasource.username=sa
spring.datasource.password=

 JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

 Application
spring.application.name=vendor-management-system
```

 Environment Variables

Override settings using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/vendor_db
export SPRING_DATASOURCE_USERNAME=myuser
export SPRING_DATASOURCE_PASSWORD=mypassword
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
```

 File Upload Configuration

File uploads are stored in the `uploads/` directory (created automatically).

```properties
 File upload settings
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
app.upload.dir=uploads
```



 Testing

 Run Tests

```bash
./gradlew test
```

 Manual Testing

Use Postman or curl to test endpoints:

```bash
# Create a department
curl -X POST http://localhost:8080/api/departments \
  -H "Content-Type: application/json" \
  -d '{"name":"IT Department","code":"IT-DEPT"}'

# Create a user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john","email":"john@example.com","firstName":"John","lastName":"Doe","role":"ADMIN","departmentId":1}'
```

Use Postman or curl to test the API endpoints.



 Database

 Development (H2)

- Type: In-memory database
- Data: Lost when application stops
- Setup: No setup required
- Access: http://localhost:8080/h2-console (if enabled)

 Production (PostgreSQL)

- Type: Persistent database
- Data: Persisted to disk
- Setup: Create database and configure connection
- Migrations: Automatically run by Flyway on startup

 Database Migrations

Migrations are located in `src/main/resources/db/migration/`

- `V1__initial_schema.sql` - Creates all tables
- Flyway runs migrations automatically on startup
- Never modify existing migrations (create new ones)



 Development

 Hot Reload

Spring Boot DevTools enables hot reload:

1. Make code changes
2. Save file
3. Application automatically restarts (if using `bootRun`)

 Adding New Features

1. Add Entity in `domain/entity/`
2. Add Repository in `domain/repository/`
3. Add DTOs in `application/dto/`
4. Add Mapper in `application/mapper/`
5. Add Service in `application/service/`
6. Add Controller in `presentation/controller/`

 Code Style

- Follow Java naming conventions
- Use meaningful names
- Add JavaDoc comments for public methods
- Keep methods small and focused



 Production Deployment

 Build for Production

```bash
./gradlew clean build
```

 Run with Production Database

```bash
# Set environment variables
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/vendor_db
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export SPRING_JPA_SHOW_SQL=false
export SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect

# Run JAR
java -jar build/libs/vendor-management-system-0.0.1-SNAPSHOT.jar
```

 Docker (Optional)

Create `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/vendor-management-system-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:

```bash
docker build -t vendor-management-system .
docker run -p 8080:8080 vendor-management-system
```



 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request



 Important Notes

 Bootstrap Registration
- No preset users - The system starts with an empty database
- First admin creates their account - Secure first-time setup via `/register` page
- After first admin - Only admins can create additional users from the Users page

 Vendor Request Workflow
The vendor approval workflow follows this sequence:
1. Draft → Requester creates vendor request
2. PENDING_COMPLIANCE_REVIEW → Compliance approver reviews
3. PENDING_FINANCE_REVIEW → Finance approver adds banking details and reviews
4. PENDING_ADMIN_REVIEW → Admin provides final approval
5. ACTIVE → Vendor is created and ready for contracts

 Currency Handling
- Currency is selected by the requester when creating a vendor request
- Currency is automatically used when creating contracts or purchase orders
- Currency is displayed throughout the system (contracts, purchase orders, dashboard)

 Learning Resources

- [LEARNING_GUIDE.md](LEARNING_GUIDE.md) - Complete learning guide explaining the project
- [ARCHITECTURE.md](ARCHITECTURE.md) - Detailed architecture documentation



 License

This project is licensed under the MIT License - see the LICENSE file for details.



 Troubleshooting

 Backend Won't Start
- Verify Java 21 is installed: `java -version`
- Set JAVA_HOME: `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk`
- Check port 8080 is available

 Frontend Won't Start
- Verify Node.js 18+ is installed: `node --version`
- Install dependencies: `cd frontend && npm install`
- Check port 3000 is available

 Database Issues (H2)
- H2 is in-memory - data is lost when backend stops (this is expected)
- Restart backend to reset database
- Migrations run automatically on startup

 Cannot Create Admin Account
- Ensure backend is running
- Check that no users exist (bootstrap only works when database is empty)
- Clear browser cache and try again

 Support

For questions or issues:
1. Check the documentation files
2. Review the learning guide and architecture docs
3. Check troubleshooting section above
4. Open an issue on GitHub



 Acknowledgments

- Built with Spring Boot
- Uses Clean Architecture principles
- Follows RESTful API design


