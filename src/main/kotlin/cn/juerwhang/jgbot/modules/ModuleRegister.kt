package cn.juerwhang.jgbot.modules

import cn.juerwhang.jgbot.modules.basic.BasicModule
import cn.juerwhang.jgbot.modules.core.ErrorHandlerModule
import cn.juerwhang.jgbot.modules.core.RemoteConfigModule
import cn.juerwhang.jgbot.modules.economy.BankModule
import cn.juerwhang.jgbot.modules.economy.SignupModule
import cn.juerwhang.jgbot.modules.other.HitokotoModule


/**
 * 当前版本号
 */
const val CURRENT_VERSION = "alpha.1.3"
const val CURRENT_VERSION_SUMMARY = """
当前版本更新日期：2019年11月11日
"""

/**
 * 待注册模块，用于手动添加需要注册的模块。
 * Tips: 本来打算使用扫描器扫描包并自动加载，但是仔细想想，这样效率挺低的。也许之后会这么做，目前先一切从简吧。 -- JuerWhang 2019/11/07
 */
val registerModules = arrayOf(
    ErrorHandlerModule,
    RemoteConfigModule,
    BasicModule,
    BankModule,
    HitokotoModule,
    SignupModule
)
