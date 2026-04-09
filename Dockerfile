# Stage 1: Build the WAR
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependency downloads separately from source changes
COPY pom.xml .
COPY war/WEB-INF/lib/jackson-all-1.9.5.jar war/WEB-INF/lib/jackson-all-1.9.5.jar
COPY war/WEB-INF/lib/twitter4j-core-3.0.3.jar war/WEB-INF/lib/twitter4j-core-3.0.3.jar
RUN mvn dependency:go-offline -q

# Copy source and web resources, then build
COPY src/ src/
COPY war/ war/
RUN mvn package -DskipTests -q

# Stage 2: Run in Tomcat 10.1 (Jakarta Servlet 6.0)
FROM tomcat:10.1-jdk17-temurin

# Remove the default ROOT webapp
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy the application as ROOT so it serves at /
COPY --from=build /app/target/scrobblefilter-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Cloud Run injects PORT; patch Tomcat's HTTP connector to match at startup
ENV PORT=8080
EXPOSE 8080
CMD ["sh", "-c", "sed -i 's/port=\"8080\"/port=\"'$PORT'\"/' /usr/local/tomcat/conf/server.xml && catalina.sh run"]
