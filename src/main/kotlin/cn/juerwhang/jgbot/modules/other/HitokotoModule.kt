package cn.juerwhang.jgbot.modules.other

import cn.juerwhang.jgbot.modules.core.CqModule
import cn.juerwhang.jgbot.modules.core.conf
import cn.juerwhang.jgbot.utils.doGet
import cn.juerwhang.jgbot.utils.toJson
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
        return this.toJson()
    }
}

object HitokotoModule: CqModule(
    true,
    "hitokoto",
    "一言，总有那么一句话能够戳中你的心。对 v1.hitokoto.cn 这一API进行封装的模块。"
) {
    private val typeNameMapper = HashMap<String, String>()

    private val url by conf("https://v1.hitokoto.cn")

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

        addEverywhereCommand("hitokoto", "一言") {
            val params = LinkedList<Pair<String, String>>()
            if (args.size > 0) {
                params.add(Pair("c", typeNameMapper.getOrDefault(args[0], "z")))
            }
            try {
                val hitokoto = doGet<Hitokoto>(this@HitokotoModule.url, *params.toTypedArray())
                logger.log("一言的请求结果： %s".format(hitokoto.toString()))
                hitokoto.hitokoto
            } catch (e: UnknownHostException) {
                "啊呀……抱歉，暂时没有 Hitokoto 能告诉你呢……"
            }
        }
    }
}