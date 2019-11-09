# JGBot - 一个自用的CQ机器人

该项目是基于 [PicqBotX](https://github.com/HyDevelop/PicqBotX) 进行二次开发的 CoolQ 机器人，将原模型进行更高层次的封装，并努力使其开发模式更加 modern。

## 努力的方向

1. 更适合函数式编程范式 —— 尤其是在 Command 这一概念上，十分适合使用函数式编程范式。
    - 努力使其能够更方便的使用 lambda 表达式。

2. 还没有什么想法 OuO 以后再说。

## 食用方法

> 以下内容适用于版本 alpha.1.1

1. 编写一个模块，并继承 CqModule：
```kotlin
object BasicModule: CqModule(
    // 该模块是否被启用，若为 false，则该模块不会被注册。
    true,
    // 该模块的模块名，目前仅用于调试。
    "basic",
    // 该模块的描述，目前仅用于调试。
    "基础模块，负责提供最基本的控制功能。"
) {
    private const val ownerUser = 2695996944L
    init {
        // 注册一个私聊命令，用于重载 Picq 的缓存。
        addPrivateCommand("重载缓存", "reload", "rl") {_, sender, _, _ ->
            if (ownerUser == sender.id) {
                sender.bot.accountManager.refreshCache()

                "缓存已重载！"
            } else {
                ""
            }
        }
    }
}
```

2. 将这个模块加入到待注册列表中，等待注册：
```kotlin
// -> cn/juerwhang/jgbot/modules/ModuleRegister.kt

/**
 * 待注册模块，用于手动添加需要注册的模块。
 */
private val registerModules = arrayOf<CqModule>(
    BasicModule // <-- 添加在这里
)
```

3. 启动机器人，然后私聊它：
```text
You -> reload
Bot -> 缓存已重载！
```