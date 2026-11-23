# LinearAim

[![build](https://github.com/xddno/LinearAim/actions/workflows/build.yml/badge.svg)](https://github.com/xddno/LinearAim/actions/workflows/build.yml)
[![license](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

把 Minecraft 的鼠标灵敏度变**线性**，参数体系对齐主流 FPS。

原版的鼠标灵敏度是三次曲线：滑块值先映射成 `f = 滑块 * 0.6 + 0.2`，最终转向速度是 `8 * f³`。
所以滑块前段很肉、后段突然变快，很难和其他 FPS 保持一致的肌肉记忆。

LinearAim 直接接管 `MouseHandler#turnPlayer`，让转向速度与灵敏度数值成**严格正比**，
并把原版的灵敏度设置完全旁路——无论你在原版界面里怎么拖，都不会有任何效果。

## 参数

| 参数 | 默认 | 范围 / 步进 | 说明 |
| --- | --- | --- | --- |
| `sensitivity` | `6.820` | `0.05 ~ 30.00`，步进 `0.005` | 主流 FPS 的灵敏度数值，直接填你在其他游戏里的数 |
| `sensitivity_y_scale` | `1.00` | `0.10 ~ 3.00`，步进 `0.01` | 垂直方向额外倍率 |
| `m_yaw` | `0.022` | `0.001 ~ 0.100`，步进 `0.001` | 水平角度系数，单位：度/计数 |
| `m_pitch` | `0.022` | `0.001 ~ 0.100`，步进 `0.001` | 垂直角度系数，单位：度/计数 |
| `invertY` | `false` | 开关 | 反转 Y 轴（接管原版的「反转鼠标」） |
| `enabled` | `true` | 开关 | 总开关，关掉后完全回到原版 |

角度换算是经典 FPS 那套：

```
每计数转角(水平) = sensitivity × m_yaw
每计数转角(垂直) = sensitivity × m_pitch × sensitivity_y_scale
```

所以 `sensitivity 6.25` + `m_yaw 0.022` 就是每计数 0.1375 度，和你在其他游戏里完全一致。
Minecraft 的 `Entity#turn` 把 turn 参数乘以 0.15 得到度数，模组内部按 `0.15 / 0.022` 换算过去。

原版默认手感（原版滑块 50%，即 0.15 度/计数）对应 **6.818**，所以默认值取 `6.820`。
DPI 不影响这个换算，它只决定 eDPI，两边口径相同。

补充：

- 首次运行（没有配置文件时）自动把原版灵敏度换算成等效线性数值，装上模组不会有手感突变
- 开镜（望远镜）时仍保留原版的 1/8 降敏比例
- 配置文件：`config/linearaim.json`

## 游戏内命令（客户端）

```
/linearaim                        查看全部参数
/linearaim sensitivity 6.25
/linearaim y_scale 1.0
/linearaim m_yaw 0.022
/linearaim m_pitch 0.022
/linearaim invertY true
/linearaim enabled false
/linearaim reset
```

## 图形界面

- Mod Menu 里的「配置」按钮
- 或游戏内按 **F10**

界面分两页：

- **灵敏度**：滑块（`−`/`+` 每次 0.005）、精确输入框（可直接敲 `1.625`）
- **高级**：`m_yaw`、`m_pitch`、`sensitivity_y_scale`、反转 Y、总开关

滑块获得焦点后按 `←` / `→` 可以按 1 step 微调。

## 运行环境

- Minecraft **1.21.11**
- Fabric Loader **0.20.1+**
- Fabric API
- [Mod Menu](https://modrinth.com/mod/modmenu)（可选，只用于提供配置入口）

## 构建

Loom 1.19 要求**用 JDK 25 运行 Gradle**，但产物字节码目标是 Java 21（游戏要求）。

```bash
export JAVA_HOME="/d/Program Files/Eclipse Adoptium/jdk-25.0.5.8-hotspot"
./gradlew build
```

产物：`build/libs/linearaim-1.0.0.jar`

所有依赖都来自官方源：`services.gradle.org`（Gradle 分发）、Maven Central、Fabric Maven、Terraformers Maven。

中国大陆网络如果较慢，可以自行替换成本地镜像：

- `gradle/wrapper/gradle-wrapper.properties` 里的 `distributionUrl` 换成 `https://mirrors.cloud.tencent.com/gradle/gradle-9.8.2-bin.zip`
- 在 `settings.gradle.kts` 和 `build.gradle.kts` 的 `repositories` 里加 `https://maven.aliyun.com/repository/public`

## 许可与合规

- 本项目**自己的代码**采用 [MIT 许可证](LICENSE)，版权归 xddno。
- 本项目**不包含** Minecraft 的任何代码、资源或反编译产物。Minecraft 及其相关权利归 Mojang / Microsoft 所有，其使用受 [Minecraft EULA](https://account.mojang.com/documents/minecraft_eula) 约束。MIT 仅覆盖本仓库的原创代码，不覆盖 Minecraft。
- 构建时通过 Fabric Loom 使用 **Mojang 官方混淆映射**（`loom.officialMojangMappings()`）。按 Mojang 的条款，映射可供开发目的复制与使用，但不得完整、未修改地再分发。本项目**不随仓库分发任何映射文件**：映射由 Loom 在构建时从 Mojang 服务器获取并缓存在本地。
- 发布的 jar 经 `remapJar` 处理，内部引用全部是 Fabric 的 **intermediary** 名称（`class_xxx` / `method_xxx`），不包含 Mojang 映射中的可读名称。
- 本项目部分代码由 AI 辅助生成。

如果你需要完全规避 Mojang 映射的授权争议，可以把 `build.gradle.kts` 换成 CC0 的 Yarn 映射，代价是需要按 Yarn 的命名重写源码。
