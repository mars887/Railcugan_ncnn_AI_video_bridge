package param

import java.util.HashMap

object ParamDescriptions {
    val paramDescriptions = linkedMapOf<ParamList.ParamKeys, HashMap<String, out Any>>(
        ParamList.ParamKeys.ffmpegPath to
                hashMapOf(
                    "en" to "indicates the path to ffmpeg.exe, if not specified in the parameters or in the properties.json, requested from the user",
                    "ru" to "Указывает путь до ffmpeg.exe, если не указан в параметрах или в properties.json, запрашивается у пользователя"
                ),
        ParamList.ParamKeys.realcuganPath to
                hashMapOf(
                    "en" to "indicates the path to realcugan.exe, if not specified in the parameters or in the properties.json, requested from the user",
                    "ru" to "Указывает путь до realcugan.exe, если не указан в параметрах или в properties.json, запрашивается у пользователя"
                ),
        ParamList.ParamKeys.ffprobePath to
                hashMapOf(
                    "en" to "indicates the path to ffprobe.exe, if not specified in the parameters or in the properties.json, requested from the user",
                    "ru" to "Указывает путь до ffprobe.exe, если не указан в параметрах или в properties.json, запрашивается у пользователя"
                ),
        ParamList.ParamKeys.scale to
                hashMapOf(
                    "en" to "sets the scale multiplier applied to the input file",
                    "ru" to "задаёт множитель масштаба, применяемого к входному файлу",
                    "values" to "range - 2 to 4",
                    "default" to 2
                ),
        ParamList.ParamKeys.realcuganThreads to
                hashMapOf(
                    "en" to "specifies the maximum number of simultaneously running realcugan.exe , multiplied by the number of active GPUs",
                    "ru" to "Указывает максимальное количество одновременно запущенных файлов realcugan.exe, умножается на количество активных GPU",
                    "values" to "range - 1 to infinity",
                    "default" to 3
                ),
        ParamList.ParamKeys.saveSettings to
                hashMapOf(
                    "en" to "allows saving executable file paths in properties.json",
                    "ru" to "разрешает сохранять пути исполняемых файлов в properties.json",
                    "values" to "true or false",
                    "default" to true
                ),
        ParamList.ParamKeys.inputPath to
                hashMapOf(
                    "en" to "sets the input file",
                    "ru" to "устанавливает входной файл"
                ),
        ParamList.ParamKeys.outputPath to
                hashMapOf(
                    "en" to "устанавливает выходной файл",
                    "ru" to "sets the output file",
                ),
        ParamList.ParamKeys.tempFilesPath to
                hashMapOf(
                    "en" to "specifies the path to the folder with temporary files",
                    "ru" to "указывает путь до папки с временными файлами",
                    "default" to "/temp"
                ),
        ParamList.ParamKeys.outputVideoCrf to
                hashMapOf(
                    "en" to "sets the CRF of the output video",
                    "ru" to "устанавливает crf выходного видео",
                    "values" to "range - 0 to 63",
                    "default" to 10
                ),
        ParamList.ParamKeys.detailLog to
                hashMapOf(
                    "en" to "enables/disables detailed logging",
                    "ru" to "включает\\выключает детальное логирование",
                    "default" to false
                ),
        ParamList.ParamKeys.infoLog to
                hashMapOf(
                    "en" to "enables/disables logging",
                    "ru" to "включает\\выключает логирование",
                    "default" to true
                ),
        ParamList.ParamKeys.allowedGpus to
                hashMapOf(
                    "en" to "sets the GPU allowed for processing",
                    "ru" to "устанавливает разрешенные для обработки GPU",
                    "values" to "gpus id via separator or \"all\""
                ),
        ParamList.ParamKeys.outputVideoCodec to
                hashMapOf(
                    "en" to "sets the output video codec",
                    "ru" to "устанавливает кодек выходного видео",
                    "default" to "same as input"
                ),
        ParamList.ParamKeys.printFullParams to
                hashMapOf(
                    "en" to "displays a complete list of the set parameters at startup",
                    "ru" to "выводит при запуске полный список установленных параметров",
                    "values" to "true or false",
                    "default" to false
                ),
        ParamList.ParamKeys.realcuganDenoiseLevel to
                hashMapOf(
                    "en" to "copies the '-n' parameter from realcugan.exe",
                    "ru" to "копирует параметр '-n' из realcugan.exe",
                    "values" to "if scale == 2 (range - 0 to 3) else 0 or 3",
                    "default" to 0
                ),
        ParamList.ParamKeys.usePngImages to
                hashMapOf(
                    "en" to "uses png images during processing",
                    "ru" to "использует png изображения во время обработки",
                    "values" to "true or false",
                    "default" to false
                ),
        ParamList.ParamKeys.autoReplaceOutput to
                hashMapOf(
                    "en" to "if set to true, replace the output file without question",
                    "ru" to "если установлено на true, заменить выходной файл без вопроса",
                    "values" to "true or false",
                    "default" to false
                ),
    )
}