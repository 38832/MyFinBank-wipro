# MyFinBank Viva Preparation Guide

This guide explains the project end to end in a viva-friendly way. Use it in this order: overview, architecture, modules, feature flows, code map, demo script, and practice answers.

## 1. One-Minute Project Explanation

MyFinBank is a Spring Boot microservice banking application. It has separate services for customer operations, admin operations, notifications, service discovery, and centralized configuration. Customers can register, login, deposit, withdraw, transfer money, apply for loans, calculate EMI, view transactions, and chat with support. Admins can login, manage customers, activate or deactivate users, and approve or deny loan applications. The services use H2 in-memory databases, JWT authentication, Spring Security, Eureka service discovery, Spring Cloud Config, REST APIs, and simple HTML/CSS/JavaScript frontends.

## 2. Architecture Summary

| Layer | Component | Purpose |
| --- | --- | --- |
| Parent project | `pom.xml` | Maven multi-module parent for all services |
| Config | `config-server` | Centralized configuration service using Spring Cloud Config |
| Discovery | `eureka-server` | Eureka registry where services register themselves |
| Customer domain | `customer-service` | Customer registration, login, banking transactions, loans, EMI, chat |
| Admin domain | `admin-service` | Admin login, customer management, loan approval or denial |
| Notifications | `notification-service` | Stores notification events, mainly low-balance alerts |
| Frontend | `customer.html`, `admin.html` | Browser screens that call backend REST APIs using JavaScript `fetch()` |

Recommended service startup order:

```text
1. config-server       port 8888
2. eureka-server       port 8761
3. notification-service port 8083
4. customer-service    port 8081
5. admin-service       port 8082
```

Useful URLs:

```text
Customer UI:          http://localhost:8081/customer.html
Admin UI:             http://localhost:8082/admin.html
Eureka UI:            http://localhost:8761
Customer Swagger:     http://localhost:8081/swagger-ui.html
Admin Swagger:        http://localhost:8082/swagger-ui.html
Notification Swagger: http://localhost:8083/swagger-ui.html
```

Default admin login:

```text
Email:    admin@myfinbank.com
Password: admin123
```

## 3. Root Project Files

### `README.md`

This gives the high-level project summary, module list, run order, frontend URLs, and default admin credentials. In a viva, use it to introduce the project quickly.

### `pom.xml`

The root `pom.xml` is a Maven parent project with packaging type `pom`. It defines:

- Java version: `17`
- Spring Boot version: `3.2.4`
- Spring Cloud version: `2023.0.6`
- JWT library version: `0.11.5`
- Modules: `config-server`, `eureka-server`, `customer-service`, `admin-service`, `notification-service`

Viva answer: "The parent POM manages common versions and builds all microservices together as a multi-module Maven project."

### `RUN_APPLICATION_POWERSHELL.md`

This contains the exact commands to run the application. Use it for demo preparation.

## 4. Module Walkthrough

### Config Server

Important file:

- `config-server/src/main/java/com/myfinbank/configserver/ConfigServerApplication.java`

Main idea:

- Uses `@EnableConfigServer`.
- Runs on port `8888`.
- Provides shared configuration through Spring Cloud Config.

Viva answer: "The config server centralizes configuration so microservices can load settings from one place instead of duplicating configuration."

### Eureka Server

Important file:

- `eureka-server/src/main/java/com/myfinbank/eurekaserver/EurekaServerApplication.java`

Main idea:

- Uses `@EnableEurekaServer`.
- Runs on port `8761`.
- Allows services to register and discover each other by service name.

Viva answer: "Eureka is used for service discovery. Instead of hardcoding host and port, services can call names like `customer-service` and `notification-service`."

### Customer Service

Important files:

- `customer-service/src/main/java/com/myfinbank/customerservice/CustomerServiceApplication.java`
- `customer-service/src/main/resources/application.yml`
- `customer-service/src/main/java/com/myfinbank/customerservice/controller/CustomerController.java`
- `customer-service/src/main/java/com/myfinbank/customerservice/service/CustomerService.java`
- `customer-service/src/main/java/com/myfinbank/customerservice/service/NotificationClient.java`
- `customer-service/src/main/java/com/myfinbank/customerservice/security/SecurityConfig.java`

Main responsibilities:

- Customer registration and login
- Deposit, withdraw, and transfer
- Loan application and EMI calculation
- Transaction history
- Customer search and admin-facing customer operations
- Chat messages
- Low-balance notification call to notification service

Database:

