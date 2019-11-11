package cn.juerwhang.jgbot.modules.economy

import cc.moecraft.icq.user.User
import cn.juerwhang.jgbot.modules.CqModule
import cn.juerwhang.jgbot.modules.basic.entities.BaseTable
import cn.juerwhang.jgbot.modules.economy.entities.*
import cn.juerwhang.jgbot.modules.economy.entities.Currency
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.entity.*
import java.util.*


object BankModule: CqModule(true, "银行模块", "用于提供经济系统相关的操作。") {
    private const val ACCOUNT_ALL_INFO_TEMPLATE = "==== 您当前的账户存款详情 ====\n"
    private const val ACCOUNT_SOME_BANK_INFO_TEMPLATE = "==== 您当前查询的货币存款详情 ===="
    private const val CURRENCY_INFO_TEMPLATE = ">> %s: %d"
    private const val DEFAULT_LEVEL = 0

    override val usingTable: List<BaseTable<*>> = arrayListOf(
        Accounts,
        Currencies,
        Banks
    )

    private const val ownerUser = 2695996944L

    init {
        addEverywhereCommand("存款", "账号", "存款信息") {_, sender, _, args ->
            if (args.size == 0) {
                val resultBuilder = StringBuilder(ACCOUNT_ALL_INFO_TEMPLATE)
                for (bank in getBanksByQQ(sender.id)) {
                    resultBuilder.appendln(CURRENCY_INFO_TEMPLATE.format(bank.currency.name, bank.amount))
                }
                resultBuilder.toString()
            } else {
                val resultBuilder = StringBuilder(ACCOUNT_SOME_BANK_INFO_TEMPLATE)

                resultBuilder.toString()
            }
        }

        addPrivateCommand("添加货币", "add-currency", "add-crc") {_, sender, _, args ->
            if (sender.id == ownerUser) {
                if (args.size == 0) {
                    "输入参数错误！正确格式：\n添加货币 <货币名称> [默认货币量 = 0]"
                } else {
                    val currencyName = args[0]
                    val defaultValue = if (args.size > 1) args[1].toLong() else 0L
                    val newId = Currencies.insertAndGenerateKey {
                        it.name to currencyName
                        it.defaultAmount to defaultValue
                    }
                    "[ %s ] 添加成功！ID: %d".format(currencyName, newId)
                }
            } else {
                ""
            }
        }

        addPrivateCommand("修改货币", "mod-currency", "mod-crc") {_, sender, _, args ->
            if (sender.id == ownerUser) {
                if (args.size < 2) {
                    "输入参数错误！正确格式：\n修改货币 <货币名称> <新名称> [默认货币量]"
                } else {
                    val oldName = args[0]
                    val currency = getCurrencyByName(oldName)
                    if (currency == null) {
                        "不存在名为 [ %s ] 的货币！请尝试使用 添加货币 指令！"
                    } else {
                        val newName = args[1]
                        val defaultValue = if (args.size > 2) args[2].toLong() else currency.defaultAmount
                        currency.name = newName
                        currency.defaultAmount = defaultValue
                        currency.flushChanges()

                        "货币数据修改成功！"
                    }
                }
            } else {
                ""
            }
        }

        addEverywhereCommand("货币列表", "currency-list", "crc-list", "crc-ls") {_, _, _, _ ->
            val resultBuilder = StringBuilder("==== 当前已有货币列表 ====\n")
            for (currency in Currencies.asSequence()) {
                resultBuilder.appendln("[ %s ( 默认值 : %d ) ]".format(currency.name, currency.defaultAmount))
            }

            resultBuilder.toString()
        }
    }

    /**
     * 根据QQ获取到所有的存款数据。
     * @param qq 目标QQ号
     * @return 目标QQ号所属的存款列表
     */
    fun getBanksByQQ(qq: Long): List<Bank> {
        val account = getAccountByQQ(qq)
        return if (Banks.count { it.account eq account.id } != Currencies.count()) {
            val temp = LinkedList<Bank>()
            for (currency in Currencies.asSequence()) {
                temp.add(getBankByAccountIdAndCurrencyId(account.id, currency.id))
            }
            temp
        } else {
            Banks.findList { it.account eq account.id }
        }
    }

    fun getBanksByQQAndCurrencyName(qq: Long, vararg currencyNames: String): List<Bank> {
        return if (currencyNames.isEmpty()) {
            getBanksByQQ(qq)
        } else {
            val currencyIds = Currencies
                .select(Currencies.id)
                .where { Currencies.name inList currencyNames.asList() }
                .map { it.getLong(1) }
            val account = getAccountByQQ(qq)
            Banks.asSequence().filter { it.account eq account.id }.filter { it.currency inList currencyIds }.toList()
        }
    }

    /**
     * 根据QQ获取账号数据，若不存在，则会新建一个并返回。
     * @param qq 目标QQ号
     * @return 与QQ号对应的账号数据
     */
    fun getAccountByQQ(qq: Long): Account {
        var result = Accounts.findOne { it.qq eq qq }
        if (result == null) {
            val accountId = Accounts.insertAndGenerateKey {
                it.qq to qq
                it.level to DEFAULT_LEVEL
            }
            result = Accounts.findById(accountId)!!
        }
        return result
    }

    /**
     * 根据账号ID与货币ID获取对应的存款数据，若不存在，则会新建一个并返回。
     * @param accountId 目标账号ID
     * @param currencyId 目标货币ID
     * @return 目标账号名下的对应货币的存款数据
     */
    fun getBankByAccountIdAndCurrencyId(accountId: Long, currencyId: Long): Bank {
        var result = Banks.asSequence()
            .filter { it.account eq accountId }
            .filter { it.currency eq currencyId }
            .firstOrNull()
        if (result == null) {
            val bankId = Banks.insertAndGenerateKey {
                it.account to accountId
                it.currency to currencyId
                it.amount to 0
            }
            result = Banks.findById(bankId)!!
        }
        return result
    }

    fun getCurrencyByName(name: String): Currency? {
        return Currencies.findOne { it.name eq name }
    }

    fun getOrCreateCurrencyByName(name: String, defaultAmount: Long): Currency {
        var result = getCurrencyByName(name)
        if (result == null) {
            val currencyId = Currencies.insertAndGenerateKey {
                it.name to name
                it.defaultAmount to defaultAmount
            }
            result = Currencies.findById(currencyId)!!
        }
        return result
    }
}

val User.account: Account get() = BankModule.getAccountByQQ(this.id)
