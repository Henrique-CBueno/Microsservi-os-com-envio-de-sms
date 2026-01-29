# Sistema de Análise de Crédito com Microserviços Orientado a Eventos

Sistema de análise de crédito baseado em eventos, construído com Spring Boot 3, arquitetura de microserviços e mensageria assíncrona, simulando um cenário real de produção com **notificações SMS via AWS SNS**, processamento paralelo e atualizações em tempo real.

## 📋 Arquitetura

O sistema é composto por **3 microserviços** orquestrados via Docker Compose, mais um frontend e infraestrutura de suporte:

### Microserviços

- **proposta-app** (porta 8080)
  - Serviço principal responsável pelo recebimento e gerenciamento de propostas
  - Integração com banco de dados PostgreSQL
  - WebSocket para notificações em tempo real ao frontend
  - Agendamento de tarefas com `@Scheduled`
  - Expõe REST API para o frontend

- **analise-credito**
  - Análise de crédito com regras de negócio baseadas em Design Pattern Strategy
  - Processamento paralelo de múltiplas propostas
  - Cálculo de pontuação com múltiplos critérios
  - Consome mensagens de fila de propostas pendentes

- **notificacao**
  - Integração com AWS SNS para envio de SMS
  - Notifica cliente conforme a análise avança
  - Recebe eventos de proposta pendente e concluída

### Infraestrutura

- **PostgreSQL** - Banco de dados de propostas
- **RabbitMQ** - Message broker com interface de gerenciamento
- **proposta-web-front** - Frontend em React/Angular consumindo a API

## 🚀 Quick Start

### Pré-requisitos

- Docker & Docker Compose
- Java 17+ (para desenvolvimento local)
- Maven 3.8.7+

### Iniciar o Sistema Completo

```bash
docker compose up -d
```

O Docker Compose realiza:

1. **Build automático** de todos os microserviços (compila com Maven `-DskipTests`)
2. **Cria imagens** Docker otimizadas com multi-stage build
3. **Orquestra containers** com dependências corretas
4. **Inicializa bancos** de dados e message broker

### Endpoints Principais

#### Proposta App (http://localhost:8080)

```bash
# Criar nova proposta
POST /proposta
Content-Type: application/json

{
  "cliente": "João Silva",
  "cpf": "123.456.789-00",
  "renda": 5000.00,
  "valorSolicitado": 10000.00,
  "prazoPagamento": 24
}

# Listar todas as propostas
GET /proposta
```

#### Frontend

```
http://localhost:80
```

## 🏗️ Arquitetura Técnica Detalhada

### 1. **Comunicação Assíncrona - RabbitMQ**

#### Topologia de Filas e Exchanges

```
┌─────────────────────────────────────────────┐
│         Exchanges (Fanout)                  │
├────────────────────┬────────────────────────┤
│ proposta-pendente  │ proposta-concluida    │
└────────────────────┴────────────────────────┘
       ↓                      ↓
  ┌──────────────┐      ┌──────────────┐
  │ Propostas    │      │ Propostas    │
  │ Pendentes    │      │ Concluídas   │
  └──────────────┘      └──────────────┘
       ↓                      ↓
  ┌─────────────────────────────────────┐
  │ ms-analise-credito                  │
  │ ms-notificacao                      │
  │ ms-proposta                         │
  └─────────────────────────────────────┘
```

#### Fluxo de Mensagens

1. **Proposta Criada** → `proposta-pendente` exchange
   - Routing: `proposta-pendente.ms-analise-credito`
   - Routing: `proposta-pendente.ms-notificacao`
   - Routing: `proposta-pendente.ms-proposta`

2. **Análise Concluída** → `proposta-concluida` exchange
   - Routing: `proposta-concluida.ms-notificacao`
   - Routing: `proposta-concluida.ms-proposta`

#### Dead Letter Queues (DLQ)

Propostas com erro são redireccionadas para:

