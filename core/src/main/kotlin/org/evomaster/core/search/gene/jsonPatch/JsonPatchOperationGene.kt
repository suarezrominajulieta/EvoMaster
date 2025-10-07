package org.evomaster.core.search.gene.jsonPatch

import org.evomaster.core.output.OutputFormat
import org.evomaster.core.search.gene.Gene
import org.evomaster.core.search.gene.root.CompositeGene
import org.evomaster.core.search.gene.string.StringGene
import org.evomaster.core.search.gene.utils.GeneUtils
import org.evomaster.core.search.service.Randomness
import org.evomaster.core.search.service.mutator.genemutation.AdditionalGeneMutationInfo
import org.evomaster.core.search.service.mutator.genemutation.SubsetGeneMutationSelectionStrategy

class JsonPatchOperationGene(
    name: String,
    var op: StringGene = StringGene("op", "replace"),
    var path: StringGene = StringGene("path", "/example"),
    var value: Gene? = StringGene("value", "exampleValue"),
    var from: Gene? = null
) : CompositeGene(name, mutableListOf()) {

    companion object {
        private val VALID_OPS = listOf("add", "remove", "replace", "move", "copy", "test")
    }

    init {
        updateChildren()
    }

    private fun updateChildren() {
        // Primero eliminamos todos los hijos existentes
        getViewOfChildren().forEach { killChild(it) }

        // Agregamos los hijos actuales
        addChild(op)
        addChild(path)
        value?.let { addChild(it) }
        from?.let { addChild(it) }
    }

    override fun copyContent(): Gene {
        val clone = JsonPatchOperationGene(
            name,
            op.copy() as StringGene,
            path.copy() as StringGene,
            value?.copy(),
            from?.copy()
        )
        return clone
    }

    override fun getValueAsPrintableString(previousGenes: List<Gene>, mode: GeneUtils.EscapeMode?, targetFormat: OutputFormat?, extraCheck: Boolean): String {
        val sb = StringBuilder()
        sb.append("{")
        sb.append("\"op\":\"${op.value}\",\"path\":\"${path.value}\"")
        if (value != null) sb.append(",\"value\":${value!!.getValueAsPrintableString(previousGenes, mode, targetFormat)}")
        if (from != null) sb.append(",\"from\":\"${(from as StringGene).value}\"")
        sb.append("}")
        return sb.toString()
    }

    override fun isMutable(): Boolean = true

    override fun checkForLocallyValidIgnoringChildren(): Boolean = true

    override fun customShouldApplyShallowMutation(
        randomness: Randomness,
        selectionStrategy: SubsetGeneMutationSelectionStrategy,
        enableAdaptiveGeneMutation: Boolean,
        additionalGeneMutationInfo: AdditionalGeneMutationInfo?
    ): Boolean = true

    override fun containsSameValueAs(other: Gene): Boolean {
        if (other !is JsonPatchOperationGene) return false
        if (op.value != other.op.value) return false
        if (path.value != other.path.value) return false
        if ((value == null) != (other.value == null)) return false
        if (value != null && !value!!.containsSameValueAs(other.value!!)) return false
        if ((from == null) != (other.from == null)) return false
        if (from != null && (from as StringGene).value != (other.from as StringGene).value) return false
        return true
    }

    override fun setValueBasedOn(gene: Gene): Boolean {
        if (gene !is JsonPatchOperationGene) return false
        op.value = gene.op.value
        path.value = gene.path.value
        value = gene.value?.copy()
        from = gene.from?.copy()
        updateChildren()
        return true
    }

    override fun copyValueFrom(other: Gene): Boolean = setValueBasedOn(other)

    override fun randomize(randomness: Randomness, tryToForceNewValue: Boolean) {
        // Elegir operación válida al azar
        op.value = randomness.choose(VALID_OPS)

        // Generar un path aleatorio (string alfanumérico)
        path.value = "/" + randomAlphanumeric(randomness, 5)

        // Dependiendo de la operación, asignar 'value' o 'from'
        when (op.value) {
            "add", "replace", "test" -> {
                value = StringGene("value", randomAlphanumeric(randomness, 4))
                from = null
            }
            "move", "copy" -> {
                from = StringGene("from", "/" + randomAlphanumeric(randomness, 4))
                value = null
            }
            "remove" -> {
                value = null
                from = null
            }
        }

        updateChildren()
    }

    // Función helper para generar strings alfanuméricos
    private fun randomAlphanumeric(randomness: Randomness, length: Int): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { chars[randomness.nextInt(0, chars.length - 1)] }
            .joinToString("")
    }
}