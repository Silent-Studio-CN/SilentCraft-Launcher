# SilentCraft Launcher

SilentCraft Launcher 是一个基于 [HMCL](https://github.com/HMCL-dev/HMCL) 的第三方 Minecraft 启动器分支，面向中文玩家，重点补充**服务端下载与管理**能力。

> 本项目不是 HMCL 官方版本，与 HMCL 开发团队无隶属关系。HMCL 官方不为本分支提供支持。

## 相比上游新增

- 服务端下载：原版服务端、Paper 构建列表
- Paper 安装页：预装插件、下载后启动、独立服务端目录
- 服务端插件浏览器：Modrinth / Hangar 双源，可勾选来源
- 服务端列表：多目录管理、jar 内容识别、配置编辑
- 应用内 server.properties 编辑器
- 窗口贴顶动画修复（macOS）

## 构建

需要 JDK 21：

    ./gradlew :HMCL:shadowJar

产物在 HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar。

自定义 Microsoft OAuth App ID：

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## 贡献者

### HMCL 上游

本分支基于 HMCL，以下为上游贡献者（按 HMCL 仓库顺序）：

<details>
<summary>全部 139 位</summary>

@huanghongxun @Glavo @yushijinhun @CiiLu @zkitefly @3gf8jv4dv @burningtnt @KSSJW
@ToobLac @Wulian233 @evilwk @neveler @Mine-diamond @fishdry1000 @Calboot @pynickle
@WhatDamon @Xirren @ShulkerSakura @ZekerZhayard @Lokins577 @vanja-san @Hylfrd @lorki
@wifi-left @Artoria2e5 @LuLu-ling @zhixuan2333 @ZZZank @OrzMiku @IceSoulHanxi @Rovniced
@pan93412 @Pigeon0v0 @yaoxi-std @e74yp8 @Tungstend @tsl0922 @BANSAFAn @MinecraftYJQ
@zly2006 @gunngz @hejiehao @TomatoPuddin @NekoCaffeine @ShadowLoveElysia @ShellWen @fatelove42
@catsout @jihuayu @yuanretro @rootwhois @xmdhs @xiaoyv404 @Cat7373 @hellckt
@Redlnn @CaaMoe @8MiYile @xfl03 @BBleae @Byacrya @slmpc @Chikage0o0
@Minecraft269 @AnemoFlower @Lonelyxiya @LasmGratel @GeneralK1ng @HomamLazaref @JasonKhew96 @kfatyuip
@hihaar @roj234 @glowinXD @gonglinyuan @dxNeil @dovisutu @asl97 @YuanYuanOwO
@YuChuXi @SALTWOOD @inclyc @YSXX1013 @YELANDAOKONG @TNTyep520 @Sharll-large @SettingDust
@Chen-Mengze @MingcongBai @TunYuntuwuQWQ @zimzaza4 @z0z0r4 @yangyangdaji @xtexx @vochant
@TTsdzb @skbeh @olivezane @nekoside-dev @mslxl @machinesmith42 @lyuxc-unknow @lcxn123
@TSaltedfishKing @Henryxjh @AdminHcat @Fronut @Ghost-chu @BartcreeBart @chang196700 @ExDragine
@cheny0y0 @CaveNightingale @AustinC2 @Churk-Ben @Asnxthaony @Andepthy @MindsMaster @a012-alex
@Chihsiao @onion108 @Rei-Hatada @mzdluo123 @outloudvi @paladini-maker @mcendu @NWMA-FYWF
@Mythologyli @MovTery @mikelei8291 @Krasjet @mvanhorn @luochen1990 @Dobby233Liu @Kylepoops
@zyxkad @KiteAB @Szizoid

</details>

### SilentCraft

- @Silent-xiaomiao

## 许可证与署名

本项目同样采用 **GPL-3.0**，并保留 HMCL 的版权与署名。你可以在 LICENSE 和源文件头中查看完整信息。

修改版分发时请：

- 保留 HMCL 的版权声明；
- 使用不同的软件名称/版本来区分官方版本；
- 不宣称是 HMCL 官方构建。

## 链接

- 仓库：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- 上游：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher is a community fork of HMCL focused on server-side download and management. It is not affiliated with or supported by the HMCL team. Licensed under GPL-3.0.
