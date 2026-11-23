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

共 139 位，按 HMCL 仓库顺序：

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
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
