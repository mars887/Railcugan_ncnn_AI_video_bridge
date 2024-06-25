Этот проект не создавался с великой целью, а просто потому, что автору захотелось его сделать.
Однако это не значит, что он не будет обновляться в будущем. Если у вас есть идеи, как его улучшить, возможно, я добавлю это.

# Realcugan NCNN AI Shell

Этот проект является оболочкой для Realcugan NCNN AI. Основная цель — добавить поддержку видео (и когда-нибуть GIF файлами), а также максимально упростить использование программы.

## Основные функции

1. **Поддержка видео (и когда-нибуть GIF файлов)**: Возможность обрабатывать видео и GIF файлы наряду с изображениями.
2. **Упрощение использования**: Чтобы увеличить изображение, достаточно просто перетащить его на Jar файл. Первый параметр без ключа будет восприниматься как входной файл, а второй параметр без ключа — как выходной файл. Например, для параметров `[param1 -key1 param2 -key2 param3 param4]`:
   - `param1` будет воспринят как входной файл.
   - `param4` будет воспринят как выходной файл.
   - Порядок параметров также может быть изменен: `[-key1 param2 -key2 param3 param1 param4]`.
   - Либо с помощью ключей -in и -out соответственно.
   - Если имя выходного файла не указано, будет установленно на имя_файла_scaled.png

### Примеры использования

```shell
java -jar RealcuganNCNN.jar input_image.png output_image.png
java -jar RealcuganNCNN.jar -upng -dnl 3 -scale 4 input_image.png output_image.png
java -jar RealcuganNCNN.jar -dlog -scale 3 -ocrf 1 -gpus all input_image.png
```
### Ярлыки
Когда вы перетаскиваете какой-либо файл на иконку приложения, путь к этому файлу приложение принимает в виде параметра.
Соответственно, вы можете создать такой ярлык:
```shell
java -jar RealcuganNCNN.jar -s 4 -dnl 3
```
И при перетаскивании картинки на этот ярлык, программа запустится с такими параметрами:
```shell
java -jar RealcuganNCNN.jar -s 4 -dnl 3 картинка.png
```
А это соответственно один из примеров

### Настройка
При первом запуске программа запросит пути к **ffmpeg**, **ffprobe** и **realcugan.exe** через всплывающее окно. Также можно передать пути к ним в параметрах командной строки. Если все пути указаны правильно, программа создаст JSON файл с нужными данными в рабочей папке, и больше не будет запрашивать их при первом запуске.
```shell
java -jar RealcuganNCNN.jar
      -ffmpegPath /path/to/ffmpeg
      -ffprobePath /path/to/ffprobe
      -realcuganPath /path/to/realcugan.exe
      input_video.mp4 output_video.mp4
```

### параметры
- **ffmpegPath**:
  Указывает путь к ffmpeg.exe. Если не указан в параметрах или в properties.json, запрашивается у пользователя.
Псевдонимы: fp, ffmpegPath

- **realcuganPath**:
  Указывает путь к realcugan.exe. Если не указан в параметрах или в properties.json, запрашивается у пользователя.
Псевдонимы: rp, realcuganPath

- **ffprobePath**:
  Указывает путь к ffprobe.exe. Если не указан в параметрах или в properties.json, запрашивается у пользователя.
Псевдонимы: fpp, ffprobePath

- **scale**:
  Устанавливает множитель масштаба, применяемый к входному файлу.
Псевдонимы: s, scale
Значение по умолчанию: 2
Допустимые значения: от 2 до 4

- **realcuganThreads**:
  Указывает максимальное количество одновременно работающих realcugan.exe, умноженное на количество активных GPU.
Псевдонимы: realcuganThreads, rt
Значение по умолчанию: 3
Допустимые значения: от 1 до бесконечности

- **saveSettings**:
  Позволяет сохранять пути к исполняемым файлам в properties.json.
Псевдонимы: saveSettings, ss
Значение по умолчанию: true
Допустимые значения: true или false

- **inputPath**:
  Устанавливает входной файл.
Псевдонимы: inputPath, in, i

- **outputPath**:
  Устанавливает выходной файл.
Псевдонимы: outputPath, out, o

- **tempFilesPath**:
  Указывает путь к папке с временными файлами.
Псевдонимы: tempFilesPath, tempFolder, temp, t
Значение по умолчанию: /temp

- **outputVideoCrf**:
  Устанавливает CRF выходного видео.
Псевдонимы: outputVideoCrf, outputCrf, ocrf
Значение по умолчанию: 10
Допустимые значения: от 0 до 63

- **detailLog**:
  Включает/выключает детальное логирование.
Псевдонимы: detailLog, dlog
Значение по умолчанию: false

- **infoLog**:
  Включает/выключает логирование.
Псевдонимы: infoLog, ilog, log
Значение по умолчанию: true

- **allowedGpus**:
  Устанавливает GPU, разрешенные для обработки.
Псевдонимы: allowedGpus, gpus, ag
Допустимые значения: ID GPU через разделитель или "all"

- **outputVideoCodec**:
  Устанавливает видеокодек выходного видео.
  Псевдонимы: outputVideoCodec, ovc
Значение по умолчанию: такой же, как у входного

- **printFullParams**:
  Отображает полный список установленных параметров при запуске.
Псевдонимы: printFullParams, printAllParams, pfp
Значение по умолчанию: false
Допустимые значения: true или false

- **realcuganDenoiseLevel**:
  Копирует параметр '-n' из realcugan.exe.
Псевдонимы: realcuganDenoiseLevel, aiDenoiseLevel, denoiseLvl, dnl
Значение по умолчанию: 0
Допустимые значения: если scale == 2, от 0 до 3, иначе 0 или 3

- **usePngImages**:
  Использует PNG изображения при обработке.
Псевдонимы: outputImagePng, outAsPng, upng
Значение по умолчанию: false
Допустимые значения: true или false

- **autoReplaceOutput**:
  Если установлено в true, заменяет выходной файл без вопросов.
Псевдонимы: autoReplaceOutput, autoReplace, ar
Значение по умолчанию: false
Допустимые значения: true или false

- **help**:
  Выводит список комманд
  - -help ru  для русского
  - -help en  for english
