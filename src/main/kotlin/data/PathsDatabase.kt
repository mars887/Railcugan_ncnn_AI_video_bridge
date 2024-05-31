package data

import App
import com.google.gson.Gson
import param.ParamList.ParamKeys
import param.Params
import param.getFrom
import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory

class PathsDatabase(val app: App) {

    var realcuganPath: String? = null
        private set

    var ffmpegPath: String? = null
        private set

    var ffprobePath: String? = null
        private set

    var inputPath: String? = null
        private set

    var outputPath: String? = null
        private set

    var tempFilesPath: String? = null
        private set

    private val exePath = System.getProperty("user.dir")
    private val pathOfJson = Paths.get("$exePath\\$DEFAULT_JSON_FILE_NAME")
    private val gson = Gson()


    private fun getOutputForExe(path: Path): String {
        if (!path.exists()) return ""
        ProcessBuilder(path.toString())
            .directory(path.parent.toFile())
            .start().run {
                val errors = errorReader()
                    .lines()
                    .toList()
                    .joinToString(separator = "\n")
                val inputs = inputReader()
                    .lines()
                    .toList()
                    .joinToString(separator = "\n")
                val out = errors + "\n" + inputs
                return out
            }
    }

    private fun requirerealcuganPath(): String {
        val path = Paths.get(openFileChooser("select REALCUGAN.exe path"))
        return if (path.exists() && path.extension == "exe") {
            path.toString()
        } else ""
    }

    private fun requireFfmpegPath(): String {
        val path = Paths.get(openFileChooser("select FFMPEG.exe path"))
        return if (path.exists() && path.extension == "exe") {
            path.toString()
        } else ""
    }

    private fun openFileChooser(windowName: String): String {
        val dialog = FileDialog(null as Frame?, windowName, FileDialog.LOAD)
        dialog.isVisible = true
        dialog.setFilenameFilter { _, name ->
            return@setFilenameFilter name.endsWith(".exe")
        }
        val output = dialog.directory + "\\" + dialog.file
        dialog.dispose()
        return output
    }

    fun initFields(trys: Int = 3): Int {
        val inited = _initFields(trys)
        if (inited == 1 && (app.params.getParam(ParamKeys.saveSettings) ?: "true") == "true") {
            val jsonValues = JsonValues(realcuganPath, ffmpegPath, ffprobePath)
            val jsonText = gson.toJson(jsonValues)
            Files.newBufferedWriter(pathOfJson).run {
                write(jsonText)
                flush()
                close()
            }
        }
        tempFilesPath = ParamKeys.tempFilesPath.getFrom(app.params)?.takeIf {
            if (it.isBlank()) false else {
                val path = Paths.get(it)
                Files.isDirectory(path) && Files.isWritable(path)
            }
        } ?: exePath
        tempFilesPath += if(tempFilesPath!!.endsWith("\\")) "temp\\" else "\\temp\\"
        inputPath = ParamKeys.inputPath.getFrom(app.params)
        outputPath = ParamKeys.outputPath.getFrom(app.params)
        return inited
    }

