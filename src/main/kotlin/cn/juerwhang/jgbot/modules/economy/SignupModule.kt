package cn.juerwhang.jgbot.modules.economy

import cn.juerwhang.jgbot.modules.economy.entities.Account
import cn.juerwhang.jgbot.modules.economy.entities.SignupLog
import cn.juerwhang.jgbot.modules.economy.entities.SignupLogs
import cn.juerwhang.jgbot.utils.call
import cn.juerwhang.jgbot.utils.formatDateTime
import cn.juerwhang.juerobot.core.CqModule
import cn.juerwhang.juerobot.core.rc
import cn.juerwhang.juerobot.store.BaseTable
import cn.juerwhang.juerobot.utils.asTemplate
import cn.juerwhang.juerobot.utils.isToday
import me.liuwj.ktorm.dsl.count
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.insert
import me.liuwj.ktorm.entity.*
import java.time.LocalDateTime
import kotlin.random.Random

object SignupModule: CqModule(
    "签到模块",
    "提供每日签到功能，并奖励指定的货币。",
    true
) {
    private val signupAwardCurrency by rc("节操")
    private val SIGNUP_SUCCESS_TEMPLATE by rc(
        "==== 签到成功 ====\n" +
                ">> 签到时间：&sign-time&\n" +
                ">> 签到奖励：&amount& &currency-name&".trimIndent())
    private val SIGNUP_ALREADY_TEMPLATE by rc(
        "==== 签到失败 ====\n" +
                ">> 您今天已经签到啦！\n" +
                ">> 签到时间：&sign-time&".trimIndent())
    private val SIGNUP_INFO_TEMPLATE by rc(
        "==== 签到详情 ====\n" +
                ">> 上次签到时间：&sign-time&（&sign-status&）\n" +
                ">> 已经签到了 &sign-number& 次".trimIndent())

    private val SIGNUP_AWARD_RANGE: Pair<Long, Long> by rc(1L to 100L)
    private val awardCurrencyId: Long by lazy {
        BankModule.getOrCreateCurrencyByName(signupAwardCurrency, 0).id
    }

    override val tableDependencies: List<BaseTable<*>>
        get() = listOf(SignupLogs)

    init {
        createGroupCommand {
            name = "签到"
            alias = listOf("signup")
            summary = "进行签到，并获取随机数量的指定货币作为签到奖励。"

            {
                val account = BankModule.getAccountByQQ(it.sender.id)
                if (account.alreadySignup()) {
                    SIGNUP_ALREADY_TEMPLATE.asTemplate(
                        "sign-time" to account.getLastSignupTime()!!.formatDateTime()
                    )
                } else {
                    val awardAmount = Random.nextLong(SIGNUP_AWARD_RANGE.first, SIGNUP_AWARD_RANGE.second)

                    val bank = BankModule.getBankByAccountIdAndCurrencyId(account.id, awardCurrencyId)
                    bank.amount += awardAmount
                    bank.flushChanges()

                    SignupLogs.insert { target ->
                        target.account to account.id
                        target.amount to awardAmount
                    }

                    SIGNUP_SUCCESS_TEMPLATE.asTemplate(
                        "sign-time" to account.getLastSignupTime()!!.formatDateTime(),
                        "amount" to awardAmount.toString(),
                        "currency-name" to signupAwardCurrency
                    )
                }
            }
        }

        createEverywhereCommand {
            name = "签到详情"
            alias = listOf("签到情况", "signup-info", "su-info")
            summary = "获取签到详情。"

            {
                val lastSignupDate = it.sender.account.getLastSignupTime()
                SIGNUP_INFO_TEMPLATE.asTemplate(
                    "sign-time" to (lastSignupDate?.formatDateTime() ?: "未签到"),
                    "sign-number" to it.sender.account.getSignupCount().toString(),
                    "sign-status" to if (lastSignupDate?.isToday() == true) "今日已签到" else "今日未签到"
                )
            }
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
