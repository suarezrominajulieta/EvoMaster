package org.evomaster.core.search.gene.xml

import org.evomaster.core.search.gene.BooleanGene
import org.evomaster.core.search.gene.ObjectGene
import org.evomaster.core.search.gene.ObjectWithAttributesGene
import org.evomaster.core.search.gene.numeric.IntegerGene
import org.evomaster.core.search.gene.string.StringGene
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.evomaster.core.search.gene.utils.GeneUtils

class ObjectWithAttributesTest {

    @Test
    fun testXmlPrintWithAttributes1() {

        val person = ObjectWithAttributesGene(
            name = "person",
            fixedFields = listOf(
                StringGene("id", value = "123"), // attribute
                ObjectWithAttributesGene(
                    name = "address",
                    fixedFields = listOf(
                        StringGene("city", value = "Rome"),
                        StringGene("country", value = "IT") // attribute
                    ),
                    isFixed = true,
                    attributeNames = setOf("country")
                )
            ),
            isFixed = true,
            attributeNames = setOf("id")
        )

        val actual = person.getValueAsPrintableString(mode = GeneUtils.EscapeMode.XML)
        val expected = "<person id=\"123\"><address country=\"IT\"><city>Rome</city></address></person>"

        assertEquals(expected, actual)
    }


    @Test
    fun testXmlPrintWithAttribute2() {

        val person = ObjectWithAttributesGene(
            name = "parent",
            fixedFields = listOf(
                StringGene("attrib1", value = "true"),
                ObjectWithAttributesGene(
                    name = "child1",
                    fixedFields = listOf(
                        StringGene("attrib2", value = "-1"),
                        StringGene("attrib3", value = "bar"),
                        IntegerGene("value", value = 42)
                    ),
                    isFixed = true,
                    attributeNames = setOf("attrib2","attrib3")
                ),
                StringGene("child2", value = "foo"),
            ),
            isFixed = true,
            attributeNames = setOf("attrib1")
        )

        val actual = person.getValueAsPrintableString(mode = GeneUtils.EscapeMode.XML)
        val expected = "<parent attrib1=\"true\"><child1 attrib2=\"-1\" attrib3=\"bar\">42</child1><child2>foo</child2></parent>"

        assertEquals(expected, actual)
    }

    @Test
    fun testXmlPrintWithAttribute3() {

        val person = ObjectWithAttributesGene(
            name = "user",
            fixedFields = listOf(
                IntegerGene("id", value = 123),
                StringGene("name", value = "Alice"),
                StringGene("email", value = "alice@example.com")
            ),
            isFixed = true,
            attributeNames = setOf("id", "name")
        )

        val actual = person.getValueAsPrintableString(mode = GeneUtils.EscapeMode.XML)
        val expected = "<user id=\"123\" name=\"Alice\"><email>alice@example.com</email></user>"

        assertEquals(expected, actual)
    }



    @Test
    fun testXmlPrintWithAttribute5() {

        val person = ObjectWithAttributesGene(
            name = "anInteger",
            fixedFields = listOf(
                IntegerGene("anInteger",value = 42),
                BooleanGene("attrib1", value = false)
            ),
            isFixed = true,
            attributeNames = setOf("attrib1")
        )


        val actual = person.getValueAsPrintableString(mode = GeneUtils.EscapeMode.XML)
        val expected = "<anInteger attrib1=\"false\"><anInteger>42</anInteger></anInteger>"
        assertEquals(expected,actual)

    }

    @Test
    fun testXmlPrintWithAttribute6() {

        val person = ObjectWithAttributesGene(
            name = "anInteger",
            fixedFields = listOf(
                IntegerGene("value",value = 42),
                BooleanGene("attrib1", value = false)
            ),
            isFixed = true,
            attributeNames = setOf("attrib1")
        )


        val actual = person.getValueAsPrintableString(mode = GeneUtils.EscapeMode.XML)
        val expected = "<anInteger attrib1=\"false\">42</anInteger>"
        assertEquals(expected,actual)

    }

    @Test
    fun testXmlPrintWithAttribute4() {

        val person = IntegerGene("anInteger",value=42)


        val actual = person.getValueAsPrintableString(mode = GeneUtils.EscapeMode.XML)
        val expected = "<anInteger>42</anInteger>"

        assertEquals(expected, actual)
    }

    @Test
    fun testXmlPrintWithAttribute7() {

        val person = ObjectWithAttributesGene(
            name = "anElement",
            fixedFields = listOf(
                BooleanGene("anAttribute",value = false),
                IntegerGene("value", value = 42)
            ),
            isFixed = true,
            attributeNames = setOf("anAttribute")
        )


        val actual = person.getValueAsPrintableString(mode = GeneUtils.EscapeMode.XML)
        val expected = "<anElement anAttribute=\"false\">42</anElement>"

        assertEquals(expected, actual)
    }

    @Test
    fun testXmlPrintWithAttribute8() {

        val person = ObjectWithAttributesGene(
            name = "outerElement",
            fixedFields = listOf(
                StringGene("outerAttrib",value = "foo"),
                ObjectWithAttributesGene("innerElement",
                    listOf(
                        StringGene("innerAttrib", value = "bar")),
                    isFixed = true,
                    attributeNames = setOf("innerAttrib"))
            ),
            isFixed = true,
            attributeNames = setOf("outerAttrib")
        )


        val actual = person.getValueAsPrintableString(mode = GeneUtils.EscapeMode.XML)
        val expected = "<outerElement outerAttrib=\"foo\"><innerElement innerAttrib=\"bar\"></innerElement></outerElement>"

        assertEquals(expected, actual)
    }

}