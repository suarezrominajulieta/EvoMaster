package com.foo.rest.examples.spring.openapi.v3.xmlApplication

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter

@Configuration
open class XmlConfig {

    @Bean
    open fun jaxb2Converter(): HttpMessageConverter<*> {
        return Jaxb2RootElementHttpMessageConverter()
    }
}

@SpringBootApplication(exclude = [SecurityAutoConfiguration::class])
@RestController
@RequestMapping("/api/xml")
open class XmlApplication {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(XmlApplication::class.java, *args)
        }
    }

    // 1. receive XML, respond STRING
    @PostMapping(
        path = ["/receive-xml-respond-string"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun xmlToString(@Valid @RequestBody input: Person): ResponseEntity<String> {
        return if (input.age in 20..30) {
            ResponseEntity.ok("ok")
        } else {
            ResponseEntity.ok("not ok")
        }
    }

    // 2. receive STRING, respond XML
    @PostMapping(
        path = ["/receive-string-respond-xml"],
        consumes = [MediaType.TEXT_PLAIN_VALUE],
        produces = [MediaType.APPLICATION_XML_VALUE]
    )
    fun stringToXml(@Valid @RequestBody input: String): ResponseEntity<Person> {
        val name = input.trim()
        return ResponseEntity.ok(Person(name, age = 25))
    }

    @PostMapping(
        path = ["/company"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun companyEndpoint(@Valid @RequestBody company: Company): ResponseEntity<String> {
        return if (company.employees.size > 2 && company.employees.any { it.age > 40 }) {
            ResponseEntity.ok("big company with seniors")
        } else {
            ResponseEntity.ok("small company")
        }
    }

    @PostMapping(
        path = ["/employee"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun employeeEndpoint(@Valid @RequestBody emp: Employee): ResponseEntity<String> {
        return if (emp.role == Role.ADMIN && emp.person.age > 30) {
            ResponseEntity.ok("experienced admin")
        } else {
            ResponseEntity.ok("not admin or too young")
        }
    }
    // 3. Department
    @PostMapping(
        path = ["/department"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun departmentEndpoint(@RequestBody dept: Department): ResponseEntity<String> {
        return if (dept.employees.isEmpty()) {
            ResponseEntity.ok("empty department")
        } else {
            ResponseEntity.ok("department with ${dept.employees.size} employees")
        }
    }

    // 4. Organization
    @PostMapping(
        path = ["/organization"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun organizationEndpoint(@RequestBody org: Organization): ResponseEntity<String> {
        return ResponseEntity.ok("organization ${org.name} with ${org.people.size} people")
    }

    // 5. Tagged person
    @PostMapping(
        path = ["/tagged-person"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun taggedPersonEndpoint(@RequestBody tp: TaggedPerson): ResponseEntity<String> {
        return ResponseEntity.ok("tagged ${tp.person.name} with id ${tp.id}")
    }
}

@XmlRootElement(name = "person", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
open class Person(
    @field:XmlElement(namespace = "")
    var name: String = "",
    @field:XmlElement(namespace = "")
    var age: Int = 0
)

@XmlRootElement(name = "company")
data class Company(
    var name: String = "",
    var employees: List<Person> = mutableListOf()
)

enum class Role { ADMIN, USER, GUEST }

@XmlRootElement(name = "employee")
data class Employee(
    var person: Person = Person(),
    var role: Role = Role.USER
)

@XmlRootElement(name = "department")
data class Department(
    var name: String = "",
    var employees: List<Employee> = mutableListOf(),
    var subDepartments: List<Department> = mutableListOf()
)

@XmlRootElement(name = "organization")
data class Organization(
    var name: String = "",
    var people: List<Person> = mutableListOf(),
    var employees: List<Employee> = mutableListOf(),
    var companies: List<Company> = mutableListOf()
)

@XmlRootElement(name = "taggedPerson", namespace = "")
@XmlAccessorType(XmlAccessType.FIELD)
open class TaggedPerson(
    @XmlAttribute
    var id: String = "",
    @field:XmlElement(namespace = "")
    var person: Person = Person()
)