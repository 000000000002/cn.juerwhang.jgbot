package cn.juerwhang.jgbot.utils

fun StringBuilder.deleteLast(length: Int): StringBuilder {
    val last = this.length - 1
    this.delete(last - length, last)
    return this
}

fun splitToPair(arg: String): Pair<String, String?>? {
    var result: Pair<String, String?>? = null
    if (arg.isNotBlank()) {
        val array = arg.split("=")
        val first = array[0].trim()
        var second: String? = null
        if (array.size > 1) {
            second = array[1].trim()
        }
        result = Pair(first, second)
    }
    return result
}
