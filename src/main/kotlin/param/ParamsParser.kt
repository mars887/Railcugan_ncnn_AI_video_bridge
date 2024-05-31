package param

import param.ParamList.ParamKeys
import param.ParamList.paramsList
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.extension

object ParamsParser {
    fun parse(list: Array<String>): Params {
        val params = ArrayList<Pair<String, String?>>()
        val iterator = list.filter { !it.isBlank() }.iterator()
        iterator.forEach {
            if (it.startsWith("-")) {
                params.add(Pair(it.drop(1), null))
            } else {
                if (params.size == 0 || ((!iterator.hasNext() && !containsKey(params, ParamKeys.inputPath.key)) && !params.last().hasEmptyValue())) {
                    println("adding input without key - $it")
                    params.add(Pair(ParamKeys.inputPath.key, it))
                } else {
                    if (params.last().hasEmptyValue()) {
                        println("adding param to last - $it")
                        val key = params.last().first
                        params.dropLast(1)
                        params.add(Pair(key, it))
                    } else {
                        if (iterator.hasNext()) {
                            println("ignoring token - $it because key not found")
                        } else if (!containsKey(params, ParamKeys.outputPath.key)) {
                            println("adding output without key - $it")
                            params.add(Pair(ParamKeys.outputPath.key, it))
                        } else {
                            println("ignoring token - $it because key not found")
                        }
                    }
                }
            }
        }
        val it = Params()
        params.forEach { (key, value) ->
            val param = paramsList.find {
                it.keys.contains(key) || it.realKey.key == key
            }
            if (param != null) {
                if (value.isNullOrBlank()) {
                    it.addParam(param.realKey, param.defIfExitst)
                } else {
                    it.addParam(param.realKey, value)
                }
            } else {
                println(
                    "skipping key ($key) ${if (value != null) "and param ($value)" else " "}because key not found"
                )
            }
        }
        paramsList.forEach { pl ->
            if (!it.containsParam(pl.realKey)) {
                it.addParam(
                    pl.realKey,
                    pl.defValue
                )
            }
        }
        val fin = params.firstOrNull {
            it.first == ParamKeys.inputPath.key
        }
        val fout = params.lastOrNull {
            it.first == ParamKeys.outputPath.key
        }
        if (fin != null) {
            if (fin.second == null || (!Paths.get(fin.second!!).exists())) {
                params.remove(fin)
            } else if (fout == null) {
                val sec = Paths.get(fin.second!!)
                it.addParam(
                    ParamKeys.outputPath,
                    (sec.parent.toString() + "\\" + sec.fileName.toString()
                        .dropLast(sec.fileName.extension.length + 1)) + "_scaled." + sec.extension
                )
            }
        }
        if(ParamKeys.scale.getFrom(it) != "2") {
            val nval = ParamKeys.realcuganDenoiseLevel.getFrom(it)!!.toInt()
            if(!(nval == 0 || nval == 3)) {
                println("Denoise level 1 and 2 are only available for -scale = 2")
                if(nval < 2) {
                    it.addParam(ParamKeys.realcuganDenoiseLevel,"0")
                    println("Denoise level set to 0")
                }
                if(nval > 1) {
                    it.addParam(ParamKeys.realcuganDenoiseLevel,"3")
                    println("Denoise level set to 3")
                }
            }
        }
        return it
    }

    private fun containsKey(params: java.util.ArrayList<Pair<String, String?>>, key: String): Boolean {
        val find = paramsList.find {
            it.realKey.key == key || it.keys.contains(key)
        } ?: return false
        params.find {
            find.realKey.key == it.first || find.keys.contains(it.first)
        } ?: return false
        return true
    }

}

private fun <A, B> Pair<A, B>.hasEmptyValue(): Boolean {
    return second == null
}
