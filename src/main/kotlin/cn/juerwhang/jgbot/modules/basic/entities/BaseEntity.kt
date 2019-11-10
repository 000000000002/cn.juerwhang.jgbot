package cn.juerwhang.jgbot.modules.basic.entities

import cc.moecraft.logger.HyLogger
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.long
import me.liuwj.ktorm.schema.varchar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

open class BaseTable<T: BaseEntity<T>>(table: String): Table<T>(table) {
    val id by long("id").primaryKey().bindTo { it.id }
    val createDate by varchar("create_date").bindTo { it.realCreateDate }

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
            if (column.name == "create_date") {
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

val SIMPLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

interface BaseEntity<T: BaseEntity<T>>: Entity<T> {
    var id: Long
    var realCreateDate: String

    var createDate: LocalDateTime
        get() { return LocalDateTime.parse(realCreateDate, SIMPLE_DATE_FORMATTER) }
        set(value) { realCreateDate = value.format(SIMPLE_DATE_FORMATTER) }
}

fun mapColumnType(source: String): String {
    return when(source) {
        "bigint" -> "integer"
        "varchar" -> "text"
        else -> source
    }
}
