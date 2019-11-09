package cn.juerwhang.jgbot.utils


data class Arguments(
    var location: String = "localhost",
    var targetPort: Int = 56100,
    var socketPort: Int = 56101,
    var prefix: Array<String> = arrayOf(">", "》"),
    var debugMode: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Arguments

        if (location != other.location) return false
        if (targetPort != other.targetPort) return false
        if (socketPort != other.socketPort) return false
        if (!prefix.contentEquals(other.prefix)) return false
        if (debugMode != other.debugMode) return false

        return true
    }

    override fun hashCode(): Int {
        var result = location.hashCode()
        result = 31 * result + targetPort
        result = 31 * result + socketPort
        result = 31 * result + prefix.contentHashCode()
        result = 31 * result + debugMode.hashCode()
        return result
    }
}

fun analyzeArgs(args: Array<out String>): Arguments {
    val result = Arguments()
    args.forEach {
        val pair = splitToPair(it)
        when(pair?.first) {
            "location" -> result.location = pair.second?:"localhost"
            "target.port" -> result.targetPort = (pair.second?:"56100").toInt()
            "source.port" -> result.socketPort = (pair.second?:"56101").toInt()
            "prefix" -> result.prefix = (pair.second?:">,》").replace("，", ",").split(",").toTypedArray()
            "debug" -> result.debugMode = true
        }
    }
    return result
}
