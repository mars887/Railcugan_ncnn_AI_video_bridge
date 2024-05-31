package processes

import app
import data.PathsDatabase
import exes.RealcuganHelper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import param.ParamList
import param.Params
import param.getFrom
import sent

class PngProcess(val paths: PathsDatabase) : DefaultProcess() {
    override suspend fun apply(params: Params) {
        val asPng = ParamList.ParamKeys.usePngImages.getFrom(params) == "true"
        val realcugan = RealcuganHelper(paths, params, asPng)
        val mutex = Mutex(true)
        realcugan.add(paths.inputPath!!, paths.outputPath!!) { path ->
            mutex.unlock()
        }
        mutex.withLock {
            app.detailLog.sent("image with path ${paths.inputPath!!} upscaled")
            realcugan.destroy()
        }
    }

}
