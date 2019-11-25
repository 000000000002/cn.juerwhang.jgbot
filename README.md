# JGBot - 一个自用的CQ机器人

该项目是基于 [PicqBotX](https://github.com/HyDevelop/PicqBotX) 进行二次开发的 CoolQ 机器人，将原模型进行更高层次的封装，并努力使其开发模式更加 modern。

## 努力的方向

1. 更适合函数式编程范式 —— 尤其是在 Command 这一概念上，十分适合使用函数式编程范式。
    - 努力使其能够更方便的使用 lambda 表达式。

2. 还没有什么想法 OuO 以后再说。

## 食用方法

> 以下内容适用于版本 alpha.1.3

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
    // alpha.1.3之后添加新功能，使用远程配置，更加灵活
    // 使用 2695996944L 作为默认值
    private var ownerUser by conf(2695996944L)

    init {
        // 注册一个私聊命令，用于重载 Picq 的缓存。
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
```

2. 将这个模块加入到待注册列表中，等待注册：
```kotlin
/**
 * -> cn/juerwhang/jgbot/modules/ModuleRegister.kt
 */

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
## 版本计划&更新日志

### alpha

##### alpha.2.0

- 从原有开发框架中脱离，单纯作为 CqModule 的集合，并调用原有框架。
- 原有开发框架独立为 [Juerobot](https://github.com/JuerGenie/juerobot)，并进行单独维护，若要进行二次开发，可直接拉取该项目的分支。

##### alpha.1.3

- 使用已有Sqlite作为远程配置，将原本写死与代码中的配置分离出来
    - 已完成远程配置模块
    - 已分离各个模块中的配置
        - 为分离提示信息，需要实现简易的模板功能
            - 已使用正则表达式进行初步实现

- 为插件的远程配置制作一个前端页面，用于修改远程配置
    - 待完成（该计划转移至 alpha.2.+ 版本完成）

- 记录版本更迭
    - 在做了
