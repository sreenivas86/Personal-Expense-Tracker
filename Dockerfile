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
# setup workdir
WORKDIR /app
# copy jar file from local build
COPY target/*.jar app.jar
# expose port
EXPOSE 8080
# run app
ENTRYPOINT [ "java","-jar","app.jar" ]
