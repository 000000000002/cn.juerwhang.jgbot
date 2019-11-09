package cn.juerwhang.jgbot

import cc.moecraft.icq.PicqBotX
import cc.moecraft.icq.PicqConfig
import cn.juerwhang.jgbot.modules.CURRENT_VERSION
import cn.juerwhang.jgbot.modules.CURRENT_VERSION_SUMMARY
import cn.juerwhang.jgbot.modules.registerModules


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

lateinit var bot: PicqBotX
lateinit var arguments: Arguments
fun main(vararg args: String) {
    arguments = analyzeArgs(args)

    val config = PicqConfig(arguments.socketPort)
    config.isDebug = true
    bot = PicqBotX(config)
    bot.logger.log("载入的目标地址: [ %s:%d ]".format(arguments.location, arguments.targetPort))
    bot.enableCommandManager(*arguments.prefix)
    bot.addAccount("jg-bot", arguments.location, arguments.targetPort)
    bot.logger.log("当前 JGBot 版本: [ %s ]".format(CURRENT_VERSION))
    bot.logger.log("版本相关信息: ")
    CURRENT_VERSION_SUMMARY.split("\n").forEach { bot.logger.log(">> %s".format(it)) }
    bot.logger.log("即将开始注册模块……")
    bot.registerModules()
    bot.startBot()
}
