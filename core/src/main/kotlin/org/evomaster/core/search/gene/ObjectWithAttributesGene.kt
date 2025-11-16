package org.evomaster.core.search.gene

import org.evomaster.core.output.OutputFormat
import org.evomaster.core.search.gene.collection.PairGene
import org.evomaster.core.search.gene.placeholder.CycleObjectGene
import org.evomaster.core.search.gene.string.StringGene
import org.evomaster.core.search.gene.utils.GeneUtils
import org.evomaster.core.search.gene.wrapper.OptionalGene

class ObjectWithAttributesGene(
    name: String,
    fixedFields: List<Gene>,
    refType: String? = null,
    isFixed: Boolean,
    template: PairGene<StringGene, Gene>? = null,
    additionalFields: MutableList<PairGene<StringGene, Gene>>? = null,
    val attributeNames: Set<String> = emptySet()
) : ObjectGene(name, fixedFields, refType, isFixed, template, additionalFields) {

    constructor(name: String, fields: List<Gene>, refType: String? = null) : this(
        name, fixedFields = fields, refType = refType, isFixed = true, template = null, additionalFields = null, attributeNames = emptySet()
    )

    override fun copyContent(): Gene {
        val copiedAdditional = additionalFields
            ?.map { it.copy() }
            ?.filterIsInstance<PairGene<StringGene, Gene>>()
            ?.toMutableList()

        return ObjectWithAttributesGene(
            name,
            fixedFields.map { it.copy() },
            refType,
            isFixed,
            template,
            copiedAdditional,
            attributeNames.toMutableSet()
        )
    }

    override fun getValueAsPrintableString(
        previousGenes: List<Gene>,
        mode: GeneUtils.EscapeMode?,
        targetFormat: OutputFormat?,
        extraCheck: Boolean
    ): String {

        // --- 1) Para JSON, x-www-form, boolean modes, etc → comportamiento normal
        if (mode != GeneUtils.EscapeMode.XML) {
            return super.getValueAsPrintableString(previousGenes, mode, targetFormat, extraCheck)
        }

        // --- 2) Recolectar los campos imprimibles igual que ObjectGene
        val includedFields = fixedFields
            .filter { it !is CycleObjectGene }
            .filter { it !is OptionalGene || (it.isActive && it.gene !is CycleObjectGene) }
            .filter { it.isPrintable() }

        // --- 3) Separar atributos de hijos
        val attributeFields = includedFields.filter { attributeNames.contains(it.name) }
        val childFields = includedFields.filter { !attributeNames.contains(it.name) }

        // --- 4) Preparar atributos en formato XML: key="value"
        fun xmlEscape(s: String): String =
            s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")

        fun printAttribute(field: Gene): String {
            val raw = field.getValueAsPrintableString(previousGenes, GeneUtils.EscapeMode.XML, targetFormat)
            return "${field.name}=\"${xmlEscape(raw.removeSurrounding("\""))}\""
        }

        val attributesString = attributeFields.joinToString(" ") { printAttribute(it) }

        val sb = StringBuilder()

        // --- 5) Caso sin hijos
        if (attributesString.isEmpty()) {
            sb.append("<$name>")
        } else {
            sb.append("<$name $attributesString>")
        }

        // --- 6) Contenido de hijos
        for (child in childFields) {

            // Valor inline si el hijo se llama "value"
            val childXml = child.getValueAsPrintableString(
                previousGenes,
                GeneUtils.EscapeMode.XML,
                targetFormat
            )

            val isInlineValue =
                child.name == "value" &&
                        !(child is ObjectWithAttributesGene)

            if (isInlineValue) {
                sb.append(childXml)
                continue
            }

            // ObjectWithAttributesGene genera su propio tag
            if (child is ObjectWithAttributesGene) {
                sb.append(childXml)
                continue
            }

            // Caso general: <child>value</child>
            sb.append("<${child.name}>")
            sb.append(childXml)
            sb.append("</${child.name}>")
        }

        sb.append("</$name>")
        return sb.toString()
    }
}