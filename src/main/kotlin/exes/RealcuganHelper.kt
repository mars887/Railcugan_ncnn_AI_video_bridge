package exes

import ImageData
import app
import data.PathsDatabase
import param.ParamList
import param.Params
import param.getFrom
import sent
import java.nio.file.Paths
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

class RealcuganHelper(private val paths: PathsDatabase, params: Params, private val outIsPng: Boolean) {
    private var gpus: Array<Int>

    private var denoiseLevel = max(min(ParamList.ParamKeys.realcuganDenoiseLevel.getFrom(params)
        ?.takeIf { it.isNotBlank() }
        ?.toIntOrNull() ?: -1, 3), -1)

    private val scale = max(min(ParamList.ParamKeys.scale.getFrom(params)!!.toInt(), 4), 2)

    private val executors: Array<QuerryThread>

    private val queue: ConcurrentLinkedQueue<Querry> = ConcurrentLinkedQueue()

    data class Querry(
        val input: String,
        val output: String,
        val function: (String) -> Unit
    )

    init {
        val gpus = arrayListOf<Int>()
        val all = getAllGpuIds()
        val res = ParamList.ParamKeys.allowedGpus.getFrom(params)?.takeIf {
            it.isNotBlank()
        } ?: ""
        if (res.contains("all")) gpus.addAll(all) else
            res.split(' ', ',', '.', ':', ';')
                .filter { it.toIntOrNull() != null }
                .map { it.toInt() }
                .forEach {
                    if (all.contains(it)) {
                        gpus.add(it)
                    }
                }
        this.gpus = gpus.toTypedArray()

        executors = Array((ParamList.ParamKeys.realcuganThreads.getFrom(params) ?: "3").toInt() * max(gpus.size, 1))
        {
            val gpu = if (gpus.size > 0) gpus[it % gpus.size] else -1
            getAndStartExecutorForGpu(gpu)
        }
    }

    fun isBusy(): Int {
        return if (queue.size != 0 || executors.any { it.isBusy.get() }) queue.size else -1
    }

    private fun getAndStartExecutorForGpu(gpu: Int): QuerryThread {
        return QuerryThread(gpu, this).also { it.start() }
    }

    private fun getAllGpuIds(): List<Int> {
        return ProcessBuilder(
            paths.realcuganPath,
            "-i", "null",
            "-o", "null.png"
        ).start().inputReader().lines().toList().map {
            it.takeWhile { it != ' ' }.drop(1).toInt()
        }.distinct()
    }

    fun add(source: ImageData, function: (String) -> Unit) {
        queue.add(Querry(source.filePath!!, source.filePath!!, function))
    }


    fun add(input: String, output: String, function: (String) -> Unit) {
        queue.add(Querry(input, output, function))
    }

    private fun getCmd(input: String, output: String, scale: Int, outIsPng: Boolean, gpu: Int): ArrayList<String> {
        val cmd = arrayListOf(
            paths.realcuganPath!!,
            "-i", input,
            "-scale", scale.toString(),
            "-n", denoiseLevel.toString(),
        )
        if (gpu != -1) {
            cmd.add("-g")
            cmd.add(gpu.toString())
        }
        if (outIsPng) {
            cmd.add("-f")
            cmd.add("png")
        }

        cmd.add("-o")
        cmd.add(output)

        return cmd
    }

    fun destroy() {
        executors.forEach(QuerryThread::interrupt)
    }

    private class QuerryThread(val gpu: Int, private val helper: RealcuganHelper) : Thread() {
        val isBusy = AtomicBoolean()

        override fun run() {
            super.run()
            try {
                while (!isInterrupted) {

                    val source = helper.queue.poll()
                    if (source == null) {
                        sleep(10)
                        continue
                    }
                    isBusy.set(true)

                    val cmd = helper.getCmd(source.input, source.output, helper.scale, helper.outIsPng, gpu)
                    app.detailLog.sent(cmd.toString())

                    ProcessBuilder(
                        cmd
                    ).directory(Paths.get(helper.paths.realcuganPath!!).parent.toFile())
                        .start()
                        .waitFor()

                    source.function(source.input)
                    isBusy.set(false)
                }
            } catch (_: InterruptedException) {
            }

        }
    }
}