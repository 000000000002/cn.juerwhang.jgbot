package cn.juerwhang.jgbot.modules.core

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
import cc.moecraft.logger.format.AnsiColor.*
import cn.juerwhang.jgbot.arguments
import cn.juerwhang.jgbot.bot
import cn.juerwhang.jgbot.modules.basic.entities.BaseTable
import java.util.*


/**
 * 向机器人中注册各个模块
 */
fun PicqBotX.registerModules() {
    cn.juerwhang.jgbot.modules.registerModules.forEach {
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
    val enabled: Boolean,
    val name: String = "",
    val summary: String = "",
    val registerSelf: Boolean = false
): IcqListener() {
    /**
     * 命令列表，该列表内的命令将会被注册。
     * @see FunctionalCommand
     */
    private val commandList = LinkedList<FunctionalCommand>()
    /**
     * 我还挺喜欢这个Logger的 :3
     */
    val logger = bot.loggerInstanceManager.getLoggerInstance("模块: %s".format(name), true)!!
    /**
     * 该模块依赖的表，这些表将会自动初始化（如果不存在的话）。
     */
    open val usingTable: List<BaseTable<*>> = emptyList()

    fun register(bot: PicqBotX) {
        logger.log("$CYAN>> ========$RESET 正在注册模块$CYAN ======== <<")
        logger.log("$CYAN>>$YELLOW 模块信息$RESET：$summary")
        if (enabled) {
            if (registerSelf) {
                logger.log("$CYAN>>$GREEN 将模块注册为事件监听器！")
                bot.eventManager.registerListener(this)
            }
            for (command in commandList.toTypedArray()) {
                logger.log(
                    "$CYAN>>$YELLOW 注册命令$RESET [ $CYAN ${command.type}$YELLOW ->$RESET ${command.properties().name} ($WHITE ${command.properties().alias.joinToString()}$RESET ) ]"
                )
                bot.commandManager.registerCommand(command)
            }
            for (table in usingTable) {
                logger.log("$CYAN>>$YELLOW 引用表$RESET：${table.tableName}")
                table.createTable(this.logger)
            }
        } else {
            logger.log("$CYAN>>$RED 模块未启用，抛弃。")
        }
        logger.log("$CYAN>> ========$RESET 模块注册完毕$CYAN ======== <<")
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
        return addCommand(
            FunctionalEverywhereCommand(
                this,
                name,
                *alias,
                block = block
            )
        )
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

typealias GroupCommandCallback = GroupCommandArgument.() -> String

data class GroupCommandArgument (
    val event: EventGroupMessage,
    val sender: GroupUser,
    val group: Group,
    val message: String,
    val args: ArrayList<String>
)

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
        return block(
            GroupCommandArgument(
                event!!,
                sender!!,
                group!!,
                command!!,
                args ?: ArrayList()
            )
        )
    }
}

typealias PrivateCommandCallback = PrivateCommandArgument.() -> String

data class PrivateCommandArgument (
    val event: EventPrivateMessage,
    val sender: User,
    val command: String,
    val args: ArrayList<String>
)

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
        return block(
            PrivateCommandArgument(
                event!!,
                sender!!,
                command!!,
                args ?: ArrayList()
            )
        )
    }
}

typealias DiscussCommandCallback = DiscussCommandArgument.() -> String

data class DiscussCommandArgument (
    val event: EventDiscussMessage,
    val sender: GroupUser,
    val discuss: Group,
    val command: String,
    val args: ArrayList<String>
)

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
        return block(
            DiscussCommandArgument(
                event!!,
                sender!!,
                discuss!!,
                command!!,
                args ?: ArrayList()
            )
        )
    }
}

typealias EverywhereCommandCallback = EverywhereCommandArgument.() -> String

data class EverywhereCommandArgument (
    val event: EventMessage,
    val sender: User,
    val command: String,
    val args: ArrayList<String>
)

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
        return block(
            EverywhereCommandArgument(
                event!!,
                sender!!,
                command!!,
                args ?: ArrayList()
            )
        )
    }
}

object ErrorHandlerModule: CqModule(true, "错误处理模块", "该模块用于输出异常至日志。", true) {
    @EventHandler
    fun errorHandlerFunction(event: EventLocalException) {
        logger.error("发生了异常：%s".format(event.exception.message))
        event.exception.printStackTrace()
        if (arguments.debugMode) {
            val respondEvent = when {
                event is EventMessage -> event
                event.parentEvent is EventMessage -> event.parentEvent
                else -> null
            } as EventMessage?
            respondEvent?.respond("执行过程中发生了点儿问题，请查看日志以获取更多信息！")
        }
    }
}
