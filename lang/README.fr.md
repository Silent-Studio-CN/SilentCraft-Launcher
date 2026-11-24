<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher est un fork tiers de [HMCL](https://github.com/HMCL-dev/HMCL) destiné aux joueurs chinois, axé sur le **téléchargement et la gestion de serveurs**.

> Ce projet n'est pas une version officielle de HMCL et n'est pas affilié à l'équipe HMCL. HMCL ne fournit pas de support pour ce fork.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## Nouveautés par rapport à l'amont

- Téléchargement de serveurs : serveur vanilla et liste de builds Paper
- Page d'installation Paper : plugins préinstallés, lancement après téléchargement, dossier de serveur isolé
- Navigateur de plugins de serveur : sources Modrinth / Hangar sélectionnables
- Liste des serveurs : gestion multi-dossiers, inspection des jar, édition de configuration
- Éditeur server.properties intégré
- Correctif d'animation de fenêtre collée en haut (macOS)

## Compilation

Nécessite JDK 21 :

    ./gradlew :HMCL:shadowJar

L'artefact se trouve dans HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar.

ID d'application OAuth Microsoft personnalisé :

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## Contributeurs

### HMCL amont

139 contributeurs, dans l'ordre du dépôt HMCL :

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## Licence et attribution

Ce projet utilise **GPL-3.0** et conserve les droits d'auteur et l'attribution de HMCL. Voir LICENSE et les en-têtes des sources.

Lors de la distribution d'une version modifiée :

- Conservez les mentions de copyright de HMCL ;
- Utilisez un nom/version différent pour la distinguer de la version officielle ;
- Ne prétendez pas qu'il s'agit d'une compilation officielle de HMCL.

## Retour

Signalez problèmes, suggestions ou questions sur [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback).

## Liens

- Dépôt：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- Retour：<https://github.com/SilentCodeTeams/feedback>
- Amont：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher est un fork communautaire de HMCL axé sur le téléchargement et la gestion de serveurs. Il n'est ni affilié ni supporté par l'équipe HMCL. Sous licence GPL-3.0.