- H2 in-memory database named `customerdb`
- Configured in `customer-service/src/main/resources/application.yml`

Entities:

- `Customer`
- `Account`
- `TransactionRecord`
- `LoanApplication`
- `ChatMessage`

DTOs:

- `RegistrationRequest`
- `LoginRequest`
- `AuthResponse`
- `AmountRequest`
- `TransferRequest`
- `LoanRequest`
- `ChatRequest`

### Admin Service

Important files:

- `admin-service/src/main/java/com/myfinbank/adminservice/AdminServiceApplication.java`
- `admin-service/src/main/resources/application.yml`
- `admin-service/src/main/java/com/myfinbank/adminservice/controller/AdminController.java`
- `admin-service/src/main/java/com/myfinbank/adminservice/service/AdminService.java`
- `admin-service/src/main/java/com/myfinbank/adminservice/service/CustomerClient.java`
- `admin-service/src/main/java/com/myfinbank/adminservice/config/DataLoader.java`
- `admin-service/src/main/java/com/myfinbank/adminservice/security/SecurityConfig.java`

Main responsibilities:

- Admin login
- Customer create, search, fetch, update, delete
- Customer activation and deactivation
- Loan approval and denial
- Admin chat message save

Database:

- H2 in-memory database named `admindb`
- Configured in `admin-service/src/main/resources/application.yml`

Entities:

- `AdminUser`
- `CustomerData`
- `AccountData`
- `LoanApplicationData`
- `ChatMessage`

Important viva point:

- `DataLoader` creates the default admin user if no admin exists.
- Admin service calls customer service through `CustomerClient`.

### Notification Service

Important files:

- `notification-service/src/main/java/com/myfinbank/notificationservice/NotificationServiceApplication.java`
- `notification-service/src/main/java/com/myfinbank/notificationservice/controller/NotificationController.java`
- `notification-service/src/main/java/com/myfinbank/notificationservice/entity/NotificationEvent.java`

Main responsibility:

- Receives notification requests and stores them as `NotificationEvent`.

Database:

- H2 in-memory database for notification events.

Main endpoint:

- `POST /api/notifications/email`

## 5. Feature Table

| Feature | Endpoint | Controller Method | Service Method | Main Entities |
| --- | --- | --- | --- | --- |
| Customer register | `POST /api/customers/register` | `CustomerController.register()` | `CustomerService.register()` | `Customer`, `Account` |
| Customer login | `POST /api/customers/login` | `CustomerController.login()` | `CustomerService.login()` | `Customer` |
| Customer logout | `POST /api/customers/logout` | `CustomerController.logout()` | Direct response | None |
| Deposit | `POST /api/customers/deposit` | `CustomerController.deposit()` | `CustomerService.deposit()` | `Account`, `TransactionRecord` |
| Withdraw | `POST /api/customers/withdraw` | `CustomerController.withdraw()` | `CustomerService.withdraw()` | `Account`, `TransactionRecord` |
| Transfer | `POST /api/customers/transfer` | `CustomerController.transfer()` | `CustomerService.transfer()` | `Customer`, `Account`, `TransactionRecord` |
| Apply loan | `POST /api/customers/apply-loan` | `CustomerController.applyLoan()` | `CustomerService.applyLoan()` | `LoanApplication` |
| Calculate EMI | `GET /api/customers/calculate-emi` | `CustomerController.calculateEmi()` | `CustomerService.calculateEmi()` | None |
| Customer chat | `POST /api/customers/chat` | `CustomerController.chat()` | `CustomerService.chat()` | `ChatMessage` |
| Get customer | `GET /api/customers/{id}` | `CustomerController.getCustomer()` | `CustomerService.findCustomerById()` | `Customer` |
| Search customers | `GET /api/customers/search` | `CustomerController.searchCustomers()` | `CustomerService.searchCustomers()` | `Customer` |
| Transactions | `GET /api/customers/{id}/transactions` | `CustomerController.getTransactions()` | `CustomerService.getTransactions()` | `TransactionRecord` |
| Loans | `GET /api/customers/{id}/loans` | `CustomerController.getLoans()` | `CustomerService.getLoans()` | `LoanApplication` |
| Admin login | `POST /api/admin/login` | `AdminController.login()` | `AdminService.login()` | `AdminUser` |
| Admin create customer | `POST /api/admin/customer` | `AdminController.createCustomer()` | `AdminService.createCustomer()` | Remote `Customer`, `Account` |
| Admin search customers | `GET /api/admin/customers` | `AdminController.searchCustomers()` | `AdminService.searchCustomers()` | Remote `Customer` |
| Admin fetch customer | `GET /api/admin/customer/{id}` | `AdminController.getCustomer()` | `AdminService.loadCustomer()` | `CustomerData` |
| Admin update customer | `PUT /api/admin/customer/{id}` | `AdminController.updateCustomer()` | `AdminService.updateCustomer()` | Remote `Customer`, local `CustomerData` |
| Admin delete customer | `DELETE /api/admin/customer/{id}` | `AdminController.deleteCustomer()` | `AdminService.deleteCustomer()` | Remote `Customer` |
| Activate customer | `PUT /api/admin/customer/{id}/activate` | `AdminController.activate()` | `AdminService.activateCustomer()` | Remote `Customer`, local `CustomerData` |
| Deactivate customer | `PUT /api/admin/customer/{id}/deactivate` | `AdminController.deactivate()` | `AdminService.deactivateCustomer()` | Remote `Customer`, local `CustomerData` |
| Approve loan | `PUT /api/admin/loan/{id}/approve` | `AdminController.approveLoan()` | `AdminService.approveLoan()` | Remote `LoanApplication` |
| Deny loan | `PUT /api/admin/loan/{id}/deny` | `AdminController.denyLoan()` | `AdminService.denyLoan()` | Remote `LoanApplication` |
| Send notification | `POST /api/notifications/email` | `NotificationController.sendEmail()` | Direct repository save | `NotificationEvent` |

