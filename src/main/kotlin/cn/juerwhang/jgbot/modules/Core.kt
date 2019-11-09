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
import cn.juerwhang.jgbot.arguments
import cn.juerwhang.jgbot.bot
import cn.juerwhang.jgbot.modules.basic.entities.BaseTable
import java.util.*


/**
 * 向机器人中注册各个模块
 */
fun PicqBotX.registerModules() {
    registerModules.forEach {
        it.register(this)
    }
}

/**
 * CQ模块，对原有监听器（IcqListener）以及命令（IcqCommand）的封装。
 * @param enabled 是否启用本模块，若为false，则该模块不会被加载
 * @param name 该模块的模块名，仅用于调试
 * @param summary 该模块的简介，仅用于调试
 * @param registerSelf 是否将该模块自身注册为监听器，若需要监听事件，则将该值设为true
 */
open class CqModule(
    private val enabled: Boolean,
    private val name: String = "",
    private val summary: String = "",
    private val registerSelf: Boolean = false
): IcqListener() {
    private val commandList = LinkedList<FunctionalCommand>()
    val logger = bot.loggerInstanceManager.getLoggerInstance("模块: %s".format(name), true)!!
    open val usingTable: List<BaseTable<*>> = emptyList()

    fun register(bot: PicqBotX) {
        logger.log(">> ======== 正在注册模块 ======== <<")
        logger.log(">> 模块信息：%s".format(summary))
        if (enabled) {
            if (registerSelf) {
                logger.log(">> 将模块注册为事件监听器！")
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
            for (table in usingTable) {
                logger.log(">> 引用表：%s".format(table.tableName))
                table.createTable(this.logger)
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
        return addCommand(FunctionalPrivateCommand(this, name, *alias, block = block))
    }

    fun addGroupCommand(name: String, vararg alias: String, block: GroupCommandCallback): CqModule {
        return addCommand(FunctionalGroupCommand(this, name, *alias, block = block))
    }

    fun addDiscussCommand(name: String, vararg alias: String, block: DiscussCommandCallback): CqModule {
        return addCommand(FunctionalDiscussCommand(this, name, *alias, block = block))
    }

    fun addEverywhereCommand(name: String, vararg alias: String, block: EverywhereCommandCallback): CqModule {
        return addCommand(FunctionalEverywhereCommand(this, name, *alias, block = block))
    }
}

open class FunctionalCommand(
    val parentModule: CqModule,
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
    parentModule: CqModule,
    name: String,
    vararg alias: String,
    val block: GroupCommandCallback
): FunctionalCommand(parentModule, "GroupCommand", name, *alias), GroupCommand {
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
    parentModule: CqModule,
    name: String,
    vararg alias: String,
    val block: PrivateCommandCallback
): FunctionalCommand(parentModule, "PrivateCommand", name, *alias), PrivateCommand {
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
    parentModule: CqModule,
    name: String,
    vararg alias: String,
    val block: DiscussCommandCallback
): FunctionalCommand(parentModule, "DiscussCommand", name, *alias), DiscussCommand {
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
    parentModule: CqModule,
    name: String,
    vararg alias: String,
    val block: EverywhereCommandCallback
): FunctionalCommand(parentModule, "EverywhereCommand", name, *alias), EverywhereCommand {
    override fun run(
        event: EventMessage?,
        sender: User?,
        command: String?,
        args: ArrayList<String>?
    ): String {
        parentModule.logger.log("处理来自于 %d 的命令: %s (%s)".format(sender?.id, command, (args?: ArrayList()).toArray().joinToString()))
        return block(event!!, sender!!, command!!, args?: ArrayList())
    }
}

object ErrorHandlerModule: CqModule(true, "错误处理模块", "该模块用于输出异常至日志。", true) {
    @EventHandler
    fun errorHandlerFunction(event: EventLocalException) {
        logger.error("发生了异常：%s".format(event.exception.message))
        event.exception.printStackTrace()
        if (event is EventMessage && arguments.debugMode) {
            event.respond("执行过程中发生了点儿问题，请查看日志以获取更多信息！")
        }
    }
}
