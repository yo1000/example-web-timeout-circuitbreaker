Timeout Circuit Breaker Examples
================================


spring-cloud-starter-circuitbreaker-resilience4j
------------------------------------------------

### How to Run

First, build sources and run application.

```
./mvnw clean spring-boot:run -pl circuitbreaker-spring-cloud
```

Second, access following url with `delay=0` parameter.

```
curl -vvvv -XGET 'localhost:8080?delay=0'
```

Third, access following url with `delay=2000` parameter.

```
curl -vvvv -XGET 'localhost:8080?delay=2000'
```

When delay time is greater than 2 seconds, then can confirm to work to circuit breaking.


resilience4j-spring-boot2
-------------------------

### How to Run

First, build sources and run application.

```
./mvnw clean spring-boot:run -pl circuitbreaker-resilience4j
```

Second, access following url with `delay=0` parameter.

```
curl -vvvv -XGET 'localhost:8081?delay=0'
```

Third, access following url with `delay=2000` parameter.

```
curl -vvvv -XGET 'localhost:8081?delay=2000'
```

When delay time is greater than 2 seconds, then can confirm to work to circuit breaking.
