# Budget Tracker App

This is a Spring Boot-based Budget Tracker application that allows users to manage their income, expenses, and monthly budgets securely using JWT authentication.

## Features

- User Authentication (JWT)
- Add/View/Delete Expenses
- Monthly Budget per Category
- Dashboard with Charts
- Swagger API Documentation

## Technologies Used

- Spring Boot
- Spring Security + JWT
- MySQL + JPA
- Swagger UI
- React (Frontend){click here https://github.com/sreenivas86/Personal-Budget-Tracker-UI}


## Required tools and sofwares 
- Runtime environment -java 21
- maven tool 
- React for frontend 

## How to Run

1. Clone this repo
2. Configure `application.properties`
3. Run with `mvn spring-boot:run`

## `/src/main/resources/application.properties`

````
spring.application.name=Expense-Tracker

spring.datasource.url=jdbc:mysql://localhost:3306/<change_your_database_name>
spring.datasource.username=<your_db_username>
spring.datasource.password=<your_db_password>


spring.jpa.database=mysql
spring.jpa.hibernate.ddl-auto=update
#spring.jpa.show-sql=true
#spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
````



````
If you're using an in-memory database with your Spring Boot application, the most common choice is H2 Database.

Here’s exactly what you need:
````

##  1. Maven Dependency for H2

````
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>runtime</scope>
</dependency>

````

##  2. application.properties for H2

````
below properties to add ur spring boot application properties file
# ==== H2 DATABASE ====
spring.datasource.url=jdbc:h2:mem:budgetdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Automatically create/drop tables based on entities
spring.jpa.hibernate.ddl-auto=update

# Show SQL in logs (optional)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# H2 Console Enabled (for browser UI)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

````

##  3. Access the H2 Console

````
[h2 access url](http://localhost:8080/h2-console)

````




Perfect! Here’s a **complete Spring Boot + Docker setup** where all configuration comes from **Docker environment variables**, ready to run with MySQL.

---

# 1️⃣ Project Structure

```
springboot-docker-app/
├─ src/
│  └─ main/
│      └─ java/com/example/demo/
│          └─ DemoApplication.java
├─ pom.xml
├─ Dockerfile
└─ .env
```

---

# 2️⃣ `application.properties`

```properties
server.port=${SERVER_PORT:8080}                 # fallback to 8080
spring.datasource.url=${DB_URL}                # from Docker env
spring.datasource.username=${DB_USERNAME}      # from Docker env
spring.datasource.password=${DB_PASSWORD}      # from Docker env
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

* `${VAR_NAME:default}` → environment variable substitution
* Works with **Docker environment variables**

---

# 3️⃣ Sample Spring Boot main class

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

---

# 4️⃣ Dockerfile

```dockerfile
# Use OpenJDK 21 Alpine
FROM openjdk:21-jdk-alpine

WORKDIR /app

# Copy built jar
COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# Expose port (from properties/env)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
```

> Make sure you run `mvn clean package` to create the `target/demo-0.0.1-SNAPSHOT.jar`.

---

# 5️⃣ `.env` file (for Docker environment variables)

```env
SERVER_PORT=9090
DB_URL=jdbc:mysql://mysql:3306/mydb
DB_USERNAME=root
DB_PASSWORD=secret
```

---

# 6️⃣ Run with Docker

```bash
# Build the image
docker build -t springboot-docker-app .

# Run container using .env file
docker run --env-file .env -p 9090:9090 springboot-docker-app
```

* `--env-file .env` → injects all environment variables into the container
* Spring Boot reads them automatically

---

# 7️⃣ Optional: Using individual `-e` flags

```bash
docker run -d -p 9090:9090 \
  -e SERVER_PORT=9090 \
  -e DB_URL=jdbc:mysql://mysql:3306/mydb \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=secret \
  springboot-docker-app
```

---

# 8️⃣ Notes

* This setup **does not require changes** to your application code when moving between dev, staging, or production.
* You can also link a **MySQL container** using Docker Compose for full environment setup.

---



