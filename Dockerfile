FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/maven:3-eclipse-temurin-17 AS build
WORKDIR /app
RUN mkdir -p /root/.m2 && printf '%s\n' \
    '<settings><mirrors><mirror><id>aliyun</id><mirrorOf>*</mirrorOf><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' \
    > /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/Weather-App-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8777
ENTRYPOINT ["java", "-jar", "app.jar"]
