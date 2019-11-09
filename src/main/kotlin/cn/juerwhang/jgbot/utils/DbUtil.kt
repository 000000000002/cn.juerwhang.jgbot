package cn.juerwhang.jgbot.utils

import me.liuwj.ktorm.database.Database

const val DB_PATH = "./jg.bot.sqlite"
fun initConnect() {
    Database.connect("jdbc:sqlite:%s".format(DB_PATH), "org.sqlite.JDBC")
}