- `proposta-pendente.dlq` - Falhas em análise
- `proposta-concluida.dlq` - Falhas na conclusão

**Configuração em cada serviço:**

```properties
spring.rabbitmq.host=rabbit-mq
spring.rabbitmq.port=5672
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.max-attempts=3
```

### 2. **Design Pattern Strategy - Análise de Crédito**

A análise usa múltiplos critérios aplicados em sequência:

```java
// Interface base
public interface CalculoPonto {
    int calcularPonto(Proposta proposta);
}

// Implementações
- RendaMaiorValorSolicitadoImpl
  • Verifica se renda > valor solicitado

- PrazoPagamentoMenorDezAnosImpl
  • Verifica prazo de pagamento < 10 anos

- PontuacaoScoreImpl
  • Calcula score baseado em múltiplos fatores
```

**Orquestração com @Order:**

```java
@Component
@Order(1)  // Executa primeiro
public class RendaMaiorValorSolicitadoImpl implements CalculoPonto { }

@Component
@Order(2)  // Executa segundo
public class PrazoPagamentoMenorDezAnosImpl implements CalculoPonto { }
```

Os critérios são aplicados paralelamente via `@RabbitListener` com múltiplos consumers.

### 3. **WebSocket - Notificações em Tempo Real**

#### Configuração

```java
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // Endpoints WebSocket
    // /ws - Cliente se conecta
    // /topic/propostas - Tópico de broadcast
}
```

#### Fluxo

1. Frontend conecta ao `/ws`
2. Serviço envia atualizações via `SimpMessagingTemplate`
3. Todos os clientes recebem notificações em tempo real

```javascript
// Frontend (exemplo)
const stompClient = new StompClient("ws://localhost:8080/ws");
stompClient.subscribe("/topic/propostas", (message) => {
  // Atualiza UI com novo status
});
```

### 4. **Persistência - PostgreSQL + JPA/Hibernate**

```
┌──────────────┐
│   Proposta   │
├──────────────┤
│ id           │ PK
│ cliente      │
│ cpf          │
│ renda        │
│ valorSolicitado
│ prazoPagamento
│ status       │ (PENDENTE, EM_ANALISE, APROVADA, REJEITADA)
│ createdAt    │
└──────────────┘
```

**Configurações:**

```properties
spring.datasource.url=jdbc:postgresql://postgres:5432/docker
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### 5. **AWS SNS - Integração SMS**

O serviço de notificação envia SMS para o cliente via AWS SNS:

```java
@Service
public class NotificacaoSmsService {
    private final AmazonSNS amazonSNS;

    public void enviarSms(String numeroTelefone, String mensagem) {
        // Envia SMS via SNS
    }
}
```

**Configuração em produção:**

```properties
aws.accessKey=${AWS_ACCESS_KEY}
aws.secretKey=${AWS_SECRET_KEY}
aws.region=us-east-1
aws.sns.endpoint=https://sns.us-east-1.amazonaws.com
```

### 6. **Agendamento - @Scheduled**

O `proposta-app` possui tarefas agendadas:

```java
@SpringBootApplication
@EnableScheduling
public class PropostaAppApplication { }

@Service
public class PropostaService {

    @Scheduled(fixedDelay = 60000)  // A cada 1 minuto
    public void verificarPropostasAtrasadas() {
        // Lógica de verificação
    }
}
```

### 7. **Data Transfer Objects (DTOs) + MapStruct**

```java
// Request DTO
public record PropostaCreateReqDTO(
    String cliente,
    String cpf,
    BigDecimal renda,
    BigDecimal valorSolicitado,
    Integer prazoPagamento
) {}

// Response DTO
public record PropostaCreateResDTO(
    Long id,
    String status,
    LocalDateTime criada
) {}

