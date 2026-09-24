FROM node:20-alpine AS frontend-build
WORKDIR /workspace

COPY frontend/package*.json ./frontend/
WORKDIR /workspace/frontend
RUN npm ci

COPY frontend/ ./
RUN npm run build


FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
COPY --from=frontend-build /workspace/src/main/resources/static ./src/main/resources/static

RUN mvn -DskipTests clean package


FROM tomcat:9.0-jdk17-temurin

ENV TZ=Asia/Shanghai
ENV LIBRARY_UPLOAD_DIR=/data/library/uploads

RUN rm -rf /usr/local/tomcat/webapps/* \
    && sed -i 's/port="8005"/port="-1"/g' /usr/local/tomcat/conf/server.xml \
    && mkdir -p /data/library/uploads

COPY --from=backend-build /workspace/target/library.war /usr/local/tomcat/webapps/library.war

VOLUME ["/data/library/uploads"]
EXPOSE 8080

CMD ["catalina.sh", "run"]
