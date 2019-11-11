package cn.juerwhang.jgbot.modules.economy.entities

import cn.juerwhang.jgbot.modules.basic.entities.BaseEntity
import cn.juerwhang.jgbot.modules.basic.entities.BaseTable
import me.liuwj.ktorm.schema.long

object SignupLogs: BaseTable<SignupLog>("table_signup_logs") {
    val account by long("account").references(Accounts) { it.account }
    val amount by long("amount").bindTo { it.amount }
}

interface SignupLog: BaseEntity<SignupLog> {
    var account: Account
    var amount: Long
}
