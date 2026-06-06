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
# expose port
EXPOSE 8080
# run app
ENTRYPOINT [ "java","-jar","app.jar" ]
