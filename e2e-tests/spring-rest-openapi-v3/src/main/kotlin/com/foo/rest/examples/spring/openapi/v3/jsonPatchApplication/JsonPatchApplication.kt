package com.foo.rest.examples.spring.openapi.v3.jsonPatchApplication

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
@RestController
@RequestMapping("/api/jsonPatch")
open class JsonPatchApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<JsonPatchApplication>(*args)
        }
    }

    private var person = Person("Alice", 30)

    @GetMapping("/patch", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPerson(): ResponseEntity<Person> = ResponseEntity.ok(person)

    @PatchMapping(
        path = ["/patch"],
        consumes = ["application/json-patch+json"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun patchPerson(@RequestBody patch: JsonNode): ResponseEntity<Person> {
        val mapper = ObjectMapper()

        // Simula la aplicación del patch (sin necesidad de librería externa)
        patch.forEach { op ->
            val path = op["path"]?.asText()
            val value = op["value"]
            val operation = op["op"]?.asText()

            if (path != null && value != null && operation == "replace") {
                when (path) {
                    "/name" -> person.name = value.asText()
                    "/age" -> person.age = value.asInt()
                }
            }
        }

        return ResponseEntity.ok(person)
    }
}

data class Person(
    var name: String = "",
    var age: Int = 0
)