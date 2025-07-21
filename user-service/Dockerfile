# Use a specific Java version as a base image
FROM eclipse-temurin:21-jdk-jammy

# Set the working directory inside the container
WORKDIR /app

# Argument to hold the name of the JAR file
ARG JAR_FILE=target/*.jar

# Copy the JAR file from the build context to the container
COPY ${JAR_FILE} application.jar

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "application.jar"]