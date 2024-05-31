import ImageData

interface ImageDataFilter {
    fun filter(data: ImageData, database: MutableCollection<ImageData>) : FilterCallback
    fun fastFilter(data: ImageData, database: MutableCollection<ImageData>) : Boolean
}

abstract class FilterCallback

class SameImageFindedCallBack(val imgData: ImageData,val sameImgID: Long): FilterCallback() {

}
class NormalFilterCallback() : FilterCallback()
class InBlackListFilterCallback() : FilterCallback()