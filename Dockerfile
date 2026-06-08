# pull alpine base image
FROM eclipse-temurin:21-jre-alpine
# env variables
# Default values / documented environment variables
ENV APP_NAME=Expense-Tracker
ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_NAME=expense_tracker
ENV DB_USERNAME=chandu
ENV DB_PASSWORD=1234
ENV APP_PORT=8080
ENV FRONTEND_CORS_LIST=http://localhost:5173,http://localhost:3000
# setup workdir
WORKDIR /app
# copy jar file from local build
COPY target/*.jar app.jar
#expose port
EXPOSE ${APP_PORT}

# Pass APP_PORT to Spring Boot
ENTRYPOINT ["java", "-jar", "-Dserver.port=${APP_PORT}", "app.jar"]