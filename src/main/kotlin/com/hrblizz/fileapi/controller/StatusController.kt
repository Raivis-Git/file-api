package com.hrblizz.fileapi.controller

import com.fasterxml.jackson.module.kotlin.readValue
import com.hrblizz.fileapi.data.entities.Entity
import com.hrblizz.fileapi.data.repository.EntityRepository
import com.hrblizz.fileapi.rest.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
class StatusController(
    private val entityRepository: EntityRepository
) {
    @RequestMapping("/status", method = [RequestMethod.GET])
    fun getStatus(): ResponseEntity<Map<String, Any>> {
        entityRepository.save(Entity(
            token = UUID.randomUUID().toString(),
            filename = "asd",
        ))

        return ResponseEntity(
            mapOf(
                "ok" to true
            ),
            HttpStatus.OK.value()
        )
    }
}
