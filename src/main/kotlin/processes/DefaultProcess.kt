package processes

import param.Params

abstract class DefaultProcess {

    abstract suspend fun apply(params: Params)

}