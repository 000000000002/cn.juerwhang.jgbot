package cn.juerwhang.jgbot.utils

fun StringBuilder.deleteLast(length: Int): StringBuilder {
    val last = this.length - 1
    this.delete(last - length, last)
    return this
}