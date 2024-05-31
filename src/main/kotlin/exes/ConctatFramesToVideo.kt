package exes

import ImageData
import app
import data.PathsDatabase
import param.ParamList
import param.Params
import param.getFrom
import sent
import java.lang.Integer.max
import java.lang.Integer.min
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.random.Random

object ConcatFramesToVideo {
    suspend fun concat(images: TreeSet<Pair<Int, ImageData>>, paths: PathsDatabase, params: Params) {

        val allMetadata = getInputMetadata(paths, params)
        val metadata = allMetadata.first {
            it["codec_type"] == "video"
        }
        val hasAudio = allMetadata.any {
            it["codec_type"] == "audio"
        }
        val frameRate = if (metadata["r_frame_rate"] == metadata["avg_frame_rate"]) {
            metadata["r_frame_rate"]
        } else null
        println("getInputMetadata")
        val dataWDs = getFrameDurations(images, paths, metadata["index"]!!.toInt())
        println("getFrameDurations")

        val pathsFile =
            Paths.get(Paths.get(images.first().second.filePath!!).parent.toString() + "\\${Random.nextLong()}.txt")
        val pathsFileWriter = Files.newBufferedWriter(pathsFile)
        dataWDs.forEach {
            pathsFileWriter.write("file \'${it.second.filePath!!}\'\nduration ${it.third}\n")
        }
        pathsFileWriter.flush()
        pathsFileWriter.close()

        val videoCodec = ParamList.ParamKeys.outputVideoCodec.getFrom(params).takeIf { !it.isNullOrBlank() }
            ?: metadata["codec_name"]!!
        val fpsSetting = if (frameRate != null) "-r" else "-fps_mode"
        val crfSetting = max(min(ParamList.ParamKeys.outputVideoCrf.getFrom(params)!!.toInt(), 63), 0).toString()

        val cmd = if (hasAudio) arrayListOf<String?>(
            paths.ffmpegPath, "-y",
            "-i", paths.inputPath,
            "-f", "concat",
            "-safe", "0",
            "-i", pathsFile.toString(),
            "-c:v", videoCodec,
            "-crf", crfSetting,
            fpsSetting, frameRate ?: "passthrough",
            "-vf", "\"format=${metadata["pix_fmt"]}\"",
            "-map", "0:a", "-map", "1:v", "-c:a", "copy",
            paths.outputPath
        )
        else arrayListOf<String?>(
            paths.ffmpegPath, "-y",
            "-f", "concat",
            "-safe", "0",
            "-i", pathsFile.toString(),
            "-c:v", videoCodec,
            "-crf", crfSetting,
            fpsSetting, frameRate ?: "passthrough",
            "-vf", "\"format=${metadata["pix_fmt"]}\"",
            paths.outputPath
        )


        println(cmd.joinToString(separator = " "))

        val process = ProcessBuilder(cmd).start()

        val t1 = Thread {
            process.inputReader().forEachLine {
                app.detailLog.sent(it)
            }
        }.also {
            it.start()
        }
        val t2 = Thread {
            process.errorReader().forEachLine {
                app.detailLog.sent(it)
            }
        }.also {
            it.start()
        }
        process.waitFor()
        t1.interrupt()
        t2.interrupt()
    }

    private fun getFrameDurations(
        _images: TreeSet<Pair<Int, ImageData>>,
        paths: PathsDatabase,
        streamIndex: Int
    ): TreeSet<Pair3<Int, ImageData, Double>> {
        val treeSet = TreeSet<Pair3<Int, ImageData, Double>> { o1, o2 ->
            o1.first.compareTo(o2.first)
        }
        val images = _images.toList()
        val process = ProcessBuilder(
            paths.ffprobePath,
            "-i", paths.inputPath!!,
            "-loglevel", "quiet",
            "-show_frames",
            "-show_entries", "frame=pts_time,duration_time,stream_index"
        ).start()
        var pts = 0.0
        process.inputReader().lines().toList()
            .joinToString(separator = " ")
            .split("[/FRAME]")
            .map { it.drop(8) }
            .map {
                it.split(" ").filter { !it.isBlank() }
                    .filter {
                        (!it.contains("SIDE_DATA") && !it.contains("side_data_type")) && it.split("=").size == 2
                    }.map {
                        val split = it.split("=")
                        Pair(split[0], split[1])
                    }
            }.map {
                val map = HashMap<String, String>()
                it.forEach {
                    map[it.first] = it.second
                }
                map
            }.filter { it.size == 3 }
            .filter {
                it["stream_index"]!!.toInt() == streamIndex
            }
            .forEachIndexed { index, map ->
                var time = map["pts_time"]!!.toDouble() - pts
                if (time == 0.0) time = map["duration_time"]!!.toDouble()
                treeSet.add(Pair3(images[index].first, images[index].second, time))
                pts = map["pts_time"]!!.toDouble()
            }
        return treeSet
    }

    private suspend fun getInputMetadata(paths: PathsDatabase, params: Params): List<HashMap<String, String>> {
        val process = ProcessBuilder(
            paths.ffprobePath,
            "-i", paths.inputPath!!,
            "-loglevel", "quiet",
            "-show_streams"
        ).start()
        val metadata = process.inputReader().lines().toList()
            .joinToString(separator = "!")
            .split("[/STREAM]")
            .map { if (it.startsWith("!")) it.drop(9) else it.drop(8) }
            .map {
                it.split("!")
                    .filter { it.isNotBlank() }
                    .map {
                        val split = it.split("=")
                        Pair(split[0], split[1])
                    }
            }.filter { it.isNotEmpty() }.map {
                val map = HashMap<String, String>()
                it.forEach {
                    map[it.first] = it.second
                }
                map
            }
        return metadata
    }

    data class Pair3<A, B, C>(
        val first: A,
        val second: B,
        val third: C
    )
}