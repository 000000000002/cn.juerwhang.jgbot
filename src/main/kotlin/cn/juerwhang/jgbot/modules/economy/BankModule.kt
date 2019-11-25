package cn.juerwhang.jgbot.modules.economy

import cc.moecraft.icq.user.User
import cn.juerwhang.jgbot.modules.economy.entities.*
import cn.juerwhang.jgbot.modules.economy.entities.Currency
import cn.juerwhang.juerobot.core.CqModule
import cn.juerwhang.juerobot.core.rc
import cn.juerwhang.juerobot.store.BaseTable
import cn.juerwhang.juerobot.utils.asTemplate
import me.liuwj.ktorm.dsl.*
import me.liuwj.ktorm.entity.*
import java.util.*


object BankModule: CqModule(
    "银行模块",
    "用于提供经济系统相关的操作。",
    true) {
    override val tableDependencies: List<BaseTable<*>>
        get() = listOf(
            Accounts,
            Currencies,
            Banks
        )

    private val ownerUser by rc(2695996944L)

    private val ACCOUNT_ALL_INFO_TEMPLATE by rc("==== 您当前的账户存款详情 ====")
    private val ACCOUNT_SOME_BANK_INFO_TEMPLATE by rc("==== 您当前查询的货币存款详情 ====")
    private val CURRENCY_INFO_TEMPLATE by rc("\n>> &curr-name&: &curr-count&")
    private val DEFAULT_LEVEL by rc(0)

    init {
        createEverywhereCommand {
            name = "存款"
            alias = listOf("账号", "存款信息")
            summary = "查看当前各个货币已有的存款。"

            {
                if (it.args.isEmpty()) {
                    val resultBuilder = StringBuilder(ACCOUNT_ALL_INFO_TEMPLATE)
                    for (bank in getBanksByQQ(it.sender.id)) {
                        resultBuilder.append(
                            CURRENCY_INFO_TEMPLATE.asTemplate(
                                "curr-name" to bank.currency.name,
                                "curr-count" to bank.amount.toString()
                            )
                        )
                    }
                    resultBuilder.toString()
                } else {
                    val resultBuilder = StringBuilder(ACCOUNT_SOME_BANK_INFO_TEMPLATE)

                    resultBuilder.toString()
                }
            }
        }

        createPrivateCommand {
            name = "添加货币"
            alias = listOf("add-currency", "add-crc")
            summary = "向货币列表中添加一个新货币，食用方法：添加货币 <货币名称> [默认货币量 = 0]"

            {
                if (it.sender.id == ownerUser) {
                    if (it.args.isEmpty()) {
                        "输入参数错误！正确格式：\n添加货币 <货币名称> [默认货币量 = 0]"
                    } else {
                        val currencyName = it.args[0]
                        val defaultValue = if (it.args.size > 1) it.args[1].toLong() else 0L
                        val newId = Currencies.insertAndGenerateKey { target ->
                            target.name to currencyName
                            target.defaultAmount to defaultValue
                        }
                        "[ $currencyName ] 添加成功！ID: $newId"
                    }
                } else {
                    ""
                }
            }
        }

        createPrivateCommand {
            name = "修改货币"
            alias = listOf("mod-currency", "mod-crc")
            summary = "修改货币列表中的指定货币的名称，食用方法：修改货币 <货币名称> <新名称> [默认货币量]"

            {
                if (it.sender.id == ownerUser) {
                    if (it.args.size < 2) {
                        "输入参数错误！正确格式：\n修改货币 <货币名称> <新名称> [默认货币量]"
                    } else {
                        val oldName = it.args[0]
                        val currency = getCurrencyByName(oldName)
                        if (currency == null) {
                            "不存在名为 [ $oldName ] 的货币！请尝试使用 添加货币 指令！"
                        } else {
                            val newName = it.args[1]
                            val defaultValue = if (it.args.size > 2) it.args[2].toLong() else currency.defaultAmount
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
        }

        createEverywhereCommand {
            name = "货币列表"
            alias = listOf("currency-list", "crc-list", "crc-ls")
            summary = "罗列出当前已有的货币列表。"

            {
                val resultBuilder = StringBuilder("==== 当前已有货币列表 ====\n")
                for (currency in Currencies.asSequence()) {
                    resultBuilder.appendln("[ ${currency.name} ( 默认值 : ${currency.defaultAmount} ) ]")
                }

                resultBuilder.toString()
            }
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
