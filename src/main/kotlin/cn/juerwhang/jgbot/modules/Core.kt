package cn.juerwhang.jgbot.modules

import cc.moecraft.icq.PicqBotX
import cc.moecraft.icq.command.CommandProperties
import cc.moecraft.icq.command.interfaces.*
import cc.moecraft.icq.event.IcqListener
import cc.moecraft.icq.event.events.message.EventDiscussMessage
import cc.moecraft.icq.event.events.message.EventGroupMessage
import cc.moecraft.icq.event.events.message.EventMessage
import cc.moecraft.icq.event.events.message.EventPrivateMessage
import cc.moecraft.icq.user.Group
import cc.moecraft.icq.user.GroupUser
import cc.moecraft.icq.user.User
import cn.juerwhang.jgbot.bot
import cn.juerwhang.jgbot.modules.basic.BasicModule
import java.util.*


/**
 * 当前版本号
 */
const val CURRENT_VERSION = "alpha.1.0"

/**
 * 待注册模块，用于手动添加需要注册的模块。
 * Tips: 本来打算使用扫描器扫描包并自动加载，但是仔细想想，这样效率挺低的。也许之后会这么做，目前先一切从简吧。 -- JuerWhang 2019/11/07
 */
private val registerModules = arrayOf<CqModule>(
    BasicModule
)

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
    private val summary: String = ""
): IcqListener() {
    private val commandList = LinkedList<FunctionalCommand>()
    private val logger = bot.loggerInstanceManager.getLoggerInstance("CqModule", true)

    fun register(bot: PicqBotX, registerSelf: Boolean = false) {
        logger.log(">> 正在注册模块：%s (Enabled: %s)".format(name, enabled.toString()))
        logger.log(">> 模块信息：%s".format(summary))
        if (enabled) {
            if (registerSelf) {
                bot.eventManager.registerListener(this)
            }
            bot.commandManager.registerCommands(*commandList.toTypedArray())
        } else {
            logger.log(">> 模块 [%s] 未启用，抛弃。".format(name))
        }
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
): FunctionalCommand(name, *alias), GroupCommand {
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
): FunctionalCommand(name, *alias), PrivateCommand {
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
): FunctionalCommand(name, *alias), DiscussCommand {
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
): FunctionalCommand(name, *alias), EverywhereCommand {
    override fun run(
        event: EventMessage?,
        sender: User?,
        command: String?,
        args: ArrayList<String>?
    ): String {
        return block(event!!, sender!!, command!!, args?: ArrayList())
    }
}