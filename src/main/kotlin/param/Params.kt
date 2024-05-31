package param

import java.util.concurrent.ConcurrentHashMap

class Params {
    private val params = ConcurrentHashMap<ParamList.ParamKeys, String>()

    fun addParam(key: ParamList.ParamKeys, value: String) {
        params[key] = value
    }

    fun getParam(key: ParamList.ParamKeys): String? = params[key]
    fun containsParam(key: ParamList.ParamKeys): Boolean = params.containsKey(key)
    fun deleteParam(key: ParamList.ParamKeys): Boolean = params.remove(key) != null

    fun forEachParam(listener: (ParamList.ParamKeys, String) -> Unit) {
        params.toSortedMap(Comparator { o1, o2 ->
            o1.compareTo(o2)
        }).forEach {
            listener(it.key, it.value)
        }
    }

}
fun ParamList.ParamKeys.getFrom(params: Params): String? {
    return params.getParam(this).takeIf { it!!.isNotBlank() }
}

