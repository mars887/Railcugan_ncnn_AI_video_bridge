import exceptions.InputFileNotSettedOrNotValid
import exceptions.OutputFileExistAndYourChoice
import exceptions.OutputFileNotSettedOrNotValid
import exceptions.PathsOfExeNotInitializedException
import param.ParamDescriptions
import param.ParamList
import param.ParamList.ParamKeys
import param.ParamsParser
import param.getFrom
import java.io.BufferedReader
import java.io.InputStreamReader

lateinit var app: App

fun main(_args: Array<String>) {
    val reader = BufferedReader(InputStreamReader(System.`in`))
    val args = if (_args.isEmpty()) {
        reader.readLine().split(" ").toTypedArray()
    } else _args

    if (args.contains("-help")) {
        val lang = if (args.contains("ru")) "ru" else "en"
        ParamDescriptions.paramDescriptions.forEach { t, map ->
            println("[".toRed() + " $t ".toGreen() + "]".toRed())
            println("- Description: ".toCyan() + "${map[lang]}")
            println("- Aliases: ".toCyan() + (ParamList.paramsList.find { it.realKey == t }?.keys?.joinToString(separator = ", ".toCyan()) ?: ""))
            if (map.containsKey("default")) println("- Default value: ".toCyan() + "${map["default"]}")
            if (map.containsKey("values")) println("- Allowed values: ".toCyan() + " ${map["values"]}")
            println()
        }
        return
    }

    val params = ParamsParser.parse(args)
    app = App(params, reader)
    try {
        app.run()
    } catch (e: Throwable) {
        outAppError(e)
    }
}

fun outAppError(e: Throwable) {
    when (e) {
        is PathsOfExeNotInitializedException -> {
            println("some of the files needed to work have not been initialized".toRed())
        }

        is InputFileNotSettedOrNotValid -> {
            println("the input file was not selected after several attempts or another error related to the input file".toRed())
        }

        is OutputFileNotSettedOrNotValid -> {
            println("the output file was not selected after several attempts or another error related to the output file".toRed())
        }

        is OutputFileExistAndYourChoice -> {
            println("the output file already exists, and the output is selected")
        }

        else -> {
            e.printStackTrace()
        }
    }
}
