package cn.juerwhang.jgbot.utils

import cc.moecraft.logger.HyLogger
import cn.juerwhang.jgbot.arguments
import cn.juerwhang.jgbot.bot
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.expression.*
import me.liuwj.ktorm.logging.Logger
import me.liuwj.ktorm.schema.*
import me.liuwj.ktorm.support.sqlite.SQLiteDialect
import me.liuwj.ktorm.support.sqlite.SQLiteFormatter
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

const val DB_PATH = "./jg.bot.sqlite"
fun initConnect() {
    Database.connect(
        url = "jdbc:sqlite:%s".format(DB_PATH),
        driver = "org.sqlite.JDBC",
        dialect = CallableSQLiteDialect(),
        logger = if (arguments.showSQL)
            ConsoleLoggerDelegate(bot.loggerInstanceManager.getLoggerInstance("Ktorm", arguments.debugMode))
        else
            null
    )
}

data class DbCallFuncExpression<T : Any>(
    val name: String,
    val args: List<SqlExpression>,
    override val sqlType: SqlType<T>,
    override val isLeafNode: Boolean = true,
    override val extraProperties: Map<String, Any> = emptyMap()
): ScalarExpression<T>()

class DbCallFuncSqlFormatter(
    database: Database,
    beautifySql: Boolean,
    indentSize: Int
): SQLiteFormatter(database, beautifySql, indentSize) {
    override fun visitUnknown(expr: SqlExpression): SqlExpression {
        return if (expr is DbCallFuncExpression<*>) {
            write(expr.name)
            write("(")
            if (expr.args.isNotEmpty()) {
                visit(expr.args[0])
                if (expr.args.size > 1) {
                    write(",")
                    for (arg in expr.args.subList(1, expr.args.size)) {
                        visit(arg)
                        removeLastBlank()
                    }
                }
            }
            write(")")
            expr
        } else {
            super.visitUnknown(expr)
        }
    }
}

class CallableSQLiteDialect: SQLiteDialect() {
    override fun createSqlFormatter(database: Database, beautifySql: Boolean, indentSize: Int): SqlFormatter {
        return DbCallFuncSqlFormatter(database, beautifySql, indentSize)
    }
}

fun <T: Any> call(name: String, vararg args: Any): DbCallFuncExpression<T> {
    val realArgs = LinkedList<SqlExpression>()
    for (arg in args) {
        realArgs.add(if (arg !is SqlExpression) {
            when (arg) {
                is LocalDateTime -> ArgumentExpression(arg, LocalDateTimeSqlType)
                is Column<*> -> ColumnExpression(arg.table.tableName, arg.name, arg.sqlType)
                else -> ArgumentExpression(arg.toString(), VarcharSqlType)
            }
        } else {
            arg
        })
    }
    @Suppress("UNCHECKED_CAST")
    return DbCallFuncExpression(name, realArgs, callFuncSqlType.getOrDefault(name, IntSqlType) as SqlType<T>)
}

val callFuncSqlType = HashMap<String, SqlType<*>>()

class ConsoleLoggerDelegate(private val logger: HyLogger): Logger {
    private fun printError(e: Throwable, printFunc: (String?) -> Unit) {
        printFunc(e.message)
        e.stackTrace.forEach { printFunc(it.toString()) }
        if (e.cause != null && e.cause != e) {
            printFunc("\n")
            printError(e.cause!!, printFunc)
        }
    }

    override fun debug(msg: String, e: Throwable?) {
        logger.debug(msg)
        e?.let { printError(e, logger::debug) }
    }

    override fun error(msg: String, e: Throwable?) {
        logger.error(msg)
        e?.let { printError(e, logger::error) }
    }

    override fun info(msg: String, e: Throwable?) {
        logger.log(msg)
        e?.let { printError(e, logger::log) }
    }

    override fun trace(msg: String, e: Throwable?) {
        logger.error(msg)
        e?.let { printError(e, logger::error) }
    }

    override fun warn(msg: String, e: Throwable?) {
        logger.warning(msg)
        e?.let { printError(e, logger::warning) }
    }

    override fun isDebugEnabled(): Boolean = true

    override fun isErrorEnabled(): Boolean = true

    override fun isInfoEnabled(): Boolean = true

    override fun isTraceEnabled(): Boolean = true

    override fun isWarnEnabled(): Boolean = true
}
