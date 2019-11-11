package cn.juerwhang.jgbot.modules.core.entities

import cn.juerwhang.jgbot.modules.basic.entities.BaseEntity
import cn.juerwhang.jgbot.modules.basic.entities.BaseTable
import me.liuwj.ktorm.schema.varchar

object RemoteConfigs: BaseTable<RemoteConfig>("table_remote_config") {
    val module by varchar("module").bindTo { it.module }
    val name by varchar("name").bindTo { it.name }
    val value by varchar("value").bindTo { it.value }
}

interface RemoteConfig: BaseEntity<RemoteConfig> {
    var module: String
    var name: String
    var value: String
}
