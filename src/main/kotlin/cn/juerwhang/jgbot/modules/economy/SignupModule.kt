package cn.juerwhang.jgbot.modules.economy

import cn.juerwhang.jgbot.modules.core.CqModule
import cn.juerwhang.jgbot.modules.basic.entities.BaseTable
import cn.juerwhang.jgbot.modules.core.conf
import cn.juerwhang.jgbot.modules.economy.entities.Account
import cn.juerwhang.jgbot.modules.economy.entities.SignupLog
import cn.juerwhang.jgbot.modules.economy.entities.SignupLogs
import cn.juerwhang.jgbot.utils.asTemplate
import cn.juerwhang.jgbot.utils.call
import cn.juerwhang.jgbot.utils.formatDateTime
import cn.juerwhang.jgbot.utils.isToday
import me.liuwj.ktorm.dsl.count
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.entity.*
import java.time.LocalDateTime
import kotlin.random.Random

object SignupModule: CqModule(true, "签到模块", "提供每日签到功能，并奖励指定的货币。") {
    private val signupAwardCurrency by conf("节操")
    private val SIGNUP_SUCCESS_TEMPLATE by conf(
        "==== 签到成功 ====\n" +
                ">> 签到时间：&sign-time&\n" +
                ">> 签到奖励：&amount& &currency-name&".trimIndent())
    private val SIGNUP_ALREADY_TEMPLATE by conf(
        "==== 签到失败 ====\n" +
                ">> 您今天已经签到啦！\n" +
                ">> 签到时间：&sign-time&".trimIndent())
    private val SIGNUP_INFO_TEMPLATE by conf(
        "==== 签到详情 ====\n" +
                ">> 上次签到时间：&sign-time&（&sign-status&）\n" +
                ">> 已经签到了 &sign-number& 次".trimIndent())

    private val SIGNUP_AWARD_RANGE: Pair<Long, Long> by conf(1L to 100L)
    private var awardCurrencyId: Long = 0L

    override val usingTable: List<BaseTable<*>> get() = arrayListOf(
        SignupLogs
    )

    init {
        // 初始化签到奖励的货币的ID
        val currency = BankModule.getOrCreateCurrencyByName(signupAwardCurrency, 0)
        awardCurrencyId = currency.id

        addEverywhereCommand("签到", "signup") {
            val account = BankModule.getAccountByQQ(sender.id)
            if (account.alreadySignup()) {
                SIGNUP_ALREADY_TEMPLATE.format(account.getLastSignupTime()!!.formatDateTime())
            } else {
                val awardAmount = Random.nextLong(SIGNUP_AWARD_RANGE.first, SIGNUP_AWARD_RANGE.second)

                val bank = BankModule.getBankByAccountIdAndCurrencyId(account.id, awardCurrencyId)
                bank.amount += awardAmount
                bank.flushChanges()

                SignupLogs.insert {
                    it.account to account.id
                    it.amount to awardAmount
                }

                SIGNUP_SUCCESS_TEMPLATE.asTemplate(mapOf(
                    "sign-time" to account.getLastSignupTime()!!.formatDateTime(),
                    "amount" to awardAmount.toString(),
                    "currency-name" to signupAwardCurrency
                ))
            }
        }

        addEverywhereCommand("签到详情", "签到情况", "signup-info", "su-info") {
            val lastSignupDate = sender.account.getLastSignupTime()

//            SIGNUP_INFO_TEMPLATE.format(
//                lastSignupDate?.formatDateTime()?:"未签到",
//                if (lastSignupDate?.isToday() == true) "已" else "未",
//                sender.account.getSignupCount()
//            )
            SIGNUP_INFO_TEMPLATE.asTemplate(mapOf(
                "sign-time" to (lastSignupDate?.formatDateTime()?:"未签到"),
                "sign-number" to sender.account.getSignupCount().toString(),
                "sign-status" to if (lastSignupDate?.isToday() == true) "今日已签到" else "今日未签到"
            ))
        }
    }

    fun getSignupLogsByQQ(qq: Long): List<SignupLog> {
        val account = BankModule.getAccountByQQ(qq)
        return SignupLogs.findList { it.account eq account.id } .sortedByDescending { it.createDate }
    }

    fun Account.getSignupCount(): Int {
        return SignupLogs.count { it.account eq this.id }
    }

    fun Account.alreadySignup(): Boolean {
        val result = SignupLogs.asSequence()
            .filter {
                it.account eq this.id
            }
            .filter {
                call<String>("strftime", "%Y%m%d", it.createDate) eq call("strftime", "%Y%m%d", call<LocalDateTime>("datetime", "now", "localtime"))
            }
            .toList().size
        return result > 0
    }

    fun Account.getLastSignupTime(): LocalDateTime? {
        return getLastSignupLog()?.createDate
    }

    fun Account.getLastSignupLog(): SignupLog? {
        return SignupLogs.asSequence().filter { it.account eq id }.sortedByDescending { it.createDate }.firstOrNull()
    }
}
