FROM openjdk:11
ENV JAVA_OPTS="-XX:+AlwaysPreTouch -XX:+TieredCompilation -XX:NewRatio=1 -XX:+UseConcMarkSweepGC -XX:MaxMetaspaceSize=1024m -XX:ParallelGCThreads=2 -XX:ConcGCThreads=2 -XX:MaxTenuringThreshold=15"
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
