package processes

import ImageData
import ImageToDataProcessor
import SameHashFilter
import app
import data.PathsDatabase
import exes.ConcatFramesToVideo
import exes.RealcuganHelper
import exes.SplitIntoFrames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import param.ParamList
import param.Params
import param.getFrom
import sent
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

class MP4Process(private val paths: PathsDatabase) : DefaultProcess() {
    override suspend fun apply(params: Params) {
        val scope = CoroutineScope(EmptyCoroutineContext + Dispatchers.IO)
        val imgToDataProcessor = ImageToDataProcessor(8, 512, LQHashSize = 64, ULQHashSize = 48)
        val sameHashFilter = SameHashFilter(0)
        val imageDatabase = LinkedHashSet<ImageData>()

        val imagesToUpscale = MutableSharedFlow<Pair<Int, ImageData>>(Int.MAX_VALUE, Int.MAX_VALUE)
        var imagesToUpscaleCounter = 0
        val imagesToUpscaleMutex = Mutex()
        val upscaledImages = TreeSet<Pair<Int, ImageData>> { o1, o2 ->
            o1.first.compareTo(o2.first)
        }
        val imagesFormat = if ((ParamList.ParamKeys.usePngImages.getFrom(params)?.takeIf {
                it.isNotBlank()
            } ?: "false") == "false") "jpg" else "png"
        val isPng = imagesFormat != "jpg"

        var frameSplitterEnded = false
        SplitIntoFrames.initSplitterFor(paths, imagesFormat).onEach {
            if (it.first == -1) {
                frameSplitterEnded = true
                return@onEach
            }
            app.detailLog.sent("frame sliced -> $it")
            imgToDataProcessor.addProcess(
                it.second, { data, _ ->
                    data.loadedImage = null
                    data.thumbnail = null

                    val filterResult: Pair<ImageData, ImageData>?
                    synchronized(imageDatabase) {
                        filterResult = sameHashFilter.filter(data, imageDatabase)
                    }
                    if (filterResult == null) {
                        app.detailLog.sent("not same $it")
                        scope.launch {
                            imagesToUpscale.emit(Pair(it.first, data))
                            imagesToUpscaleMutex.withLock {
                                imagesToUpscaleCounter++
                            }
                            synchronized(imageDatabase) {
                                imageDatabase.add(data)
                            }
                        }
                    } else {
                        app.detailLog.sent("same $it")
                        synchronized(upscaledImages) {
                            upscaledImages.add(Pair(it.first, filterResult.second))
                        }
                        Files.delete(Paths.get(data.filePath!!))
                    }

                }, requireThumnail = false
            )
        }.launchIn(scope)

        val realcuganHelper = RealcuganHelper(paths, params, isPng)
        imagesToUpscale.onEach {
            realcuganHelper.add(it.second) { image ->
                synchronized(upscaledImages) {
                    app.detailLog.sent("frame - ${it.first} upscaled")
                    upscaledImages.add(Pair(it.first, it.second))
                }
            }
        }.launchIn(scope)

        try {
            app.infoLog.sent("waiting frame splitter end")
            while (!frameSplitterEnded) Thread.sleep(100)
            app.infoLog.sent("waiting image processor end")
            imgToDataProcessor.join(1000)
            app.infoLog.sent("waiting realcugan helper end")
            do {
                val busyState = realcuganHelper.isBusy()
                println("realcugan queue - $busyState")
                Thread.sleep(200)
            } while (busyState != -1)

            realcuganHelper.destroy()
            app.infoLog.sent("waiting video concat end")
            ConcatFramesToVideo.concat(upscaledImages, paths, params)
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
        }
    }

    private fun MutableSharedFlow<String>.print(text: String) {
        runBlocking {
            this@print.emit("MP4Log -> $text")
        }
    }
}