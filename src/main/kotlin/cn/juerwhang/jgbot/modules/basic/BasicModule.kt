package cn.juerwhang.jgbot.modules.basic

import cn.juerwhang.juerobot.core.CqModule
import cn.juerwhang.juerobot.core.rc


object BasicModule: CqModule(
    "basic",
    "基础模块，负责提供最基本的控制功能。",
    true
) {
    private val ownerUser by rc(2695996944L)
    init {
        createPrivateCommand {
            name = "重载缓存"
            alias = listOf("reload", "rl")
            summary = "刷新 PicqBotX 中的缓存。"

            {
                if (ownerUser == it.sender.id) {
                    it.sender.bot.accountManager.refreshCache()

                    "缓存已重载！"
                } else {
                    ""
                }
            }
        }
    }
}
