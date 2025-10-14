package org.evomaster.e2etests.spring.openapi.v3.jsonPatch

import com.foo.rest.examples.spring.openapi.v3.jsonPatch.JsonPatchController
import io.github.classgraph.AnnotationInfoList.emptyList
import org.evomaster.client.java.instrumentation.shared.ClassName
import org.evomaster.core.EMConfig
import org.evomaster.core.output.OutputFormat
import org.evomaster.core.problem.rest.data.HttpVerb
import org.evomaster.core.problem.rest.data.RestCallAction
import org.evomaster.e2etests.spring.openapi.v3.SpringTestBase
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.collections.emptyList

class JsonPatchTest : SpringTestBase() {

    companion object {
        @BeforeAll
        @JvmStatic
        fun init() {
            val config = EMConfig()
            initClass(JsonPatchController(), config)
        }
    }

    @Test
    fun testRunEM() {

        val className = ClassName("org.foo.JsonPatchEM")
        val outputFormat = OutputFormat.JAVA_JUNIT_5

        testRunEMGeneric(true, className, outputFormat)

    }

    fun testRunEMGeneric(basicAssertions: Boolean, className: ClassName, outputFormat: OutputFormat? = OutputFormat.JAVA_JUNIT_5){
        runTestHandlingFlakyAndCompilation(
            "JsonPatchEM",
            "org.foo.JsonPatchEM",
            10
        ) { args: List<String> ->

            val solution = initAndRun(args)
            assertTrue(solution.individuals.isNotEmpty())

            assertHasAtLeastOne(solution, HttpVerb.PATCH, 200, "/api/jsonPatch/patch", null)
            assertHasAtLeastOne(solution, HttpVerb.GET, 200, "/api/jsonPatch/patch", null)

            val patchRequests = solution.individuals
                .flatMap { ind -> ind.individual.seeAllActions() }
                .filter { action ->
                    action is RestCallAction &&
                            action.toString().contains("/api/jsonPatch/patch") &&
                            action.toString().contains("PATCH")
                }



            val validOps = listOf("add", "remove", "replace", "move", "copy", "test")

            patchRequests.forEach { req ->
                val body = req.toString()
                println(body)
                assertTrue(body.trim().startsWith("["), "El cuerpo del PATCH debe ser un array JSON: $body")

                val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
                val operations = try {
                    mapper.readTree(body)
                } catch (e: Exception) {
                    fail("El cuerpo del PATCH no es JSON válido: $body")
                }

                assertTrue(operations.isArray, "El cuerpo del PATCH debe ser un array de operaciones: $body")

                operations.forEach { opNode ->
                    val op = opNode.get("op")?.asText()
                    val path = opNode.get("path")?.asText()
                    val from = opNode.get("from")
                    val value = opNode.get("value")

                    assertNotNull(op, "Cada operación debe tener 'op': $body")
                    assertTrue(validOps.contains(op), "Operación 'op' inválida: $op")

                    assertNotNull(path, "Cada operación debe tener 'path': $body")
                    assertTrue(path?.startsWith("/") ?: true,  "El path debe empezar con '/': $path")

                    if (from != null) {
                        assertTrue(
                            op == "move" || op == "copy",
                            "'from' solo debe existir para operaciones move o copy, no para '$op': $body"
                        )
                    } else {
                        assertFalse(
                            op == "move" || op == "copy",
                            "'from' es obligatorio para move o copy: $body"
                        )
                    }

                    if (value != null) {
                        assertTrue(
                            op == "add" || op == "replace" || op == "test",
                            "'value' solo debe existir para add, replace o test, no para '$op': $body"
                        )
                    } else {
                        assertFalse(
                            op == "add" || op == "replace" || op == "test",
                            "'value' es obligatorio para add, replace o test: $body"
                        )
                    }
                }
            }
        }
    }
}