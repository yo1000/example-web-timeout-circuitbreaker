package com.yo1000.webtimeout

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration

@SpringBootApplication
class WebTimeoutApplication

fun main(args: Array<String>) {
    runApplication<WebTimeoutApplication>(*args)
}

@ConfigurationProperties(prefix = "circuit-breaker")
class CircuitBreakerConfigurationProperties(
    var timeoutSeconds: Long = 2
)

@Configuration
@EnableConfigurationProperties(CircuitBreakerConfigurationProperties::class)
class CircuitBreakerConfig(
    private val props: CircuitBreakerConfigurationProperties
) {
    @Bean
    fun defaultCustomizer(): Customizer<Resilience4JCircuitBreakerFactory> {
        return Customizer<Resilience4JCircuitBreakerFactory> { factory ->
            factory.configureDefault { id ->
                Resilience4JConfigBuilder(id)
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(props.timeoutSeconds)).build())
                    .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                    .build()
            }
        }
    }
}

@Aspect
@Component
class WebTimeoutControllerCircuitBreaker(
    private val circuitBreakerFactory: CircuitBreakerFactory<*, *>
) {
    @Around("execution(* com.yo1000.webtimeout.WebTimeoutController+.*(..))")
    fun aroundController(joinPoint: ProceedingJoinPoint): Any? {
        return circuitBreakerFactory.create("slowRequest")
            .run({
                joinPoint.proceed()
            }, {
                throwable -> fallback(throwable)
            })
    }

    fun fallback(e: Throwable): ResponseEntity<String> {
        e.printStackTrace()

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("1")
    }
}

@RestController
class WebTimeoutController {
    @GetMapping
    fun get(
        @RequestParam(name = "delay", required = false, defaultValue = "0") delay: Long
    ): ResponseEntity<String> {
        Thread.sleep(delay)
        return ResponseEntity
            .ok("0")
    }
}
