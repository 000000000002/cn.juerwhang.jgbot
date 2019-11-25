package cn.juerwhang.jgbot.modules.economy.entities

import cn.juerwhang.juerobot.store.BaseEntity
import cn.juerwhang.juerobot.store.BaseTable
import me.liuwj.ktorm.schema.long

object SignupLogs: BaseTable<SignupLog>("table_signup_logs") {
    val account by long("account").references(Accounts) { it.account }
    val amount by long("amount").bindTo { it.amount }
}

interface SignupLog: BaseEntity<SignupLog> {
    var account: Account
    var amount: Long
}
