# MyFinBank Capstone Project

This workspace contains a Spring Boot multi-module microservice implementation for the MyFinBank banking application.

## Project Modules

- `config-server` - Spring Cloud Config server for shared configuration.
- `eureka-server` - Eureka service registry for microservice discovery.
- `customer-service` - Customer operations, registration, login, deposit, withdraw, transfer, loan application, chat, and transaction tracking.
- `admin-service` - Admin operations, customer activation/deactivation, loan approval/denial, and service-to-service customer lookup.
- `notification-service` - Notification microservice that receives low-balance events and stores notification events.

## Notes

- Each service uses its own H2 in-memory database.
- JWT authentication is implemented for customer and admin login.
- Swagger UI is available at `/swagger-ui.html` for each service.
- Customer frontend is at `http://localhost:8081/customer.html`.
- Admin frontend is at `http://localhost:8082/admin.html`.
- Viva preparation notes are available in `VIVA_PREPARATION_GUIDE.md`.

## Recommended Run Order

1. Start `config-server` on port `8888`.
2. Start `eureka-server` on port `8761`.
3. Start `notification-service` on port `8083`.
4. Start `customer-service` on port `8081`.
5. Start `admin-service` on port `8082`.

## Default Admin Credentials

- Email: `admin@myfinbank.com`
- Password: `admin123`
