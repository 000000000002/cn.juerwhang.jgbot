package cn.juerwhang.jgbot.modules.core

import cn.hutool.json.JSONUtil
import cn.juerwhang.jgbot.modules.basic.entities.BaseTable
import cn.juerwhang.jgbot.modules.core.entities.RemoteConfigs
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.dsl.insertAndGenerateKey
import me.liuwj.ktorm.entity.*
import java.lang.IllegalArgumentException

object RemoteConfigModule: CqModule(
    true,
    "远程配置模块",
    "为其他模块提供远程配置能力，并开放一个web应用，用于编辑相关配置。"
) {
    override val usingTable: List<BaseTable<*>> = arrayListOf(
        RemoteConfigs
    )

    val configCache = HashMap<CqModule, MutableMap<String, String>>()

    init {
        addPrivateCommand("刷新配置", "config-refresh", "cfg-refresh") {
            val count = configCache.size
            configCache.clear()
            "远程配置的缓存共有 %s 条，已被清空！".format(count)
        }
    }
}

inline operator fun <reified T> CqModule.get(index: String): T {
    val cache = RemoteConfigModule.configCache.getOrPut(this) { HashMap() }
    if (!cache.containsKey(index)) {
        RemoteConfigModule.logger.log("缓存中未存在相关配置，尝试从远程配置获取：%s.%s".format(this.name, index))
        var config = RemoteConfigs.asSequence()
            .filter { it.module eq this.name }
            .filter { it.name eq index }
            .firstOrNull()
        if (config == null && this.defaultConfig.containsKey(index)) {
            RemoteConfigModule.logger.log("远程配置中未存在相关配置，尝试从默认值中创建：%s.%s".format(this.name, index))
            val newId = RemoteConfigs.insertAndGenerateKey {
                it.module to name
                it.name to index
                it.value to defaultConfig[index]
            }
            config = RemoteConfigs.findById(newId)!!
        }
        cache[index] = config?.value?:throw IllegalArgumentException("模块 [ %s ] 中并没有配置参数：%s".format(this.name, index))
    }
    return JSONUtil.toBean(cache[index], T::class.java)
}
