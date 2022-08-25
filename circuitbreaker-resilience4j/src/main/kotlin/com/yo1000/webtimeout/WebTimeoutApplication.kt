package com.yo1000.webtimeout

import io.github.resilience4j.bulkhead.annotation.Bulkhead
import io.github.resilience4j.timelimiter.annotation.TimeLimiter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.CompletableFuture

@SpringBootApplication
class WebTimeoutApplication

fun main(args: Array<String>) {
    runApplication<WebTimeoutApplication>(*args)
}

@RestController
class WebTimeoutController {
    @TimeLimiter(name = "backendTest", fallbackMethod = "fallback")
    @Bulkhead(name="slowRequest", type=Bulkhead.Type.THREADPOOL)
    @GetMapping
    fun get(
        @RequestParam(name = "delay", required = false, defaultValue = "0") delay: Long
    ): CompletableFuture<ResponseEntity<String>> {
        Thread.sleep(delay)
        return CompletableFuture.completedFuture(ResponseEntity
            .ok("0"))
    }

    fun fallback(e: Throwable): CompletableFuture<ResponseEntity<String>> {
        e.printStackTrace()

        return CompletableFuture.completedFuture(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("1"))
    }
}
