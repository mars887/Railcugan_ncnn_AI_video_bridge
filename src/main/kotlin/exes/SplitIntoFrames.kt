package exes

import data.PathsDatabase
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.extension

object SplitIntoFrames {
    val threads = ConcurrentHashMap<String, Thread>()

    private val pathComparator = object : Comparator<Path> {
        override fun compare(o1: Path?, o2: Path?): Int {
            if (o1 == null) return 1
            if (o2 == null) return -1
            return (o1.fileName.toString().dropLast(o1.extension.length + 1).toInt()).compareTo(
                o2.fileName.toString().dropLast(o2.extension.length + 1).toInt()
            )
        }
    }

    fun initSplitterFor(paths: PathsDatabase,imagesFormat: String): MutableSharedFlow<Pair<Int, Path>> {
        val flow = MutableSharedFlow<Pair<Int, Path>>(10000, 10000, BufferOverflow.SUSPEND)
        val tempFolderPath =
            paths.tempFilesPath + Paths.get(paths.inputPath!!).fileName.toString()
                .dropLastWhile { it != '.' }.dropLast(1) + "\\"
        try {
            Files.createDirectories(Paths.get(tempFolderPath))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        println("frame splitter started")
        Thread {
            val process = ProcessBuilder(
                paths.ffmpegPath,
                "-i",
                paths.inputPath,
                "$tempFolderPath\\%d.$imagesFormat"
            ).directory(File(paths.tempFilesPath!!)).start()
            Thread {
                process.errorReader().forEachLine {
                }
            }.start()

            try {
                val pathList = mutableSetOf<Int>()

                while (process.isAlive) {
                    Thread.sleep(250)
                    Files.list(Paths.get(tempFolderPath)).sorted(pathComparator).forEach {
                        val index = it.fileName.toString().dropLast(imagesFormat.length + 1).toInt()
                        if (pathList.add(index)) {
                            runBlocking {
                                flow.emit(Pair(index, it))
                            }
                        }
                    }
                }
                Thread.sleep(2000)
                Files.list(Paths.get(tempFolderPath)).sorted(pathComparator).forEach {
                    val index = it.fileName.toString().dropLast(imagesFormat.length + 1).toInt()
                    if (pathList.add(index)) {
                        runBlocking {
                            flow.emit(Pair(index, it))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            runBlocking {
                flow.emit(Pair(-1, Paths.get("")))
            }
            println("mp4 process finished")
        }.also { threads[paths.inputPath!!] = it }.start()
        return flow
    }
}