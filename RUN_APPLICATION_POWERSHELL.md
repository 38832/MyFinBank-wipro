# MyFinBank Run Steps - PowerShell

Run all commands from separate PowerShell windows unless mentioned otherwise.

## 1. Go To Project Folder

```powershell
cd "C:\Users\Admin\Capstone Project"
```

## 2. Optional: Verify Build

```powershell
mvn test
```

## 3. Start Config Server

Open PowerShell window 1:

```powershell
cd "C:\Users\Admin\Capstone Project"
mvn -pl config-server spring-boot:run
```

Wait until you see that the application started on port `8888`.

## 4. Start Eureka Server

Open PowerShell window 2:

```powershell
cd "C:\Users\Admin\Capstone Project"
mvn -pl eureka-server spring-boot:run
```

Wait until it starts on port `8761`.

## 5. Start Notification Service

Open PowerShell window 3:

```powershell
cd "C:\Users\Admin\Capstone Project"
mvn -pl notification-service spring-boot:run
```

Wait until it starts on port `8083`.

## 6. Start Customer Service

Open PowerShell window 4:

```powershell
cd "C:\Users\Admin\Capstone Project"
mvn -pl customer-service spring-boot:run
```

Wait until it starts on port `8081`.

## 7. Start Admin Service

Open PowerShell window 5:

```powershell
cd "C:\Users\Admin\Capstone Project"
mvn -pl admin-service spring-boot:run
```

Wait until it starts on port `8082`.

## 8. Open In Browser

```text
Customer UI: http://localhost:8081/customer.html
Admin UI:    http://localhost:8082/admin.html
Eureka UI:   http://localhost:8761
```

## 9. Default Admin Login

```text
Email:    admin@myfinbank.com
Password: admin123
```

## 10. Swagger URLs

```text
Customer Swagger:      http://localhost:8081/swagger-ui.html
Admin Swagger:         http://localhost:8082/swagger-ui.html
Notification Swagger:  http://localhost:8083/swagger-ui.html
```

## 11. Stop The Application

Close each PowerShell window where a service is running.

Or stop all Java processes:

```powershell
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
```

## 12. If Port Is Already In Use

Check which process is using the port:

```powershell
Get-NetTCPConnection -LocalPort 8888,8761,8081,8082,8083 -ErrorAction SilentlyContinue | Select-Object LocalPort,State,OwningProcess
```

Stop a specific process:

```powershell
Stop-Process -Id <PROCESS_ID> -Force
```

Replace `<PROCESS_ID>` with the `OwningProcess` value from the previous command.

## Correct Start Order

```text
1. config-server
2. eureka-server
3. notification-service
4. customer-service
5. admin-service
```
Config Server:        http://localhost:8888
Eureka Server:        http://localhost:8761
Notification Service: http://localhost:8083
Customer UI:          http://localhost:8081/customer.html
Admin UI:             http://localhost:8082/admin.html