## 6. Important Request Flows

### Customer Registration

1. Browser submits registration form in `customer.html`.
2. `customer.js` sends `POST /api/customers/register`.
3. `CustomerController.register()` receives `RegistrationRequest`.
4. `CustomerService.register()` checks if email exists.
5. Password is encoded with `BCryptPasswordEncoder`.
6. New `Customer` is saved.
7. New `Account` is created for that customer.
8. Response says registration successful.

Viva line: "Registration creates both the customer record and the linked bank account."

### Customer Login And JWT

1. Browser sends email and password to `POST /api/customers/login`.
2. `CustomerService.login()` authenticates using `AuthenticationManager`.
3. If valid, `JwtUtil.generateToken()` creates a JWT.
4. Frontend stores the token in local storage.
5. Later requests send `Authorization: Bearer <token>`.
6. `JwtAuthenticationFilter` validates the token before protected APIs.

Viva line: "The application is stateless because JWT is sent with each request instead of using server-side sessions."

### Deposit

1. Customer enters customer ID and amount.
2. `customer.js` sends `POST /api/customers/deposit`.
3. `CustomerService.deposit()` finds active customer.
4. It finds the customer account.
5. It adds amount to balance.
6. It saves a `TransactionRecord` with type `DEPOSIT`.

### Withdraw

1. `CustomerService.withdraw()` checks customer and account.
2. If balance is insufficient, it returns an insufficient balance message.
3. Otherwise it subtracts amount.
4. It saves a `TransactionRecord` with type `WITHDRAW`.
5. If balance becomes zero, it calls `NotificationClient.sendLowBalanceAlert()`.
6. Notification client sends `POST /api/notifications/email`.

### Transfer

1. Source customer is found by ID.
2. Target customer is found by email.
3. Source account and target account are loaded.
4. Source balance is checked.
5. Source balance decreases and target balance increases.
6. Two transaction records are saved:
   - `TRANSFER_OUT` for source customer
   - `TRANSFER_IN` for target customer

### Loan Application And Approval

1. Customer applies through `POST /api/customers/apply-loan`.
2. `CustomerService.applyLoan()` saves a `LoanApplication` with pending status.
3. Admin logs in and clicks approve or deny.
4. Admin service endpoint calls `AdminService.approveLoan()` or `AdminService.denyLoan()`.
5. `AdminService` uses `CustomerClient`.
6. `CustomerClient` calls customer service admin loan endpoint.
7. Customer service updates the pending loan status to `APPROVED` or `DENIED`.

### Admin Customer Management

1. Admin logs in using default credentials.
2. `AdminService.login()` returns JWT.
3. Admin frontend stores token in memory.
4. Admin actions call `/api/admin/...`.
5. `AdminService` delegates customer operations to `CustomerClient`.
6. `CustomerClient` uses `RestTemplate` to call customer-service APIs.

## 7. Security And JWT

Customer security file:

- `customer-service/src/main/java/com/myfinbank/customerservice/security/SecurityConfig.java`

Admin security file:

