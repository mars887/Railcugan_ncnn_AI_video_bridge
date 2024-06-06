package param

object ParamList {
    val paramsList = listOf(
        Param(
            ParamKeys.ffmpegPath,
            arrayOf("fp", "ffmpegPath"),
            null
        ),
        Param(
            ParamKeys.realcuganPath,
            arrayOf("rp", "realcuganPath"),
            null
        ),
        Param(
            ParamKeys.ffprobePath,
            arrayOf("fpp", "ffprobePath"),
            null
        ),
        Param(
            ParamKeys.scale,
            arrayOf("s", "scale"),
            2
        ),
        Param(
            ParamKeys.realcuganThreads,
            arrayOf("realcuganThreads", "rt"),
            2
        ),
        Param(
            ParamKeys.saveSettings,
            arrayOf("saveSettings", "ss"),
            true
        ),
        Param(
            ParamKeys.inputPath,
            arrayOf("inputPath", "in", "i"),
            null
        ),
        Param(
            ParamKeys.outputPath,
            arrayOf("outputPath", "out", "o"),
            null
        ),
        Param(
            ParamKeys.tempFilesPath,
            arrayOf("tempFilesPath", "tempFolder", "temp", "t"),
            null
        ),
        Param(
            ParamKeys.outputVideoCrf,
            arrayOf("outputVideoCrf", "outputCrf", "ocrf"),
            10
        ),
        Param(
            ParamKeys.detailLog,
            arrayOf("detailLog", "dlog"),
            false,
            true
        ),
        Param(
            ParamKeys.infoLog,
            arrayOf("infoLog", "ilog", "log"),
            true,
            true
        ),
        Param(
            ParamKeys.allowedGpus,
            arrayOf("allowedGpus", "gpus","ag"),
            null,
            "all"
        ),
        Param(
            ParamKeys.outputVideoCodec,
            arrayOf("outputVideoCodec", "ovc"),
            null
        ),
        Param(
            ParamKeys.printFullParams,
            arrayOf("printFullParams", "printAllParams","pfp"),
            false,
            true
        ),
        Param(
            ParamKeys.realcuganDenoiseLevel,
            arrayOf("realcuganDenoiseLevel", "aiDenoiseLevel","denoiseLvl","dnl"),
            null,
            3
        ),
        Param(
            ParamKeys.usePngImages,
            arrayOf("outputImagePng","outAsPng","upng"),
            false,
            true
        ),
        Param(
            ParamKeys.autoReplaceOutput,
            arrayOf("autoReplaceOutput","autoReplace","ar"),
            false,
            true
        )
    )

    enum class ParamKeys(val key: String) {
        realcuganPath("realcuganPath"),
        ffmpegPath("ffmpegPath"),
        ffprobePath("ffprobePath"),
        scale("scale"),
        realcuganThreads("realcuganThreads"),
        saveSettings("saveSettings"),
        outputPath("outputPath"),
        inputPath("inputPath"),
        tempFilesPath("tempFilePath"),
        outputVideoCrf("outputVideoCrf"),
        detailLog("detailLog"),
        infoLog("infoLog"),
        allowedGpus("allowedGpus"),
        outputVideoCodec("outputVideoCodec"),
        printFullParams("printFullParams"),
        realcuganDenoiseLevel("realcuganDenoiseLevel"),
        usePngImages("outputImagePng"),
        autoReplaceOutput("autoReplaceOutput"),
    }

    data class Param(
        val realKey: ParamKeys,
        val keys: Array<String>,
        var defValue: String,
        var defIfExitst: String
    ) {
        constructor(
            realKey: ParamKeys,
            keys: Array<String>,
            defValue: Any?,
            defIfExitst: Any? = null
        ) : this(realKey, keys, (defValue ?: "").toString(),(defIfExitst?: "").toString())
    }
}