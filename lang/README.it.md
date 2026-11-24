<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher è un fork di terze parti di [HMCL](https://github.com/HMCL-dev/HMCL) per giocatori cinesi, incentrato su **download e gestione dei server**.

> Questo progetto non è una versione ufficiale di HMCL e non è affiliato al team HMCL. HMCL non fornisce supporto per questo fork.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## Novità rispetto all'upstream

- Download server: server vanilla ed elenco build Paper
- Pagina di installazione Paper: plugin preinstallati, avvio dopo il download, directory server isolata
- Browser plugin server: fonti Modrinth / Hangar selezionabili
- Elenco server: gestione multi-directory, ispezione jar, modifica configurazione
- Editor server.properties integrato
- Correzione animazione finestra agganciata in alto (macOS)

## Compilazione

Richiede JDK 21:

    ./gradlew :HMCL:shadowJar

L'artefatto è in HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar.

ID app OAuth Microsoft personalizzato:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## Contributori

### HMCL upstream

139 contributori, nell'ordine del repository HMCL:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## Licenza e attribuzione

Questo progetto usa **GPL-3.0** e mantiene copyright e attribuzione di HMCL. Vedi LICENSE e le intestazioni dei sorgenti.

Quando distribuisci una versione modificata:

- Mantieni gli avvisi di copyright di HMCL;
- Usa un nome/versione diverso per distinguerla dalla build ufficiale;
- Non dichiarare che è una build ufficiale di HMCL.

## Feedback

Invia problemi, suggerimenti o domande su [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback).

## Link

- Repository：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- Feedback：<https://github.com/SilentCodeTeams/feedback>
- Upstream：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher è un fork della community di HMCL incentrato su download e gestione dei server. Non è affiliato né supportato dal team HMCL. Licenza GPL-3.0.
