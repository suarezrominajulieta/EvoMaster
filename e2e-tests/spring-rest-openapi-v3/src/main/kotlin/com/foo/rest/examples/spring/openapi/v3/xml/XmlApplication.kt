package com.foo.rest.examples.spring.openapi.v3.xml

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
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

    // 1. Receive XML, respond STRING
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

    // 2. Receive STRING, respond XML
    @PostMapping(
        path = ["/receive-string-respond-xml"],
        consumes = [MediaType.TEXT_PLAIN_VALUE],
        produces = [MediaType.APPLICATION_XML_VALUE]
    )
    fun stringToXml(@Valid @RequestBody input: String): ResponseEntity<Person> {
        val name = input.trim()
        return ResponseEntity.ok(Person(name, age = 25))
    }

    // 3. Company
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

    //4. Employee
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

    // 5. Department
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

    // 6. Organization
    @PostMapping(
        path = ["/organization"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun organizationEndpoint(@RequestBody org: Organization): ResponseEntity<String> {
        return ResponseEntity.ok("organization ${org.name} with ${org.people.size} people")
    }

    // 7. Tagged person
    @PostMapping(
        path = ["/tagged-person"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun taggedPersonEndpoint(@Valid @RequestBody tp: TaggedPerson): ResponseEntity<String> {
        return ResponseEntity.ok("tagged ${tp.person.name} with id ${tp.id}")
    }

    // 8. Employee with attribute in nested Person
    @PostMapping(
        path = ["/employee-with-attr"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun employeeWithAttr(@RequestBody emp: EmployeeWithAttr): ResponseEntity<String> {
        return ResponseEntity.ok("employee ${emp.person.name} has id ${emp.person.id}")
    }

    // 9. Group with list of persons with attributes
    @PostMapping(
        path = ["/group-with-attrs"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun groupWithAttrs(@RequestBody group: Group): ResponseEntity<String> {
        val ids = group.people.joinToString(",") { it.id }
        return ResponseEntity.ok("group ${group.name} has ids: $ids")
    }

    // 10. Project with root and nested attributes
    @PostMapping(
        path = ["/project"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.TEXT_PLAIN_VALUE]
    )
    fun projectEndpoint(@RequestBody project: Project): ResponseEntity<String> {
        val employeeIds = project.members.joinToString(",") { it.id }
        return ResponseEntity.ok("project ${project.code} has members: $employeeIds")
    }

    // 11. List of Projects with only list
    @PostMapping(
        path = ["/projects"],
        consumes = [MediaType.APPLICATION_XML_VALUE],
        produces = [MediaType.APPLICATION_XML_VALUE]
    )
    fun receiveProjectList(@RequestBody list: ProjectList): ProjectList {
        return list
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

@XmlRootElement(name = "employeeWithAttr")
@XmlAccessorType(XmlAccessType.FIELD)
open class EmployeeWithAttr(
    @field:XmlElement(namespace = "")
    var role: Role = Role.USER,

    @field:XmlElement(namespace = "")
    var person: PersonWithAttr = PersonWithAttr()
)

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "personWithAttr")
open class PersonWithAttr(
    @XmlAttribute
    var id: String = "",
    @field:XmlElement(namespace = "")
    var name: String = "",
    @field:XmlElement(namespace = "")
    var age: Int = 0
)

@XmlRootElement(name = "group")
@XmlAccessorType(XmlAccessType.FIELD)
open class Group(
    @field:XmlElement(namespace = "")
    var name: String = "",
    var people: List<PersonWithAttr> = mutableListOf()
)

@XmlRootElement(name = "project")
@XmlAccessorType(XmlAccessType.FIELD)
open class Project(
    @XmlAttribute
    var code: String = "",
    @field:XmlElement(name = "member", namespace = "")
    var members: List<Member> = mutableListOf()
)

@XmlRootElement(name = "list")
@XmlAccessorType(XmlAccessType.FIELD)
open class ProjectList(
    @field:XmlElement(name = "project", namespace = "")
    var projects: MutableList<Project> = mutableListOf()
)

@XmlAccessorType(XmlAccessType.FIELD)
open class Member(
    @XmlAttribute
    var id: String = "",
    @field:XmlElement(namespace = "")
    var name: String = ""
)