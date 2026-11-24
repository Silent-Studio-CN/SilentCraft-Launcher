<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher is a third-party Minecraft launcher fork based on [HMCL](https://github.com/HMCL-dev/HMCL), aimed at Chinese players and focused on **server download and management**.

> This project is not an official HMCL version and is not affiliated with the HMCL team. HMCL does not provide support for this fork.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## New compared to upstream

- Server downloads: vanilla server and Paper build list
- Paper install page: preinstalled plugins, launch after download, isolated server directory
- Server plugin browser: Modrinth / Hangar dual sources with selectable sources
- Server list: multi-directory management, jar inspection, config editing
- In-app server.properties editor
- Top-flush window animation fix (macOS)

## Build

Requires JDK 21:

    ./gradlew :HMCL:shadowJar

The artifact is HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar.

Custom Microsoft OAuth App ID:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## Contributors

### HMCL upstream

139 contributors, in HMCL repository order:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## License and attribution

This project is also licensed under **GPL-3.0** and retains HMCL copyright and attribution. See LICENSE and source headers for details.

When distributing a modified version:

- Keep HMCL copyright notices;
- Use a different software name/version to distinguish it from the official build;
- Do not claim to be an official HMCL build.

## Feedback

Submit issues, suggestions or questions at [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback).

## Links

- Repository：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- Feedback：<https://github.com/SilentCodeTeams/feedback>
- Upstream：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher is a community fork of HMCL focused on server-side download and management. It is not affiliated with or supported by the HMCL team. Licensed under GPL-3.0.
