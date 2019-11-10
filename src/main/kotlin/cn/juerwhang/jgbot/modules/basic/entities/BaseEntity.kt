package cn.juerwhang.jgbot.modules.basic.entities

import cc.moecraft.logger.HyLogger
import cn.juerwhang.jgbot.utils.deleteLast
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.datetime
import me.liuwj.ktorm.schema.long
import java.time.LocalDateTime

open class BaseTable<T: BaseEntity<T>>(table: String): Table<T>(table) {
    val id by long("id").primaryKey().bindTo { it.id }
    val createDate by datetime("create_date").bindTo { it.createDate }

    fun createTable(logger: HyLogger) {
        val createTableSql = StringBuilder("create table if not exists ")
            .append(this.tableName)
            .append(" (")
        for (column in this.columns) {
            createTableSql
                .append(column.name)
                .append(" ")
                .append(mapColumnType(column.sqlType.typeName))
            if (column == this.primaryKey) {
                createTableSql.append(" primary key autoincrement")
            }
            if (column.name == "create_date" && column.sqlType.typeName == "datetime") {
                createTableSql.append(" default (datetime('now', 'localtime'))")
            }
            createTableSql.append(",")
        }
        if (createTableSql.endsWith(',')) {
            createTableSql.deleteCharAt(createTableSql.length - 1)
        }
        createTableSql.append(")")

        logger.log(">> 尝试执行SQL语句 --> %s".format(createTableSql.toString()))
        Database.global.useConnection {
            it.createStatement().execute(createTableSql.toString())
        }
    }
}

interface BaseEntity<T: BaseEntity<T>>: Entity<T> {
    var id: Long
    var createDate: LocalDateTime
}

fun mapColumnType(source: String): String {
    return when(source) {
        "bigint" -> "integer"
        "varchar" -> "text"
        else -> source
    }
}
