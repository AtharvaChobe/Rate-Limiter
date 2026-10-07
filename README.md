# Rate Limiter Service

A distributed API rate-limiting service built with **Java, Spring Boot, Redis, and Lua**.

The service uses the **Token Bucket algorithm** to control how many requests an API client can make within a configured time window. Redis stores the shared rate-limit state, allowing multiple instances of the service to enforce the same limit.

## Features

- Token Bucket rate-limiting algorithm
- Redis-backed shared state
- Atomic rate-limit evaluation using a Redis Lua script
- API-key-based rate limiting
- Supports multiple Spring Boot instances
- Concurrent request testing
- Simple REST API
- No database or frontend required

## Architecture

```text
Client Application
       |
       | POST /v1/check
       v
+----------------------+
|  Rate Limiter API    |
|    Spring Boot       |
+----------+-----------+
           |
           v
+----------------------+
|        Redis         |
| Shared Rate State    |
+----------+-----------+
           |
           v
     Lua Token Bucket
```

## How It Works

For every API key, Redis maintains:

- `tokens` - currently available tokens
- `lastRefill` - last time the bucket was refilled

When a request arrives:

1. The service calculates the refill rate from the configured limit and time window.
2. Redis executes the Lua script.
3. The script calculates newly available tokens.
4. If at least one token is available, one token is consumed and the request is allowed.
5. Otherwise, the request is rejected.
6. The Redis operation is atomic, preventing race conditions when multiple requests arrive concurrently.

### Example

If the configuration is:

```json
{
  "apiKey": "abc123",
  "limit": 10,
  "windowSeconds": 60
}
```

The bucket has a capacity of **10 tokens** and refills at:

```text
10 / 60 = 0.1667 tokens per second
```

Each allowed request consumes one token.

## API

### Check Rate Limit

**POST**

```text
/v1/check
```

### Request

```json
{
  "apiKey": "abc123",
  "limit": 10,
  "windowSeconds": 60
}
```

### Response

When a token is available:

```text
Request allowed
```

When the limit is exceeded:

```text
Rate limit exceeded
```

## Running the Project

### 1. Start Redis

Make sure Docker is installed, then run:

```bash
docker run -d --name rate-limiter-redis -p 6379:6379 redis:7
```

Verify the container is running:

```bash
docker ps
```

### 2. Start the Spring Boot Application

Run the application using IntelliJ IDEA or Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The application runs on:

```text
http://localhost:8080
```

### 3. Test the API

Using Postman, curl, or any REST client:

```bash
curl -X POST http://localhost:8080/v1/check ^
  -H "Content-Type: application/json" ^
  -d "{"apiKey":"abc123","limit":10,"windowSeconds":60}"
```

## Distributed Rate Limiting

The service keeps rate-limit state in Redis instead of local application memory.

This allows multiple instances of the Spring Boot application to share the same rate-limit state.

For example:

```text
              +------------------+
              |      Redis       |
              | Shared State     |
              +--------+---------+
                       |
             +---------+---------+
             |                   |
             v                   v
     +---------------+   +---------------+
     | Instance 1    |   | Instance 2    |
     | :8080         |   | :8081         |
     +---------------+   +---------------+
```

If the limit is `10`, sending requests across both instances still results in only the configured number of requests being allowed.

## Concurrency Testing

The project includes a test that sends **50 concurrent requests** against a bucket with a capacity of **10**.

Expected result:

```text
Allowed requests = 10
```

This verifies that the Redis Lua script performs the check-and-update operation atomically under concurrent access.

Run the tests with:

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

## Why Redis?

A local in-memory rate limiter would only maintain state inside one application instance.

Redis provides shared state:

```text
Instance 1 ──┐
              ├──> Redis
Instance 2 ──┘
```

Therefore, all application instances can enforce the same rate limit.

## Why Lua?

The rate-limit operation involves multiple steps:

```text
Read tokens
      ↓
Calculate refill
      ↓
Check availability
      ↓
Consume token
      ↓
Save state
```

If these operations were performed separately, concurrent requests could cause race conditions.

The Lua script executes the complete operation atomically inside Redis.

## Technology Stack

- **Java**
- **Spring Boot**
- **Spring Web**
- **Spring Data Redis**
- **Redis**
- **Lua**
- **JUnit 5**
- **Docker**
- **Maven**

## Project Structure

```text
rate-limiter-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/atharva/ratelimiter/
│   │   │       ├── config/
│   │   │       │   └── RedisConfig.java
│   │   │       ├── controller/
│   │   │       │   └── RateLimiterController.java
│   │   │       ├── dto/
│   │   │       │   └── RateLimitRequest.java
│   │   │       └── service/
│   │   │           └── RedisRateLimiter.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── rate-limit.lua
│   └── test/
│       └── java/
│           └── com/atharva/ratelimiter/
│               └── service/
│                   └── RedisRateLimiterTest.java
├── pom.xml
└── README.md
```

## Key Concepts Demonstrated

This project demonstrates practical backend concepts including:

- REST API design
- Rate limiting
- Token Bucket algorithm
- Redis data structures
- Redis Lua scripting
- Atomic operations
- Distributed state management
- Concurrent request handling
- Spring Boot integration with Redis
- Multi-instance application behavior

## Future Improvements

Possible extensions include:

- HTTP 429 responses with rate-limit headers
- Configurable limits per API key
- Authentication and authorization
- Rate-limit monitoring and metrics
- Sliding Window algorithm
- Distributed deployment behind a load balancer

These are intentionally outside the current scope of the project.

## License

This project is available for learning and portfolio purposes.
