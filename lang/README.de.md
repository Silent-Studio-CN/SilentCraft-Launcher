<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher ist ein Drittanbieter-Fork von [HMCL](https://github.com/HMCL-dev/HMCL) für chinesische Spieler mit Fokus auf **Server-Download und -Verwaltung**.

> Dieses Projekt ist keine offizielle HMCL-Version und steht in keiner Verbindung zum HMCL-Team. HMCL bietet für diesen Fork keinen Support.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## Neu gegenüber Upstream

- Server-Downloads: Vanilla-Server und Paper-Build-Liste
- Paper-Installationsseite: vorinstallierte Plugins, Start nach dem Download, isoliertes Serververzeichnis
- Server-Plugin-Browser: Modrinth- / Hangar-Quellen auswählbar
- Serverliste: Mehrordnerverwaltung, Jar-Prüfung, Konfigurationsbearbeitung
- Integrierter server.properties-Editor
- Korrektur der Fensteranimation am oberen Rand (macOS)

## Build

Benötigt JDK 21:

    ./gradlew :HMCL:shadowJar

Das Artefakt liegt unter HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar.

Eigene Microsoft-OAuth-App-ID:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## Mitwirkende

### HMCL-Upstream

139 Mitwirkende, in der Reihenfolge des HMCL-Repositories:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## Lizenz und Namensnennung

Dieses Projekt steht unter **GPL-3.0** und behält HMCL-Urheberrechte und -Namensnennung. Siehe LICENSE und Quelldateiköpfe.

Bei der Verteilung einer geänderten Version:

- HMCL-Copyright-Hinweise beibehalten;
- Anderen Softwarenamen/-version verwenden, um sie vom offiziellen Build zu unterscheiden;
- Nicht behaupten, ein offizieller HMCL-Build zu sein.

## Feedback

Probleme, Vorschläge oder Fragen unter [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback) melden.

## Links

- Repository：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- Feedback：<https://github.com/SilentCodeTeams/feedback>
- Upstream：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher ist ein Community-Fork von HMCL mit Fokus auf Server-Download und -Verwaltung. Nicht mit dem HMCL-Team verbunden oder von ihm unterstützt. Lizenziert unter GPL-3.0.
