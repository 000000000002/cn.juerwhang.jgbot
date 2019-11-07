package cn.juerwhang.jgbot

import cc.moecraft.icq.PicqBotX
import cc.moecraft.icq.PicqConfig
import cn.juerwhang.jgbot.modules.CURRENT_VERSION
import cn.juerwhang.jgbot.modules.registerModules


lateinit var bot: PicqBotX
fun main(vararg args: String) {
    val config = PicqConfig(56101)
    config.isDebug = true
    bot = PicqBotX(config)
    bot.logger.log("载入的目标地址: [ %s:%d ]".format(args[0], 56100))
    bot.enableCommandManager(">", "》")
    bot.addAccount("jg-bot", args[0], 56100)
    bot.logger.log("当前 JGBot 版本: [ %s ]".format(CURRENT_VERSION))
    bot.logger.log("即将开始注册模块……")
    bot.registerModules()
    bot.startBot()
}