- `admin-service/src/main/java/com/myfinbank/adminservice/security/SecurityConfig.java`

Key points:

- CSRF is disabled because APIs are stateless.
- Session creation policy is `STATELESS`.
- Public routes include login, registration, static frontend files, and Swagger.
- Protected routes require JWT authentication.
- Passwords are encoded using BCrypt.
- `JwtAuthenticationFilter` checks JWT before the standard username-password filter.

Important limitation to know:

- Some customer admin endpoints are currently marked `permitAll()` in customer service so admin service can call them easily. If asked about improvement, say these should ideally be protected using service-to-service authentication or admin JWT forwarding.

## 8. Microservice Communication

Two important client classes:

- `admin-service/src/main/java/com/myfinbank/adminservice/service/CustomerClient.java`
- `customer-service/src/main/java/com/myfinbank/customerservice/service/NotificationClient.java`

How it works:

- Both use `RestTemplate`.
- `RestTemplate` is configured with `@LoadBalanced` in `WebConfig`.
- Service names are used in URLs:
  - `http://customer-service/...`
  - `http://notification-service/...`
- Eureka resolves these names to running service instances.

Viva line: "This project uses synchronous REST communication between microservices through load-balanced RestTemplate and Eureka service discovery."

## 9. Database And JPA

Each service has its own H2 in-memory database:

- Customer service: `customerdb`
- Admin service: `admindb`
- Notification service: notification H2 database

JPA is used through:

- Entity classes with `@Entity`
- Repository interfaces extending Spring Data JPA repositories
- `ddl-auto: update` in `application.yml`

Viva line: "Each microservice owns its own database, which follows the microservice principle of database-per-service."

## 10. Frontend And Backend Integration

Customer frontend:

- `customer-service/src/main/resources/static/customer.html`
- `customer-service/src/main/resources/static/customer.css`
- `customer-service/src/main/resources/static/customer.js`

Admin frontend:

- `admin-service/src/main/resources/static/admin.html`
- `admin-service/src/main/resources/static/admin.css`
- `admin-service/src/main/resources/static/admin.js`

Important points:

- The frontend uses normal HTML forms and buttons.
- JavaScript uses `fetch()` to call backend REST APIs.
- Customer JWT is stored in local storage.
- Admin JWT is stored in the JavaScript variable `adminToken`.
- Token is sent through the `Authorization` header.

## 11. Exception Handling

Important files:

- `customer-service/src/main/java/com/myfinbank/customerservice/exception/GlobalExceptionHandler.java`
- `admin-service/src/main/java/com/myfinbank/adminservice/exception/GlobalExceptionHandler.java`
- `notification-service/src/main/java/com/myfinbank/notificationservice/exception/GlobalExceptionHandler.java`

Main idea:

- `@RestControllerAdvice` handles exceptions globally.
- Bad request errors return HTTP 400 with a message.
- Authentication errors return HTTP 401.
- General errors return HTTP 500.

Viva line: "Global exception handling avoids repeating try-catch blocks in every controller and gives consistent API responses."

## 12. Demo Script

Use this sequence during viva or practice:

1. Explain project modules from the root folder.
2. Start services using `RUN_APPLICATION_POWERSHELL.md`.
3. Show Eureka UI and point out registered services.
4. Open customer UI.
5. Register a customer.
6. Login as customer.
7. Deposit money.
8. Withdraw money.
9. Register a second customer.
10. Transfer money to second customer by email.
11. Apply for a loan.
12. Calculate EMI.
13. Open admin UI.
14. Login with default admin credentials.
15. Search or fetch customer.
16. Deactivate and activate customer.
17. Approve or deny pending loan.
18. Explain where each action is implemented in controller and service code.

## 13. Common Viva Questions And Answers

### 1. What is your project?

My project is MyFinBank, a microservice-based banking application built using Spring Boot. It supports customer registration, login, banking transactions, loan applications, admin customer management, notifications, JWT security, and frontend-backend integration.

### 2. Why did you use microservices?

Microservices separate the application into independent services. In this project, customer logic, admin logic, notifications, configuration, and discovery are separated. This makes the system easier to understand, scale, and maintain.

### 3. What is Eureka?

Eureka is a service registry. Each microservice registers itself with Eureka, and other services can discover it using its service name. This project uses Eureka server on port `8761`.

### 4. What is Config Server?

Config Server provides centralized configuration for services. Instead of keeping all settings separately in each service, common configuration can be managed from one place.

### 5. How does login work?

