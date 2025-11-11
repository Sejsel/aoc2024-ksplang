package cz.sejsel.ksplang.annotools

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.path
import cz.sejsel.ksplang.builder.AnnotatedKsplangTree
import kotlinx.serialization.json.Json
import kotlin.io.path.readText

class AnnoToolsCli : CliktCommand(name = "annotools") {
    override fun run() {}
}

class InfoCommand : CliktCommand(name = "info") {
    private val inputFile by argument(
        name = "input",
        help = "Input JSON file containing an AnnotatedKsplangTree"
    ).path(mustExist = true, canBeDir = false, mustBeReadable = true)

    override fun run() {
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }

        val jsonContent = inputFile.readText()
        val tree = json.decodeFromString<AnnotatedKsplangTree>(jsonContent)

        echo("Successfully deserialized AnnotatedKsplangTree from ${inputFile.fileName}")
        echo("Tree type: ${tree::class.simpleName}")

        when (tree) {
            is AnnotatedKsplangTree.Root -> {
                echo("Root node with ${tree.children.size} children")
            }
            is AnnotatedKsplangTree.Block -> {
                echo("Block: ${tree.name ?: "<unnamed>"}")
                echo("Block type: ${tree.type}")
                echo("Children: ${tree.children.size}")
            }
            is AnnotatedKsplangTree.Op -> {
                echo("Operation: ${tree.instruction}")
            }
        }
    }
}

fun main(args: Array<String>) {
    AnnoToolsCli()
        .subcommands(InfoCommand(), LocateCommand(), CallstackCommand(), BrowseCommand())
        .main(args)
}

