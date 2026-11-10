# ---------- Etapa 1: compila o projeto ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Baixa as dependências primeiro (fica em cache enquanto o pom.xml não mudar)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copia o código e gera o .jar
# -DskipTests: o teste padrão sobe a aplicação e precisaria do banco e do JWT_SECRET durante o build
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: imagem final, só com o Java e o .jar ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# MaxRAMPercentage: deixa o Java usar até 75% da memória do container (512 MB no plano Free)
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
