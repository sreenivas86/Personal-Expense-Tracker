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