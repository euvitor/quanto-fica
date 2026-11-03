# 🏠 Quanto Fica?

> **Simulador transparente e didático de amortização SAC e custos reais de aquisição de imóveis no Brasil.**

[![CI/CD Pipeline](https://github.com/euvitor/quanto-fica/actions/workflows/ci.yml/badge.svg)](https://github.com/euvitor/quanto-fica/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java: 21](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot: 4.1.1](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Angular: 19](https://img.shields.io/badge/Angular-19%20Zoneless-red.svg)](https://angular.dev/)
[![Tailwind CSS: v4](https://img.shields.io/badge/Tailwind%20CSS-v4-38bdf8.svg)](https://tailwindcss.com/)

---

## 🎯 O Problema

Ao comprar um imóvel financiado (ex: Caixa Econômica Federal), a maioria dos compradores comete dois erros graves de planejamento financeiro:

1. **A ilusão da parcela inicial:** Não compreendem como a amortização funciona ao longo do tempo (por que no SAC a parcela começa mais alta e cai, enquanto na Price é constante, e quanto de juros reais é pago ao final de 30 anos).
2. **Os custos ocultos de aquisição:** Focam apenas na entrada e na parcela, mas são surpreendidos com despesas obrigatórias que somam de **4% a 8% do valor total do imóvel** antes mesmo de pegar as chaves:
   - **ITBI** (Imposto de Transmissão de Bens Imóveis);
   - **Escritura e Registro de Imóveis** (tabelas de cartório com 50% de desconto no 1º imóvel pelo Art. 290 da Lei 6.015/73);
   - **Custos bancários de avaliação e vistoria**;
   - **Seguros habitacionais obrigatórios (DFI e MIP)**.

O **Quanto Fica** nasceu para responder de forma clara, matemática e acessível:
> *"Quanto dinheiro eu realmente preciso ter guardado hoje, e quanto vou pagar mês a mês até quitar o imóvel?"*

---

## 🏗️ Arquitetura do Monorepo

```text
quanto-fica/
├── docs/                        # Especificações técnicas, regras de negócio e planejamento
│   ├── domain-rules.md          # Matemática financeira (SAC) e regras tributárias/cartorárias
│   ├── api-contracts.md         # Contratos e especificações dos endpoints REST (OpenAPI/RFC 7807)
│   └── roadmap.md               # Plano diretor e checklist de progresso 100% concluído
├── backend/                     # API REST em Java 21 + Spring Boot 4 + PostgreSQL + Flyway
│   ├── Dockerfile               # Multi-stage Eclipse Temurin 21 -> JRE Alpine (non-root spring:spring)
│   ├── pom.xml                  # Dependências Maven (Web, Validation, JPA, Flyway, Test)
│   └── src/
│       ├── main/java/br/com/quantofica/backend/
│       │   ├── dto/             # Contratos imutáveis de entrada e saída (Java Records)
│       │   ├── model/           # Entidades JPA mapeadas (Simulation) com NUMERIC(15,2)
│       │   ├── repository/      # Repositórios Spring Data JPA
│       │   ├── service/         # Regras de negócio, motor SAC e orquestração de persistência
│       │   ├── controller/      # Endpoints REST (201 Created com header Location, 200, 404)
│       │   └── exception/       # Tratamento global de erros (RFC 7807 ProblemDetail)
│       └── test/                # Suíte de testes com JUnit 5, AssertJ, Mockito e WebMvcTest (23 testes)
├── frontend/                    # Single Page Application em Angular Moderno (Zoneless + Signals + Tailwind)
│   ├── Dockerfile               # Multi-stage Node 22 build -> Nginx Alpine runtime
│   ├── nginx.conf               # Roteamento SPA com fallback index.html, Gzip e Cache-Control
│   └── src/app/
│       ├── components/          # Formulário reativo, Cards de métricas, Gráficos Chart.js e Tabela
│       ├── services/            # SimulationService com signals reativos e persistência
│       └── models/              # Interfaces TypeScript estritamente tipadas
├── docker-compose.yml           # Orquestração local: Postgres 16 + Spring Boot + Nginx Frontend
└── .github/workflows/ci.yml     # Pipeline de Integração Contínua (Testes Backend + Build Frontend)
```

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia | Motivação Técnica |
| :--- | :--- | :--- |
| **Linguagem Backend** | Java 21 (LTS) | Records imutáveis, Pattern Matching, Virtual Threads e tipagem estrita. |
| **Framework Backend** | Spring Boot 4.x | Ecossistema maduro, injeção de dependências e arquitetura em camadas. |
| **Banco de Dados** | PostgreSQL 16 + Flyway | Banco relacional robusto com versionamento automático de migrations DDL. |
| **Precisão Numérica** | `java.math.BigDecimal` | Precisão arbitrária e controle rigoroso de arredondamento bancário (`HALF_EVEN`). |
| **Validação** | Jakarta Validation | Fail-fast validation com anotações declarativas (`@NotNull`, `@DecimalMin`). |
| **Testes Backend** | JUnit 5, AssertJ & Mockito | 23 testes unitários e de integração web com banco H2 em memória. |
| **Frontend** | Angular 19 (Zoneless + Signals) | Máxima performance sem Zone.js, reatividade granular com Signals e Reactive Forms. |
| **Estilização** | Tailwind CSS v4 | Design System moderno, layout Linear Fintech Clean, responsivo e sem excessos. |
| **DevOps** | Docker, Nginx, GitHub Actions | Containers enxutos em Alpine Linux, pipeline automatizado de CI/CD. |

---

## 🚀 Como Executar o Projeto

### Opção 1: Via Docker Compose (Recomendado)

Suba todo o ecossistema (PostgreSQL, Backend Spring Boot e Frontend Nginx) com um único comando:

```bash
docker compose up -d --build
```

Acesse no seu navegador:
* **Frontend Web:** [http://localhost:4200](http://localhost:4200)
* **Backend API:** [http://localhost:8080](http://localhost:8080/api/v1/simulations)
* **PostgreSQL:** `localhost:5432` (user: `quantofica`, pass: `quantofica`, db: `quantofica_db`)

Para encerrar os serviços:
```bash
docker compose down
```

---

### Opção 2: Desenvolvimento Local Manual

#### 1. Backend (Spring Boot)
```bash
cd backend
./mvnw clean test
./mvnw spring-boot:run
```

#### 2. Frontend (Angular)
```bash
cd frontend
npm install
npm start
```

---

## 📚 Documentação Técnica

Consulte os documentos detalhados na pasta [`docs/`](file:///Users/vitor/dev/Projetos/17.quanto-fica/docs):

1. 📐 **[Regras de Negócio e Fórmulas Financeiras](file:///Users/vitor/dev/Projetos/17.quanto-fica/docs/domain-rules.md):** Fórmulas matemáticas do SAC, taxas anuais/mensais e regras cartorárias.
2. 🔌 **[Contratos da API REST](file:///Users/vitor/dev/Projetos/17.quanto-fica/docs/api-contracts.md):** Especificação OpenAPI dos payloads e respostas RFC 7807.
3. 🗺️ **[Roadmap de Execução](file:///Users/vitor/dev/Projetos/17.quanto-fica/docs/roadmap.md):** Histórico completo de desenvolvimento de todas as 6 fases.

---

## 👨‍💻 Autor

Desenvolvido por **Vitor**  
* **Portfólio / Site:** [euvitor.dev](https://euvitor.dev)  
* **GitHub:** [@euvitor](https://github.com/euvitor)  
* **Repositório do Projeto:** [github.com/euvitor/quanto-fica](https://github.com/euvitor/quanto-fica)
