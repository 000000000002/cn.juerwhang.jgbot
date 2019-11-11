package cn.juerwhang.jgbot.modules.economy.entities

import cn.juerwhang.jgbot.modules.basic.entities.BaseEntity
import cn.juerwhang.jgbot.modules.basic.entities.BaseTable
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.long
import me.liuwj.ktorm.schema.varchar

object Accounts: BaseTable<Account>("table_accounts") {
    val qq by long("qq").bindTo { it.qq }
    val level by int("level").bindTo { it.level }
}

interface Account: BaseEntity<Account> {
    var qq: Long
    var level: Int
}

object Currencies: BaseTable<Currency>("table_currencies") {
    val name by varchar("name").bindTo { it.name }
    val defaultAmount by long("default_amount").bindTo { it.defaultAmount }
}

interface Currency: BaseEntity<Currency> {
    var name: String
    var defaultAmount: Long?
}

object Banks: BaseTable<Bank>("table_banks") {
    val account by long("account").references(Accounts) { it.account }
    val currency by long("currency").references(Currencies) { it.currency }
    val amount by long("amount").bindTo { it.amount }
}

interface Bank: BaseEntity<Bank> {
    var account: Account
    var currency: Currency
    var amount: Long
}