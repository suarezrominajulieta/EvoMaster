package org.evomaster.core.search.gene

import org.evomaster.core.output.OutputFormat
import org.evomaster.core.search.gene.collection.PairGene
import org.evomaster.core.search.gene.string.StringGene
import org.evomaster.core.search.gene.utils.GeneUtils

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

        if (mode != GeneUtils.EscapeMode.XML) {
            // normal behavior for JSON, etc.
            return super.getValueAsPrintableString(previousGenes, mode, targetFormat, extraCheck)
        }

        val includedFields = fixedFields.filter { it.isPrintable() }
        val sb = StringBuilder()

        val attributes = includedFields
            .filter { attributeNames.contains(it.name) }
            .joinToString(" ") {
                "${it.name}=\"${it.getValueAsRawString()}\""
            }

        // ---- hijos normales (no atributos) ----
        val childFields = includedFields
            .filter { !attributeNames.contains(it.name) }

        if (childFields.isEmpty()) {
            // elemento sin hijos: <Tag a="1"/> o <Tag/>
            if (attributes.isEmpty()) {
                sb.append("<$name/>")
            } else {
                sb.append("<$name $attributes/>")
            }
            return sb.toString()
        }

        // elemento con hijos
        if (attributes.isEmpty()) {
            sb.append("<$name>")
        } else {
            sb.append("<$name $attributes>")
        }

        // imprimir cada hijo como tag interno
        childFields.forEach {
            sb.append("<${it.name}>")
            sb.append(it.getValueAsPrintableString(previousGenes, mode, targetFormat))
            sb.append("</${it.name}>")
        }

        sb.append("</$name>")
        return sb.toString()
    }
}