The user submits email and password. Spring Security authenticates the credentials through `AuthenticationManager`. If valid, `JwtUtil` generates a JWT token. The frontend stores the token and sends it with future requests.

### 6. How does JWT work here?

JWT is used for stateless authentication. After login, the token is sent in the `Authorization` header as `Bearer <token>`. The JWT filter validates the token and sets the authenticated user in the security context.

### 7. How does money transfer work?

The transfer API receives source customer ID, target email, and amount. The service checks the source customer, finds the target customer, verifies sufficient balance, debits the source account, credits the target account, and saves transaction records for both users.

### 8. How does admin approve loans?

The admin frontend calls the admin service. Admin service calls customer service through `CustomerClient`. Customer service finds the pending loan application and updates its status to `APPROVED` or `DENIED`.

### 9. How do services communicate?

Services communicate using REST APIs and `RestTemplate`. The `RestTemplate` is load balanced, so it can call service names like `http://customer-service` and `http://notification-service`, which are resolved through Eureka.

### 10. What database is used?

The project uses H2 in-memory databases. Each service has a separate database, such as `customerdb` for customer service and `admindb` for admin service.

### 11. What is the role of DTOs?

DTOs transfer data between frontend and backend or between services. For example, `LoginRequest`, `RegistrationRequest`, `TransferRequest`, and `AuthResponse` carry request and response data without exposing full entity objects unnecessarily.

### 12. What is the role of repositories?

Repositories are Spring Data JPA interfaces used to perform database operations. They reduce boilerplate code because Spring automatically provides methods like save, find, delete, and custom finder methods.

### 13. What is the role of controllers?

Controllers expose REST endpoints. They receive HTTP requests, call service-layer methods, and return HTTP responses.

### 14. What is the role of services?

Service classes contain business logic. For example, `CustomerService` handles deposit, withdrawal, transfer, loan application, and transaction saving.

### 15. What would you improve in this project?

I would improve validation, protect admin endpoints more strictly, add persistent databases like MySQL, add API gateway support, improve test coverage, and use stronger service-to-service authentication.

## 14. Fast Revision Checklist

Before viva, make sure you can explain:

- What every module does
- Why the project uses microservices
- How Eureka helps service discovery
- How Config Server helps configuration
- How JWT login works
- How frontend calls backend APIs
- How customer registration creates customer and account records
- How deposit, withdraw, and transfer update accounts
- How loan approval moves through admin service to customer service
- How low-balance notification reaches notification service
- Where entities, DTOs, repositories, controllers, and services are located
- What H2 database is and why each service has its own database
- What you would improve if given more time

## 15. Best Code Reading Order

Read these files in this sequence:

```text
README.md
pom.xml
RUN_APPLICATION_POWERSHELL.md
config-server/src/main/java/com/myfinbank/configserver/ConfigServerApplication.java
eureka-server/src/main/java/com/myfinbank/eurekaserver/EurekaServerApplication.java
customer-service/src/main/resources/application.yml
customer-service/src/main/java/com/myfinbank/customerservice/controller/CustomerController.java
customer-service/src/main/java/com/myfinbank/customerservice/service/CustomerService.java
customer-service/src/main/java/com/myfinbank/customerservice/entity/Customer.java
customer-service/src/main/java/com/myfinbank/customerservice/entity/Account.java
customer-service/src/main/java/com/myfinbank/customerservice/entity/TransactionRecord.java
customer-service/src/main/java/com/myfinbank/customerservice/entity/LoanApplication.java
customer-service/src/main/java/com/myfinbank/customerservice/security/SecurityConfig.java
customer-service/src/main/java/com/myfinbank/customerservice/security/JwtUtil.java
customer-service/src/main/java/com/myfinbank/customerservice/service/NotificationClient.java
admin-service/src/main/resources/application.yml
admin-service/src/main/java/com/myfinbank/adminservice/config/DataLoader.java
admin-service/src/main/java/com/myfinbank/adminservice/controller/AdminController.java
admin-service/src/main/java/com/myfinbank/adminservice/service/AdminService.java
admin-service/src/main/java/com/myfinbank/adminservice/service/CustomerClient.java
admin-service/src/main/java/com/myfinbank/adminservice/security/SecurityConfig.java
notification-service/src/main/java/com/myfinbank/notificationservice/controller/NotificationController.java
notification-service/src/main/java/com/myfinbank/notificationservice/entity/NotificationEvent.java
customer-service/src/main/resources/static/customer.js
admin-service/src/main/resources/static/admin.js
```

