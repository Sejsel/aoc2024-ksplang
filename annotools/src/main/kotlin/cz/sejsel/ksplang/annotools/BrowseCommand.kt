package cz.sejsel.ksplang.annotools

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
import cz.sejsel.ksplang.builder.AnnotatedKsplangTree
import kotlinx.serialization.json.Json
import kotlin.collections.forEach
import kotlin.io.path.readText

class BrowseCommand : CliktCommand(name = "browse") {
    private val inputFile by option(
        "--ksplang", help = "Input JSON file containing an AnnotatedKsplangTree"
    ).path(mustExist = true, canBeDir = false, mustBeReadable = true).required()

    private val listFunctions by option(
        "--functions", help = "List top-level functions"
    ).flag(default = false)

    override fun run() {
        val json = Json {
            ignoreUnknownKeys = true
        }

        val jsonContent = inputFile.readText()
        val tree = json.decodeFromString<AnnotatedKsplangTree>(jsonContent) as AnnotatedKsplangTree.Root

        if (listFunctions) {
            echo("Functions:")
            tree.children.forEach { child ->
                when (child) {
                    is AnnotatedKsplangTree.Block -> {
                        val name = child.name ?: "<unnamed>"
                        if (name.startsWith("fun_wrapper_")) {
                            echo("\tBlock: $name (${child.type})")
                        }
                    }

                    is AnnotatedKsplangTree.Op -> {}

                    is AnnotatedKsplangTree.Root -> error("root as child")
                }
            }
        }
    }
}