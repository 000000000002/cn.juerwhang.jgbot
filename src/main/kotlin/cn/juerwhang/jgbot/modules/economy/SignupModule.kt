package cn.juerwhang.jgbot.modules.economy

import cn.juerwhang.jgbot.modules.core.CqModule
import cn.juerwhang.jgbot.modules.basic.entities.BaseTable
import cn.juerwhang.jgbot.modules.economy.entities.Account
import cn.juerwhang.jgbot.modules.economy.entities.SignupLog
import cn.juerwhang.jgbot.modules.economy.entities.SignupLogs
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
    private const val SIGNUP_AWARD_CURRENCY = "节操"
    private val SIGNUP_SUCCESS_TEMPLATE = """
        ==== 签到成功 ====
        >> 签到时间：%s
        >> 签到奖励：%d %s
    """.trimIndent()
    private val SIGNUP_ALREADY_TEMPLATE = """
        ==== 签到失败 ====
        >> 您今天已经签到啦！
        >> 签到时间：%s
    """.trimIndent()
    private val SIGNUP_INFO_TEMPLATE = """
        ==== 签到详情 ====
        >> 上次签到时间：%s（今日%s签到）
        >> 已经签到了 %d 次
    """.trimIndent()

    private val SIGNUP_AWARD_RANGE = 5L..100L
    var awardCurrencyId: Long = 0L

    override val usingTable: List<BaseTable<*>> = arrayListOf(
        SignupLogs
    )

    init {
        // 初始化签到奖励的货币的ID
        val currency = BankModule.getOrCreateCurrencyByName(SIGNUP_AWARD_CURRENCY, 0)
        awardCurrencyId = currency.id

        addEverywhereCommand("签到", "signup") {
            val account = BankModule.getAccountByQQ(sender.id)
            if (account.alreadySignup()) {
                SIGNUP_ALREADY_TEMPLATE.format(account.getLastSignupTime()!!.formatDateTime())
            } else {
                val awardAmount = Random.nextLong(SIGNUP_AWARD_RANGE.first, SIGNUP_AWARD_RANGE.last)

                val bank = BankModule.getBankByAccountIdAndCurrencyId(account.id, awardCurrencyId)
                bank.amount += awardAmount
                bank.flushChanges()

                SignupLogs.insert {
                    it.account to account.id
                    it.amount to awardAmount
                }

                SIGNUP_SUCCESS_TEMPLATE.format(account.getLastSignupTime()!!.formatDateTime(), awardAmount, SIGNUP_AWARD_CURRENCY)
            }
        }

        addEverywhereCommand("签到详情", "签到情况", "signup-info", "su-info") {
            val lastSignupDate = sender.account.getLastSignupTime()

            SIGNUP_INFO_TEMPLATE.format(
                lastSignupDate?.formatDateTime()?:"未签到",
                if (lastSignupDate?.isToday() == true) "已" else "未",
                sender.account.getSignupCount()
            )
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
