# ==========================================
# Estágio 1: Build da Aplicação com Maven
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copia os arquivos do Maven Wrapper e o POM primeiro para aproveitar o cache de camadas do Docker
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Garante permissão de execução do wrapper
RUN chmod +x mvnw

# Baixa as dependências em uma camada separada (se o pom.xml não mudar, o Docker usa o cache)
RUN ./mvnw dependency:go-offline -B

# Copia o código-fonte
COPY src/ src/

# Compila o projeto e empacota o jar (ignorando testes aqui pois eles já rodam na esteira de CI)
RUN ./mvnw clean package -DskipTests -B

# ==========================================
# Estágio 2: Imagem Final de Execução (Runtime)
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Boas práticas de segurança: cria um usuário de sistema não-root
RUN addgroup -g 1001 -S spring && \
    adduser -u 1001 -S spring -G spring

# Copia o .jar compilado do estágio do builder
COPY --from=builder /build/target/*.jar app.jar

# Define as permissões para o usuário spring
RUN chown -R spring:spring /app

# Troca para o usuário não-root
USER spring:spring

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Parâmetros otimizados para container na JVM (Container-aware Memory Management)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]