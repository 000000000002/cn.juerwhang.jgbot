package cn.juerwhang.jgbot.modules.other

import cn.juerwhang.jgbot.utils.doGet
import cn.juerwhang.jgbot.utils.toJson
import cn.juerwhang.juerobot.core.CqModule
import cn.juerwhang.juerobot.core.rc
import cn.juerwhang.juerobot.utils.logger
import java.net.UnknownHostException
import java.util.*

/*
{
  "id": 3987,
  "hitokoto": "最令人伤心的事就是你发现自己已经做不好那件你曾经很擅长的事了。",
  "type": "f",
  "from": "elitegarfield",
  "creator": "綾音",
  "created_at": "1540529526"
}
 */
data class Hitokoto(
    val id: Long,
    val hitokoto: String,
    val type: String,
    val from: String,
    val creator: String,
    val createdAt: Long
) {
    override fun toString(): String {
        return this.toJson()
    }
}

object HitokotoModule: CqModule(
    "hitokoto",
    "一言，总有那么一句话能够戳中你的心。对 v1.hitokoto.cn 这一API进行封装的模块。",
    true
) {
    private val typeNameMapper = mapOf(
        "动画" to "a",
        "anime" to "a",

        "漫画" to "b",
        "comic" to "b",

        "游戏" to "c",
        "game" to "c",

        "小说" to "d",
        "novel" to "d",

        "原创" to "e",
        "myself" to "e",

        "网络" to "f",
        "internet" to "f",

        "其他" to "g",
        "other" to "g"
    )

    private val url by rc("https://v1.hitokoto.cn")
    private val netWorkNotFoundMessage by rc("啊呀……抱歉，暂时没有 Hitokoto 能告诉你呢……")

    init {
        createEverywhereCommand {
            name = "hitokoto"
            alias = listOf("一言")

            return@createEverywhereCommand {
                val params = LinkedList<Pair<String, String>>()
                if (it.args.isNotEmpty()) {
                    params.add(Pair("c", typeNameMapper.getOrDefault(it.args[0], "z")))
                }
                try {
                    val hitokoto = doGet<Hitokoto>(this@HitokotoModule.url, *params.toTypedArray())
                    logger.log("一言的请求结果： %s".format(hitokoto.toString()))
                    hitokoto.hitokoto
                } catch (e: UnknownHostException) {
                    netWorkNotFoundMessage
                }
            }
        }
    }
}