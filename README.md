# LinearAim

[![build](https://github.com/xddno/LinearAim/actions/workflows/build.yml/badge.svg)](https://github.com/xddno/LinearAim/actions/workflows/build.yml)
[![license](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![release](https://img.shields.io/github/v/release/xddno/LinearAim)](https://github.com/xddno/LinearAim/releases/latest)

**English** | [简体中文](README_zh-CN.md)

Turns Minecraft's mouse sensitivity into a **linear** scale, with a parameter set modelled on
mainstream FPS games.

Vanilla uses a cubic curve: the slider value is mapped to `f = slider * 0.6 + 0.2`, and the final
turn speed is `8 * f³`. The low end feels mushy while the high end suddenly becomes twitchy, which
makes it hard to keep the same muscle memory across games.

LinearAim takes over `MouseHandler#turnPlayer` so that turn speed is **strictly proportional** to
the sensitivity value, and bypasses the vanilla sensitivity setting entirely — no matter how you
drag the vanilla slider, it has no effect.

## Parameters

| Parameter | Default | Range / step | Description |
| --- | --- | --- | --- |
| `sensitivity` | `6.820` | `0.05 ~ 30.00`, step `0.005` | Sensitivity value from mainstream FPS games; enter your own number |
| `sensitivity_y_scale` | `1.00` | `0.10 ~ 3.00`, step `0.01` | Extra multiplier for vertical movement |
| `m_yaw` | `0.022` | `0.001 ~ 0.100`, step `0.001` | Horizontal angle factor, in degrees per count |
| `m_pitch` | `0.022` | `0.001 ~ 0.100`, step `0.001` | Vertical angle factor, in degrees per count |
| `invertY` | `false` | toggle | Invert the Y axis (overrides vanilla "Invert Mouse") |
| `enabled` | `true` | toggle | Master switch; turn off to restore vanilla behaviour |

Angle conversion follows the classic FPS convention:

```
degrees per count (horizontal) = sensitivity × m_yaw
degrees per count (vertical)   = sensitivity × m_pitch × sensitivity_y_scale
```

So `sensitivity 6.25` with `m_yaw 0.022` gives 0.1375° per count — identical to what the same
numbers produce elsewhere. Minecraft's `Entity#turn` multiplies the turn argument by 0.15 to obtain
degrees, so the mod converts using `0.15 / 0.022`.

Vanilla's default feel (slider at 50%, i.e. 0.15° per count) corresponds to **6.818**, which is why
the default is `6.820`. DPI does not affect this conversion — it only determines eDPI, and the unit
is the same on both sides.

Notes:

- On first launch (no config file yet) the vanilla sensitivity is converted into an equivalent
  linear value, so installing the mod does not change how the game feels
- While scoping (spyglass), vanilla's 1/8 sensitivity reduction is preserved
- Config file: `config/linearaim.json`

## In-game commands (client)

```
/linearaim                        show all parameters
/linearaim sensitivity 6.25
/linearaim y_scale 1.0
/linearaim m_yaw 0.022
/linearaim m_pitch 0.022
/linearaim invertY true
/linearaim enabled false
/linearaim reset
```

## GUI

- The "Config" button inside Mod Menu
- Or press **F10** in game

Two pages:

- **Sensitivity**: slider (`−` / `+` in 0.005 steps) and an exact input box (type `1.625` directly)
- **Advanced**: `m_yaw`, `m_pitch`, `sensitivity_y_scale`, invert Y, master switch

With a slider focused, `←` / `→` adjusts it by one step.

## Requirements

- Minecraft **1.21.11**
- Fabric Loader **0.20.1+**
- Fabric API
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional, only provides the config entry point)

## Building

Loom 1.19 requires **JDK 25 to run Gradle**, while the produced bytecode targets Java 21 (what the
game requires).

```bash
export JAVA_HOME="/path/to/jdk-25"
./gradlew build
```

Output: `build/libs/linearaim-1.0.0.jar`

All dependencies come from official sources: `services.gradle.org` (Gradle distribution),
Maven Central, Fabric Maven and Terraformers Maven.

If you are on a slow connection in mainland China, you can point them at a local mirror:

- Replace `distributionUrl` in `gradle/wrapper/gradle-wrapper.properties` with
  `https://mirrors.cloud.tencent.com/gradle/gradle-9.8.2-bin.zip`
- Add `https://maven.aliyun.com/repository/public` to `repositories` in both `settings.gradle.kts`
  and `build.gradle.kts`

## Contributing and branch policy

`main` is protected:

- Direct pushes are rejected; every change goes through a pull request
- Force pushes and branch deletion are disabled
- Code changes automatically run `build`; documentation-only changes (`*.md`, `LICENSE`,
  `.gitignore`, etc.) do not trigger CI

The repository ships a `pre-push` hook. Enable it once locally to catch mistakes before they reach
the remote:

```bash
git config core.hooksPath .githooks
```

## License and compliance

- The project's **own code** is licensed under [MIT](LICENSE), copyright xddno.
- This project **contains no** Minecraft code, assets or decompiled output. Minecraft and related
  rights belong to Mojang / Microsoft and are governed by the
  [Minecraft EULA](https://account.mojang.com/documents/minecraft_eula). MIT covers only the
  original code in this repository, not Minecraft itself.
- Builds use **Mojang's official mappings** (`loom.officialMojangMappings()`) through Fabric Loom.
  Under Mojang's terms the mappings may be copied and used for development purposes, but may not be
  redistributed complete and unmodified. This project **does not distribute any mapping files**:
  Loom fetches them from Mojang's servers at build time and caches them locally.
- The published jar is processed by `remapJar`, so all internal references use Fabric
  **intermediary** names (`class_xxx` / `method_xxx`) and contain no readable names from Mojang's
  mappings.
- Parts of this project's code were generated with the assistance of AI.

If you want to avoid the Mojang mapping licensing question entirely, switch `build.gradle.kts` to
the CC0 Yarn mappings — at the cost of rewriting the source against Yarn's naming.
