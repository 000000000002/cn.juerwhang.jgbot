package cn.juerwhang.jgbot.modules.other

import cn.juerwhang.jgbot.modules.CqModule
import cn.juerwhang.jgbot.utils.doGet
import java.net.UnknownHostException
import java.util.*
import kotlin.collections.HashMap

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
        return "{id: %d, type: %s, from: %s, creator: %s, hitokoto: %s}".format(id, type, from, creator, hitokoto)
    }
}

object HitokotoModule: CqModule(
    true,
    "hitokoto",
    "一言，总有那么一句话能够戳中你的心。对 v1.hitokoto.cn 这一API进行封装的模块。"
) {
    private const val HITOKOTO_URL = "https://v1.hitokoto.cn"

    private val typeNameMapper = HashMap<String, String>()

    init {
        typeNameMapper["动画"] = "a"
        typeNameMapper["anime"] = "a"
        typeNameMapper["漫画"] = "b"
        typeNameMapper["comic"] = "b"
        typeNameMapper["游戏"] = "c"
        typeNameMapper["game"] = "c"
        typeNameMapper["小说"] = "d"
        typeNameMapper["novel"] = "d"
        typeNameMapper["原创"] = "e"
        typeNameMapper["myself"] = "e"
        typeNameMapper["网络"] = "f"
        typeNameMapper["internet"] = "f"
        typeNameMapper["其他"] = "g"
        typeNameMapper["other"] = "g"

        addEverywhereCommand("hitokoto", "一言") {event, sender, _, args ->
            val params = LinkedList<Pair<String, String>>()
            if (args.size > 0) {
                params.add(Pair("c", typeNameMapper.getOrDefault(args[0], "z")))
            }
            try {
                val hitokoto = doGet<Hitokoto>(HITOKOTO_URL, *params.toTypedArray())
                logger.log("一言的请求结果： %s".format(hitokoto.toString()))
                hitokoto.hitokoto
            } catch (e: UnknownHostException) {
                "啊呀……抱歉，暂时没有 Hitokoto 能告诉你呢……"
            }
        }
    }
}