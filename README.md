# 🏠 Quanto Fica?

> **Simulador transparente e didático de financiamento imobiliário e custos reais de aquisição de imóveis no Brasil.**

---

## 🎯 O Problema

Ao comprar um imóvel financiado (ex: Caixa Econômica Federal), a maioria dos compradores comete dois erros graves de planejamento financeiro:

1. **A ilusão da parcela inicial:** Não compreendem como a amortização funciona ao longo do tempo (por que no SAC a parcela começa mais alta e cai, enquanto na Price é constante, e quanto de juros reais é pago ao final de 30 anos).
2. **Os custos ocultos de aquisição:** Focam apenas na entrada e na parcela, mas são surpreendidos com despesas obrigatórias que somam de **4% a 8% do valor total do imóvel** antes mesmo de pegar as chaves:
   - **ITBI** (Imposto de Transmissão de Bens Imóveis);
   - **Escritura e Registro de Imóveis** (tabelas de cartório por estado);
   - **Custos bancários de avaliação e vistoria**;
   - **Seguros obrigatórios habitacionais (DFI e MIP)** embutidos nas parcelas.

O **Quanto Fica** nasceu para responder de forma clara, matemática e acessível:
> *"Quanto dinheiro eu realmente preciso ter guardado hoje, e quanto vou pagar mês a mês até quitar o imóvel?"*

---

## 🏗️ Estrutura do Monorepo

```text
quanto-fica/
├── docs/                        # Especificações técnicas, regras de negócio e planejamento
│   ├── domain-rules.md          # Matemática financeira (SAC/Price) e regras tributárias/cartorárias
│   ├── api-contracts.md         # Contratos e especificações dos endpoints REST (OpenAPI/RFC 7807)
│   └── roadmap.md               # Plano de execução faseado e checklist de progresso
├── backend/                     # API REST em Java 21 + Spring Boot 4 + PostgreSQL + Flyway
│   ├── pom.xml                  # Dependências Maven (Web, Validation, JPA, Flyway, Test)
│   └── src/
│       ├── main/java/br/com/quantofica/backend/
│       │   ├── dto/             # Contratos imutáveis de entrada e saída (Java Records)
│       │   ├── model/           # Entidades JPA mapeadas (Simulation)
│       │   ├── repository/      # Repositórios Spring Data JPA
│       │   ├── service/         # Regras de negócio, motor SAC e orquestração de persistência
│       │   ├── controller/      # Endpoints REST (201 Created com header Location, 200, 404)
│       │   └── exception/       # Tratamento global de erros (ProblemDetail RFC 7807)
│       └── test/                # Suíte de testes com JUnit 5, AssertJ, Mockito e WebMvcTest
└── frontend/                    # Single Page Application em Angular Moderno (Standalone, Signals, Tailwind)
```

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia | Motivação Técnica |
| :--- | :--- | :--- |
| **Linguagem Backend** | Java 21 (LTS) | Suporte a Records, Pattern Matching, Virtual Threads e alta performance. |
| **Framework Backend** | Spring Boot 4.x | Ecossistema maduro, injeção de dependências, arquitetura limpa. |
| **Banco de Dados** | PostgreSQL 16 + Flyway | Banco relacional robusto com versionamento rigoroso de migrations DDL. |
| **Precisão Numérica** | `java.math.BigDecimal` | Precisão arbitrária e controle estrito de arredondamento bancário (`HALF_EVEN`). |
| **Validação** | Jakarta Validation | Fail-fast validation com anotações declarativas (`@NotNull`, `@DecimalMin`). |
| **Testes Backend** | JUnit 5, AssertJ & Mockito | Testes unitários puros e testes web de integração com banco H2 em memória. |
| **Frontend** | Angular (Moderno) + TypeScript | Standalone Components, Signals reativos, Reactive Forms e arquitetura corporativa. |
| **Estilização** | Tailwind CSS | Design moderno, responsivo, sem overhead de CSS legado. |

---

## 📚 Documentação Complementar

Consulte os documentos detalhados na pasta [`docs/`](file:///Users/vitor/dev/Projetos/17.quanto-fica/docs):

1. 📐 **[Regras de Negócio e Fórmulas Financeiras](file:///Users/vitor/dev/Projetos/17.quanto-fica/docs/domain-rules.md):** Fórmulas matemáticas do SAC, cálculo de juros mensais a partir de taxas anuais e custos de aquisição.
2. 🔌 **[Contratos da API REST](file:///Users/vitor/dev/Projetos/17.quanto-fica/docs/api-contracts.md):** Especificação completa de payloads, rotas e tratamento de erros.
3. 🗺️ **[Roadmap de Execução](file:///Users/vitor/dev/Projetos/17.quanto-fica/docs/roadmap.md):** Fases do projeto, entregáveis e status de cada etapa.

---

## 🚀 Como Executar o Backend Localmente

### Pré-requisitos
* Java 21 instalado (`java -version`)
* Maven 3.9+ (ou utilizar o wrapper `./mvnw`)

### Passos
```bash
cd backend
./mvnw clean compile
./mvnw test
./mvnw spring-boot:run
```
A API estará acessível em `http://localhost:8080`.