// Mapeador
@Mapper(componentModel = "spring")
public interface PropostaMapper {
    PropostaCreateResDTO toDTO(Proposta proposta);
    Proposta toEntity(PropostaCreateReqDTO dto);
}
```

### 8. **CORS - Cross-Origin Resource Sharing**

```java
@Configuration
public class CorsConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:3000")  // Frontend
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*");
            }
        };
    }
}
```

### 9. **Resiliência na Aplicação**

- **Retry Automático:** RabbitMQ com `retry.max-attempts=3`
- **Dead Letter Queue:** Propostas com erro são redirecionadas
- **Health Check:** RabbitMQ com `healthcheck` no compose
- **Dependencies Management:** Containers aguardam dependências inicializarem

## 📦 Stack Tecnológico

```
Spring Boot 3.5.10
├── Spring Web (REST API)
├── Spring Data JPA (Persistência)
├── Spring AMQP (RabbitMQ)
├── Spring WebSocket
└── Spring Scheduling

Banco de Dados & Message Broker
├── PostgreSQL 15
└── RabbitMQ 3.12

Utilities
├── Lombok (Redução de boilerplate)
├── MapStruct (Mapeamento de DTOs)
├── Jackson (JSON serialization)
└── AWS SDK SNS (Envio de SMS)

Build & Deploy
├── Maven 3.8.7
├── Docker (Multi-stage builds)
└── Docker Compose (Orquestração)
```

## 🔧 Configuração de Perfis

### Desenvolvimento (Default)

```properties
# application.properties
spring.rabbitmq.host=localhost
spring.datasource.url=jdbc:postgresql://localhost:5432/docker
aws.sns.endpoint=http://localhost:4566  # LocalStack
```

### Produção

```properties
# application-prod.properties
spring.rabbitmq.host=rabbit-mq        # Nome do serviço no compose
spring.datasource.url=jdbc:postgresql://postgres:5432/docker
aws.sns.endpoint=https://sns.us-east-1.amazonaws.com
```

**Ativação:**

```bash
# Via variável de ambiente
docker compose up  # SPRING_PROFILES_ACTIVE=prod

# Ou manualmente
java -jar app.jar --spring.profiles.active=prod
```

## 🐳 Docker & Compose

### Multi-stage Dockerfile

```dockerfile
# Estágio 1: Build
FROM maven:3.8.7-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Runtime
FROM eclipse-temurin:17-jdk
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Vantagens:**

- Sempre compila com código atualizado
- Pula testes com `-DskipTests` para velocidade
- Imagem final enxuta (apenas JDK + JAR)

### Orquestração

```yaml
services:
  proposta-app:
    build: ./proposta-app
    depends_on:
      rabbit-mq:
        condition: service_healthy
      postgres:
        condition: service_started
```

## 📊 Fluxo Completo de Uma Proposta

```
1. Cliente submete proposta
   ↓
2. proposta-app recebe POST /proposta
   ├─ Persiste no PostgreSQL
   └─ Publica em proposta-pendente exchange

3. RabbitMQ distribui para 3 consumers
   ├─ analise-credito (aplica regras)
   ├─ notificacao (prepara SMS)
   └─ ms-proposta (atualiza status)

4. analise-credito processa
   └─ Aplica estratégias em paralelo

5. Resultado retorna via proposta-concluida exchange
   ├─ notificacao envia SMS via AWS SNS
   ├─ proposta-app atualiza PostgreSQL
   └─ WebSocket notifica frontend em tempo real
```

## 🔐 Segurança

- ✅ CORS configurado
- ✅ Retry automático com DLQ
- ✅ Validação de DTOs
- ✅ Health checks

## 📈 Escalabilidade

- **Consumers Paralelos:** Múltiplas instâncias podem consumir da mesma fila
- **RabbitMQ Clustering:** Suporta múltiplos nós
- **PostgreSQL:** Pode ser migrado para ambiente gerenciado
- **Docker Compose:** Ideal para desenvolvimento; use Kubernetes para produção

## 🤝 Contribuindo

1. Clone o repositório
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT.

---

**Desenvolvido com ❤️ por Henrique**
