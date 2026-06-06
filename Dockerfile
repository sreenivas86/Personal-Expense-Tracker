<<<<<<< HEAD
# pull alpine base image
FROM eclipse-temurin:21-jre-alpine
# env variables
ENV APP_NAME=Expense-Tracker
ENV DB_URL=jdbc:mysql://localhost:3306/expense_tracker
ENV DB_USERNAME=chandu
ENV DB_PASSWORD=1234
ENV APP_PORT=8080
# setup workdir
WORKDIR /app
# copy jar file from local build
COPY target/*.jar app.jar
# run app
ENTRYPOINT [ "java","-jar","app.jar" ]
=======
# -------- Stage 1: Build --------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests


# -------- Stage 2: Run --------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
>>>>>>> c04cecd396f60a82b47f2563a8e2e8dcfcd88781
