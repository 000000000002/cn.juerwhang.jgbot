package cn.juerwhang.jgbot.modules.core

import cn.juerwhang.jgbot.modules.basic.entities.BaseTable
import cn.juerwhang.jgbot.modules.core.entities.RemoteConfigs
import cn.juerwhang.jgbot.utils.toJson
import cn.juerwhang.jgbot.utils.toObject
import me.liuwj.ktorm.dsl.*
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

inline fun <reified T: Any> CqModule.conf(defaultValue: T?): ModuleConfigDelegate<T> {
    return ModuleConfigDelegate(this, defaultValue, T::class.java)
}

typealias ConfigMap = MutableMap<RemoteConfigGetterArgument, String>
class ModuleConfigDelegate<T: Any>(
    private val module: CqModule,
    private val defaultValue: T?,
    private val clazz: Class<T>
): MutableMap<String, T> {
    companion object {
        private val argumentCache = HashMap<String, RemoteConfigGetterArgument>()
        fun getOrCreateArgumentFromCache(module: CqModule, name: String, defaultValue: Any?): RemoteConfigGetterArgument {
            val key = "${module.name}.$name"
            var result = argumentCache[key]
            if (result == null) {
                result = RemoteConfigGetterArgument(module.name, name, defaultValue?.toJson()?:"null")
                argumentCache[key] = result
            }
            return result
        }
    }

    override fun get(key: String): T? {
        return RemoteConfigMap[getOrCreateArgumentFromCache(module, key, defaultValue)]?.toObject(clazz)
    }
    override fun put(key: String, value: T): T? {
        return RemoteConfigMap.put(getOrCreateArgumentFromCache(module, key, defaultValue), value.toJson())?.toObject(clazz)
    }

    override val size: Int get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override fun containsKey(key: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun containsValue(value: T): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun isEmpty(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override val entries: MutableSet<MutableMap.MutableEntry<String, T>> get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val keys: MutableSet<String> get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val values: MutableCollection<T> get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override fun clear() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun putAll(from: Map<out String, T>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun remove(key: String): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

data class RemoteConfigGetterArgument(
    val module: String,
    val name: String,
    val defaultValue: String
)

object RemoteConfigMap: ConfigMap {
    private val configCache: ConfigMap = HashMap()

    override val size: Int get() = RemoteConfigs.count()

    override fun containsKey(key: RemoteConfigGetterArgument): Boolean = RemoteConfigs.asSequence()
        .filter { it.module eq key.module }
        .filter { it.name eq key.name }
        .count() > 0

    override fun containsValue(value: String): Boolean = RemoteConfigs.count { it.value eq value } > 0

    override fun get(key: RemoteConfigGetterArgument): String? {
        val temp = configCache[key]
        if (temp != null) {
            return temp
        } else {
            RemoteConfigModule.logger.log("缓存中未存在相关配置，尝试从远程配置获取：${key.module}.${key.name}")
            var config = RemoteConfigs.asSequence()
                .filter { it.module eq key.module }
                .filter { it.name eq key.name }
                .firstOrNull()

            if (config == null) {
                RemoteConfigModule.logger.log("远程配置中未存在相关配置，尝试从默认值中创建：${key.module}.${key.name}")
                val newId = RemoteConfigs.insertAndGenerateKey {
                    it.module to key.module
                    it.name to key.name
                    it.value to key.defaultValue
                }
                config = RemoteConfigs.findById(newId)!!
            }
            configCache[key] = config.value
            return config.value
        }
    }

    override fun isEmpty(): Boolean = false

    override val entries: MutableSet<MutableMap.MutableEntry<RemoteConfigGetterArgument, String>>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val keys: MutableSet<RemoteConfigGetterArgument>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
    override val values: MutableCollection<String>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun clear() {
        this.configCache.clear()
    }

    override fun put(key: RemoteConfigGetterArgument, value: String): String? {
        RemoteConfigModule.logger.log("更新远程配置：set ${key.module}.${key.name} = '$value'")
        RemoteConfigs.update {
            it.value to value
            where {
                it.module eq key.module and (it.name eq key.name)
            }
        }
        return this.configCache.put(key, value)
    }

    override fun putAll(from: Map<out RemoteConfigGetterArgument, String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(key: RemoteConfigGetterArgument): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
