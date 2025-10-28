package org.evomaster.core.search.gene.jsonPatch

import org.evomaster.core.output.OutputFormat
import org.evomaster.core.search.gene.Gene
import org.evomaster.core.search.gene.root.CompositeGene
import org.evomaster.core.search.gene.utils.GeneUtils
import org.evomaster.core.search.service.Randomness
import org.evomaster.core.search.service.mutator.genemutation.AdditionalGeneMutationInfo
import org.evomaster.core.search.service.mutator.genemutation.SubsetGeneMutationSelectionStrategy
import org.slf4j.LoggerFactory


class JsonPatchGene(name: String) : CompositeGene(name, mutableListOf()) {

    companion object {
        private val log = LoggerFactory.getLogger(JsonPatchGene::class.java)
        private val VALID_OPS = listOf("add", "remove", "replace", "move", "copy", "test")
    }

    val operations: List<JsonPatchOperationGene>
        get() = getViewOfChildren().filterIsInstance<JsonPatchOperationGene>()

    override fun copyContent(): Gene {
        val clone = JsonPatchGene(name)

        operations.forEach { op ->
            clone.addChild(op.copy() as JsonPatchOperationGene)
        }

        return clone
    }

    override fun randomize(randomness: Randomness, tryToForceNewValue: Boolean) {
        getViewOfChildren().toList().forEach { killChild(it) }
        val numOps = randomness.nextInt(1, 5)
        repeat(numOps) { idx ->
            val opGene = JsonPatchOperationGene("op_$idx")
            opGene.randomize(randomness, tryToForceNewValue)
            opGene.doInitialize(randomness)
            addChild(opGene)
        }
    }


    override fun getValueAsPrintableString(previousGenes: List<Gene>, mode: GeneUtils.EscapeMode?, targetFormat: OutputFormat?, extraCheck: Boolean): String {
        val opsStr = operations.joinToString(",") {
            it.getValueAsPrintableString(previousGenes, mode, targetFormat)
        }
        return "[$opsStr]"
    }

    override fun isMutable(): Boolean = true

    override fun mutationWeight(): Double = operations.size.toDouble()

    override fun checkForLocallyValidIgnoringChildren(): Boolean = true

    override fun customShouldApplyShallowMutation(
        randomness: Randomness,
        selectionStrategy: SubsetGeneMutationSelectionStrategy,
        enableAdaptiveGeneMutation: Boolean,
        additionalGeneMutationInfo: AdditionalGeneMutationInfo?
    ): Boolean = true

    override fun containsSameValueAs(other: Gene): Boolean {
        if (other !is JsonPatchGene) return false
        if (operations.size != other.operations.size) return false
        return operations.zip(other.operations).all { (a, b) -> a.containsSameValueAs(b) }
    }

    @Deprecated("Not used")
    override fun setValueBasedOn(gene: Gene): Boolean {
        if (gene !is JsonPatchGene) return false
        getViewOfChildren().toList().forEach { killChild(it) }
        gene.operations.forEach { addChild(it.copy() as JsonPatchOperationGene) }
        return true
    }

    override fun copyValueFrom(other: Gene): Boolean = setValueBasedOn(other)
}

