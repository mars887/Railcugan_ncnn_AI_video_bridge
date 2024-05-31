import data.PathsDatabase
import exceptions.InputFileNotSettedOrNotValid
import exceptions.OutputFileExistAndYourChoice
import exceptions.OutputFileNotSettedOrNotValid
import exceptions.PathsOfExeNotInitializedException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import param.ParamList
import param.Params
import param.getFrom
import java.io.BufferedReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.system.exitProcess

class App(internal val params: Params, val reader: BufferedReader) {

    public val infoLog = Channel<String>(1024, BufferOverflow.DROP_OLDEST)
    public val detailLog = Channel<String>(1024, BufferOverflow.DROP_OLDEST)

    private val pathsDatabase = PathsDatabase(this)
    private val ioScope = CoroutineScope(EmptyCoroutineContext + Dispatchers.IO)

    fun run() {
        if (pathsDatabase.initFields() == 0) throw PathsOfExeNotInitializedException()

        println("Clearing temp files...")
        Paths.get(pathsDatabase.tempFilesPath!!).treeRemove()

        println("ffmpeg path     -> ${pathsDatabase.ffmpegPath}")
        println("ffprobe path    -> ${pathsDatabase.ffprobePath}")
        println("realcugan path  -> ${pathsDatabase.realcuganPath}")
        println("temp files path -> ${pathsDatabase.tempFilesPath}")

        if (!pathsDatabase.checkInputFile()) if (!pathsDatabase.readInputFilePath()) throw InputFileNotSettedOrNotValid()
        if (!pathsDatabase.checkOutputFile(params)) if (!pathsDatabase.readOutputFilePath(params = params)) throw OutputFileNotSettedOrNotValid()

        println("input file      -> ${pathsDatabase.inputPath}")
        println("output file     -> ${pathsDatabase.outputPath}")

        if(ParamList.ParamKeys.printFullParams.getFrom(params) == "true") {
            println(" -- Params -- ")
            params.forEachParam { paramKeys, s ->
                println("  [" + "$paramKeys".toGreen() + "] = $s")
            }
            println(" ------------ ")
        }

        println("starting".toGreen())

        val process = ProcessRouter.route(pathsDatabase)

        if (ParamList.ParamKeys.infoLog.getFrom(params) == "true")
            ioScope.launch {
                while (true)
                    infoLog.consumeEach {
                        println("info -> $it")
                    }
            }
        if (ParamList.ParamKeys.detailLog.getFrom(params) == "true")
            ioScope.launch {
                while (true)
                    detailLog.consumeEach {
                        println("detail -> $it")
                    }
            }

        runBlocking {
            process.apply(params)
        }
        println("Clearing temp files...")
        Paths.get(pathsDatabase.tempFilesPath!!).treeRemove()
        println("finished".toGreen())
        exitProcess(0)
    }

    fun openErrorDialog(e: Throwable) {
        e.printStackTrace()
    }

    fun resolveOutputPathExists(path: Path, trys: Int = 3): Boolean {
        print("output file \"$path\" exists... \nreplace it [yes,replace,rep,r,y]\nchoose another path [choose,another,c,a]\nexit [exit,e]\nyour choice: ")
        val yesWords = arrayOf("yes", "y", "da", "да", "ага", "угу", "replace", "overwrite")
        val anotherWords = arrayOf("choose", "ch", "c", "another", "a")
        val noWords = arrayOf("no", "n", "net", "нет", "ноу", "не", "выйти", "exit", "e")
        repeat(trys) {
            val line = reader.readLine()
            if (yesWords.contains(line)) return true
            if (anotherWords.contains(line)) return false
            if (noWords.contains(line)) throw OutputFileExistAndYourChoice()
        }
        throw OutputFileExistAndYourChoice()
    }
}

private fun Path.treeRemove() {
    if(!exists()) return
    if(isDirectory()) {
        Files.list(this).forEach {
            it.treeRemove()
        }
        Files.delete(this)
    } else {
        println("deleting $this")
        Files.delete(this)
    }
}

public fun <E> Channel<E>.sent(message: E) {
    runBlocking {
        this@sent.send(message)
    }
}