<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher is een externe fork van [HMCL](https://github.com/HMCL-dev/HMCL) voor Chinese spelers, gericht op **serverdownload en -beheer**.

> Dit project is geen officiële HMCL-versie en is niet verbonden aan het HMCL-team. HMCL biedt geen ondersteuning voor deze fork.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## Nieuw ten opzichte van upstream

- Serverdownloads: vanilla-server en Paper-buildlijst
- Paper-installatiepagina: vooraf geïnstalleerde plugins, starten na download, aparte servermap
- Serverpluginbrowser: Modrinth / Hangar-bronnen selecteerbaar
- Serverlijst: beheer van meerdere mappen, jar-inspectie, configuratie bewerken
- In-app server.properties-editor
- Correctie van vensteranimatie bovenaan (macOS)

## Bouwen

Vereist JDK 21:

    ./gradlew :HMCL:shadowJar

Het artefact staat in HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar.

Aangepaste Microsoft OAuth-app-id:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## Bijdragers

### HMCL-upstream

139 bijdragers, in de volgorde van de HMCL-repository:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## Licentie en naamsvermelding

Dit project valt onder **GPL-3.0** en behoudt HMCL-auteursrecht en naamsvermelding. Zie LICENSE en bronbestandshoofden.

Bij het verspreiden van een gewijzigde versie:

- Behoud HMCL-auteursrechtmeldingen;
- Gebruik een andere softwarenaam/versie om het van de officiële build te onderscheiden;
- Beweer niet dat het een officiële HMCL-build is.

## Feedback

Meld problemen, suggesties of vragen op [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback).

## Links

- Repository：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- Feedback：<https://github.com/SilentCodeTeams/feedback>
- Upstream：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher is een communityfork van HMCL gericht op serverdownload en -beheer. Niet verbonden aan of ondersteund door het HMCL-team. Gelicenseerd onder GPL-3.0.
