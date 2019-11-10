package cn.juerwhang.jgbot.utils

import cn.juerwhang.jgbot.arguments
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.expression.*
import me.liuwj.ktorm.logging.ConsoleLogger
import me.liuwj.ktorm.logging.LogLevel
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
        logger = if (arguments.debugMode) ConsoleLogger(LogLevel.DEBUG) else null
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
            if (arg is LocalDateTime) {
                ArgumentExpression(arg as LocalDateTime, LocalDateTimeSqlType)
            } else if (arg is Column<*>) {
                val column = arg as Column<*>
                ColumnExpression(column.table.tableName, column.name, column.sqlType)
            } else {
                ArgumentExpression(arg.toString(), VarcharSqlType)
            }
        } else {
            arg
        })
    }
    @Suppress("UNCHECKED_CAST")
    return DbCallFuncExpression(name, realArgs, callFuncSqlType.getOrDefault(name, IntSqlType) as SqlType<T>)
}

val callFuncSqlType = HashMap<String, SqlType<*>>()

