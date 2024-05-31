import kotlin.math.abs

class SameHashFilter(private val diffLimit: Int) {
    fun filter(data: ImageData, database: Collection<ImageData>): Pair<ImageData, ImageData>? {
        var minDiff = Integer.MAX_VALUE
        var minDiffId = 0L

        database.forEach {
            var diff = 0
            data.hash.forEachIndexed { index, i ->
                diff += abs(it.hash[index] - i)
            }

            if (diff < minDiff) {
                minDiff = diff
                minDiffId = it.id
            }
        }
        return if (minDiff > diffLimit) null else {
            val imageTwo = database.find { it.id == minDiffId }!!
            Pair(data, imageTwo)
        }
    }

    fun fastFilter(data: ImageData, database: MutableCollection<ImageData>): Boolean {
        database.forEach {
            var diff = 0
            data.hash.forEachIndexed { index, i ->
                diff += abs(it.hash[index] - i)
            }
            if (diff <= diffLimit) return false
        }
        return true
    }

    companion object {
        fun calculateDiffBetween(source: ImageData, data: ImageData): Int {
            var diff = 0
            data.hash.forEachIndexed { index, i ->
                diff += abs(source.hash[index] - i)
            }
            return diff
        }
    }
}