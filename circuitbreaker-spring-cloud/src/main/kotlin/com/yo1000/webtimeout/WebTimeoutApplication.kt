package com.yo1000.webtimeout

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.timelimiter.TimeLimiterConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.util.concurrent.CompletableFuture


@SpringBootApplication
class WebTimeoutApplication

fun main(args: Array<String>) {
	runApplication<WebTimeoutApplication>(*args)
}

@Configuration
class CircuitBreakerConfig {
	@Bean
	fun defaultCustomizer(): Customizer<Resilience4JCircuitBreakerFactory> {
		return Customizer<Resilience4JCircuitBreakerFactory> { factory ->
			factory.configureDefault { id ->
				Resilience4JConfigBuilder(id)
					.timeLimiterConfig(TimeLimiterConfig.custom()
						.timeoutDuration(Duration.ofSeconds(2)).build())
					.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
					.build()
			}
		}
	}
}

@RestController
class WebTimeoutController(
	private val circuitBreakerFactory: CircuitBreakerFactory<*, *>
) {
	@GetMapping
	fun get(
		@RequestParam(name = "delay", required = false, defaultValue = "0") delay: Long
	): CompletableFuture<ResponseEntity<String>> {
		return circuitBreakerFactory.create("slowRequest").run({
			Thread.sleep(delay)
			CompletableFuture.completedFuture(ResponseEntity
				.ok("0"))
		}, {
			throwable -> fallback(throwable)
		})
	}

	fun fallback(e: Throwable): CompletableFuture<ResponseEntity<String>> {
		e.printStackTrace()

		return CompletableFuture.completedFuture(ResponseEntity
			.status(HttpStatus.SERVICE_UNAVAILABLE)
			.body("1"))
	}
}
