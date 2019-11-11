package cn.juerwhang.jgbot

import cc.moecraft.icq.PicqBotX
import cc.moecraft.icq.PicqConfig
import cn.juerwhang.jgbot.modules.CURRENT_VERSION
import cn.juerwhang.jgbot.modules.CURRENT_VERSION_SUMMARY
import cn.juerwhang.jgbot.modules.core.registerModules
import cn.juerwhang.jgbot.utils.Arguments
import cn.juerwhang.jgbot.utils.analyzeArgs
import cn.juerwhang.jgbot.utils.initConnect


lateinit var bot: PicqBotX
lateinit var arguments: Arguments
fun main(vararg args: String) {
    val startDate = System.currentTimeMillis()

    arguments = analyzeArgs(args)

    val config = PicqConfig(arguments.socketPort)
    config.isDebug = true
    bot = PicqBotX(config)
    bot.enableCommandManager(*arguments.prefix)

    bot.logger.log("载入的目标地址: [ %s:%d ]".format(arguments.location, arguments.targetPort))
    bot.addAccount("jg-bot", arguments.location, arguments.targetPort)

    bot.logger.log("当前 JGBot 版本: [ %s ]".format(CURRENT_VERSION))
    bot.logger.log("版本相关信息: ")
    CURRENT_VERSION_SUMMARY.split("\n").forEach { bot.logger.log(">> %s".format(it)) }

    initConnect()
    bot.logger.log("即将开始注册模块……")
    bot.registerModules()

    bot.startBot()

    val endDate = System.currentTimeMillis()
    bot.logger.log("启动完毕，共耗时 %f 秒！".format((endDate - startDate)/1000.0))
}
