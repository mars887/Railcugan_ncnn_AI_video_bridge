import data.PathsDatabase
import processes.DefaultProcess
import processes.MP4Process
import processes.PngProcess
import java.nio.file.Paths
import kotlin.io.path.extension

object ProcessRouter {
    fun route(paths: PathsDatabase): DefaultProcess {
        val inputPath = Paths.get(paths.inputPath!!)
        return when(inputPath.extension) {
            "mp4","mov" -> {
                MP4Process(paths)
            }
            "jpg","png","jpeg" -> {
                PngProcess(paths)
            }
            else -> throw IllegalArgumentException()
        }
    }
    private const val INPUT_FORMATS = PathsDatabase.AVAILABLE_INPUT_FORMATS
}