package cn.juerwhang.jgbot.modules.basic

import cn.juerwhang.jgbot.modules.core.CqModule

object BasicModule: CqModule(true, "basic", "基础模块，负责提供最基本的控制功能。") {
    private const val ownerUser = 2695996944L
    init {
        addPrivateCommand("重载缓存", "reload", "rl") {
            if (ownerUser == sender.id) {
                sender.bot.accountManager.refreshCache()

                "缓存已重载！"
            } else {
                ""
            }
        }
    }
}
