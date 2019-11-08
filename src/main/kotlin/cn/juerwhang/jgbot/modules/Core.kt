package cn.juerwhang.jgbot.modules

import cc.moecraft.icq.PicqBotX
import cc.moecraft.icq.command.CommandProperties
import cc.moecraft.icq.command.interfaces.*
import cc.moecraft.icq.event.EventHandler
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.local.EventLocalException
import cc.moecraft.icq.event.events.message.EventDiscussMessage
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.event.events.message.EventPrivateMessage
import cc.moecraft.icq.user.Group
import cc.moecraft.icq.user.GroupUser
import cc.moecraft.icq.user.User
import cn.juerwhang.jgbot.bot
import java.util.*


/**
 * 向机器人中注册各个模块
 */
fun PicqBotX.registerModules() {
    registerModules.forEach {
        it.register(this)
    }
}

open class CqModule(
    private val enabled: Boolean,
    private val name: String = "",
    private val summary: String = "",
    private val registerSelf: Boolean = false
): IcqListener() {
    private val commandList = LinkedList<FunctionalCommand>()
    val logger = bot.loggerInstanceManager.getLoggerInstance("模块: %s".format(name), true)

    fun register(bot: PicqBotX) {
        logger.log(">> 正在注册模块 ( Enabled: %s )".format(enabled.toString()))
        logger.log(">> 模块信息：%s".format(summary))
        if (enabled) {
            if (registerSelf) {
                bot.eventManager.registerListener(this)
            }
            for (command in commandList.toTypedArray()) {
                logger.log(
                    ">> 注册命令 [ %s ( %s ) ] -> %s".format(
                        command.properties().name,
                        command.properties().alias.joinToString(),
                        command.type
                    )
                )
                bot.commandManager.registerCommand(command)
            }
        } else {
            logger.log(">> 模块未启用，抛弃。")
        }
        logger.log(">> ======== 模块注册完毕 ======== <<")
    }

    private fun addCommand(command: FunctionalCommand): CqModule {
        commandList.add(command)
        return this
    }

    fun addPrivateCommand(name: String, vararg alias: String, block: PrivateCommandCallback): CqModule {
        return addCommand(FunctionalPrivateCommand(name, *alias, block = block))
    }

    fun addGroupCommand(name: String, vararg alias: String, block: GroupCommandCallback): CqModule {
        return addCommand(FunctionalGroupCommand(name, *alias, block = block))
    }

    fun addDiscussCommand(name: String, vararg alias: String, block: DiscussCommandCallback): CqModule {
        return addCommand(FunctionalDiscussCommand(name, *alias, block = block))
    }

    fun addEverywhereCommand(name: String, vararg alias: String, block: EverywhereCommandCallback): CqModule {
        return addCommand(FunctionalEverywhereCommand(name, *alias, block = block))
    }
}

open class FunctionalCommand(
    val type: String,
    private val name: String,
    private vararg val alias: String
): IcqCommand {
    override fun properties(): CommandProperties {
        return CommandProperties(name, *alias)
    }
}

typealias GroupCommandCallback = (
    event: EventGroupMessage,
    sender: GroupUser,
    group: Group,
    message: String,
    args: ArrayList<String>
) -> String

class FunctionalGroupCommand(
    name: String,
    vararg alias: String,
    val block: GroupCommandCallback
): FunctionalCommand("GroupCommand", name, *alias), GroupCommand {
    override fun groupMessage(
        event: EventGroupMessage?,
        sender: GroupUser?,
        group: Group?,
        command: String?,
        args: ArrayList<String>?
    ): String {
        return block(event!!, sender!!, group!!, command!!, args?: ArrayList())
    }
}

typealias PrivateCommandCallback = (
    event: EventPrivateMessage,
    sender: User,
    command: String,
    args: ArrayList<String>
) -> String

class FunctionalPrivateCommand(
    name: String,
    vararg alias: String,
    val block: PrivateCommandCallback
): FunctionalCommand("PrivateCommand", name, *alias), PrivateCommand {
    override fun privateMessage(
        event: EventPrivateMessage?,
        sender: User?,
        command: String?,
        args: ArrayList<String>?
    ): String {
        return block(event!!, sender!!, command!!, args?: ArrayList())
    }
}

typealias DiscussCommandCallback = (
    event: EventDiscussMessage,
    sender: GroupUser,
    discuss: Group,
    command: String,
    args: ArrayList<String>
) -> String

class FunctionalDiscussCommand(
    name: String,
    vararg alias: String,
    val block: DiscussCommandCallback
): FunctionalCommand("DiscussCommand", name, *alias), DiscussCommand {
    override fun discussMessage(
        event: EventDiscussMessage?,
        sender: GroupUser?,
        discuss: Group?,
        command: String?,
        args: ArrayList<String>?
    ): String {
        return block(event!!, sender!!, discuss!!, command!!, args?: ArrayList())
    }
}

typealias EverywhereCommandCallback = (
    event: EventMessage,
    sender: User,
    command: String,
    args: ArrayList<String>
) -> String

class FunctionalEverywhereCommand(
    name: String,
    vararg alias: String,
    val block: EverywhereCommandCallback
): FunctionalCommand("EverywhereCommand", name, *alias), EverywhereCommand {
    override fun run(
        event: EventMessage?,
        sender: User?,
        command: String?,
        args: ArrayList<String>?
    ): String {
        return block(event!!, sender!!, command!!, args?: ArrayList())
    }
}

object ErrorHandlerModule: CqModule(true, "错误处理模块", "该模块用于输出异常至日志。", true) {
    @EventHandler
    fun errorHandlerFunction(event: EventLocalException) {
        event.exception.printStackTrace()
    }
}