    private fun _initFields(trys: Int = 3): Int {
        var realcuganExeValidated = false
        var ffmpegExeValidated = false
        var ffprobeExeValidated = false
        if (pathOfJson.exists()) {
            try {
                val jsonText = Files.newBufferedReader(pathOfJson).lines().toList().joinToString(separator = "\n")
                val jsonValues = gson.fromJson(jsonText, JsonValues::class.java)

                realcuganPath = jsonValues.realcuganPath
                ffmpegPath = jsonValues.ffmpegPath
                ffprobePath = jsonValues.ffprobePath
            } catch (_: Exception) {
            }
        }
        val rp = app.params.getParam(ParamKeys.realcuganPath).run { if (isNullOrBlank()) null else this }
        if (rp != null && getOutputForExe(Paths.get(rp)).contains("realcugan-ncnn-vulkan")) {
            realcuganPath = rp
            realcuganExeValidated = true
        } else if (rp != null) {
            println("realcugan.exe path from params not valid")
        }
        val fp = app.params.getParam(ParamKeys.ffmpegPath).run { if (isNullOrBlank()) null else this }
        if (fp != null && getOutputForExe(Paths.get(fp)).contains("ffmpeg version")) {
            ffmpegPath = rp
            ffmpegExeValidated = true
        } else if (fp != null) {
            println("ffmpeg.exe path from params not valid")
        }
        val fpp = app.params.getParam(ParamKeys.ffprobePath).run { if (isNullOrBlank()) null else this }
        if (fpp != null && getOutputForExe(Paths.get(fpp)).contains("ffprobe version")) {
            ffprobePath = fpp
            ffprobeExeValidated = true
        } else if (fpp != null) {
            println("ffprobe.exe path from params not valid")
        }

        try {
            if (!realcuganExeValidated)
                repeat(trys) {
                    if (realcuganPath == null) realcuganPath = requirerealcuganPath()
                    val realcuganExeValid = getOutputForExe(Paths.get(realcuganPath!!))
                        .contains("realcugan-ncnn-vulkan")

                    if (!realcuganExeValid) {
                        realcuganPath = requirerealcuganPath()
                    } else {
                        realcuganExeValidated = true
                        return@repeat
                    }
                }

            if (!ffmpegExeValidated)
                repeat(trys) {
                    if (ffmpegPath == null)
                        ffmpegPath = requireFfmpegPath()
                    val ffmpegExeValid = getOutputForExe(Paths.get(ffmpegPath!!))
                        .contains("ffmpeg version")
                    if (!ffmpegExeValid) {
                        ffmpegPath = requireFfmpegPath()

                    } else {
                        ffmpegExeValidated = true
                        return@repeat
                    }
                }

            if (!ffprobeExeValidated)
                repeat(trys) {
                    if (ffprobePath == null)
                        ffprobePath = requireFprobePath()// replace
                    val ffprobeExeValid = getOutputForExe(Paths.get(ffprobePath!!))
                        .contains("ffprobe version")
                    if (!ffprobeExeValid) {
                        ffprobePath = requireFprobePath()// replace

                    } else {
                        ffprobeExeValidated = true
                        return@repeat
                    }
                }
        } catch (e: Exception) {
            app.openErrorDialog(e)
        }
        return if (ffmpegExeValidated && realcuganExeValidated && ffprobeExeValidated) 1 else 0
    }

    private fun requireFprobePath(): String {
        val path = Paths.get(openFileChooser("select FFPROBE.exe path"))
        return if (path.exists() && path.extension == "exe") {
            path.toString()
        } else ""
    }

    fun checkInputFile(): Boolean {
        if (inputPath.isNullOrBlank()) return false
        val path = Paths.get(inputPath!!)
        if (!path.exists()) return false
        if (!(AVAILABLE_INPUT_FORMATS.split(" ").contains(path.extension))) return false
        if (!Files.isReadable(path)) return false
        return Files.size(path) != 0L
    }

    fun readInputFilePath(trys: Int = 3): Boolean {
        repeat(trys) {
            val dialog = FileDialog(null as Frame?, "choose input file", FileDialog.LOAD)
            dialog.setFilenameFilter { dir, name ->
                AVAILABLE_INPUT_FORMATS.split(" ").contains(dir.toPath().extension)
            }
            dialog.isVisible = true
            inputPath = dialog.directory + dialog.file
            if (checkInputFile()) return true
        }
        return false
    }

    fun checkOutputFile(params: Params): Boolean {
        if (outputPath.isNullOrBlank()) return false
        var path = Paths.get(outputPath!!)

        if (path.isDirectory()) {
            convertOutputDirectoryToFile()
            path = Paths.get(outputPath!!)
        }
        if(path.extension.isBlank()) {
            outputPath = outputPath + "." + Paths.get(inputPath!!).extension
            path = Paths.get(outputPath!!)
        }

        if (!AVAILABLE_INPUT_FORMATS.split(" ").contains(path.extension)) {
            outputPath += Paths.get(inputPath!!).extension
        }

        if (path.exists())
            return ((ParamKeys.autoReplaceOutput.getFrom(params)?: "false") == "true")
                    || app.resolveOutputPathExists(path)
        return true
    }

    fun readOutputFilePath(trys: Int = 3,params: Params): Boolean {
        repeat(trys) {
            val dialog = FileDialog(null as Frame?, "choose output file or directory", FileDialog.SAVE)
            dialog.isVisible = true
            outputPath = dialog.directory + dialog.file
            if (checkOutputFile(params)) return true
        }
        return false
    }

    private fun convertOutputDirectoryToFile() {
        val inputPath = Paths.get(inputPath!!)
        outputPath = (outputPath.toString() + "\\" +
                inputPath.fileName.toString().dropLast(inputPath.fileName.extension.length + 1)) +
                "_scaled." + inputPath.extension
    }

    companion object {
        const val DEFAULT_JSON_FILE_NAME = "properties.json"
        const val AVAILABLE_INPUT_FORMATS = "mp4 mov jpg png jpeg"
    }
}