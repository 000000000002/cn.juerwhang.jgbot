package cn.juerwhang.jgbot

import cc.moecraft.icq.PicqBotX
import cc.moecraft.icq.PicqConfig
import cc.moecraft.logger.format.AnsiColor.*
import cn.juerwhang.jgbot.modules.CURRENT_VERSION
import cn.juerwhang.jgbot.modules.CURRENT_VERSION_SUMMARY
import cn.juerwhang.jgbot.modules.basic.BasicModule
import cn.juerwhang.jgbot.modules.economy.BankModule
import cn.juerwhang.jgbot.modules.economy.SignupModule
import cn.juerwhang.jgbot.modules.other.HitokotoModule
import cn.juerwhang.jgbot.utils.toJson
import cn.juerwhang.juerobot.Juerobot
import cn.juerwhang.juerobot.core.RemoteConfigManagerModule
import cn.juerwhang.juerobot.preset.ErrorHandleModule
import cn.juerwhang.juerobot.utils.Arguments
import cn.juerwhang.juerobot.utils.connectDatabase
import cn.juerwhang.juerobot.utils.getArguments


lateinit var bot: PicqBotX
lateinit var arguments: Arguments
fun main(vararg args: String) {
    val startDate = System.currentTimeMillis()

    arguments = getArguments()

    val config = PicqConfig(arguments.socketPort)

    config.isDebug = true
    bot = PicqBotX(config)
    bot.logger.log("${YELLOW}加载配置: $RESET${arguments.toJson()}")

    bot.enableCommandManager(*arguments.prefix)
    bot.logger.log("${YELLOW}载入的目标地址\t$CYAN[ ${arguments.location}:${arguments.targetPort} ]")
    bot.addAccount("jg-bot", arguments.location, arguments.targetPort)

    bot.logger.log("${YELLOW}当前JGBot版本\t$CYAN[ $CURRENT_VERSION ]")
    bot.logger.log("${YELLOW}版本相关信息: ")
    CURRENT_VERSION_SUMMARY.split("\n").forEach { bot.logger.log("$CYAN>>$RESET $it") }

    Juerobot.init(bot) {
        connectDatabase {
            showSql = true
            url = "./jg.bot.sqlite"
        }

        install(RemoteConfigManagerModule)
        install(ErrorHandleModule)

        install(BasicModule)
        install(BankModule)
        install(HitokotoModule)
        install(SignupModule)
    }
    Juerobot.start()
    val endDate = System.currentTimeMillis()
    bot.logger.log("${GREEN}启动完毕，共耗时$RESET ${(endDate - startDate)/1000.0}$GREEN 秒！")
}
