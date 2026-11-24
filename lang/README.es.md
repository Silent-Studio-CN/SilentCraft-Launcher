<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher es un fork de terceros de [HMCL](https://github.com/HMCL-dev/HMCL) para jugadores chinos, centrado en la **descarga y gestión de servidores**.

> Este proyecto no es una versión oficial de HMCL ni está afiliado al equipo de HMCL. HMCL no ofrece soporte para este fork.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## Novedades respecto al upstream

- Descarga de servidores: servidor vanilla y lista de builds de Paper
- Página de instalación de Paper: plugins preinstalados, inicio tras la descarga, directorio de servidor aislado
- Explorador de plugins de servidor: fuentes Modrinth / Hangar seleccionables
- Lista de servidores: gestión multidirectorio, inspección de jar, edición de configuración
- Editor de server.properties integrado
- Corrección de la animación de ventana pegada al borde superior (macOS)

## Compilación

Requiere JDK 21:

    ./gradlew :HMCL:shadowJar

El artefacto está en HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar.

ID de aplicación OAuth de Microsoft personalizado:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## Colaboradores

### HMCL upstream

139 colaboradores, en el orden del repositorio de HMCL:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## Licencia y atribución

Este proyecto usa **GPL-3.0** y conserva los derechos de autor y la atribución de HMCL. Consulta LICENSE y las cabeceras de los archivos.

Al distribuir una versión modificada:

- Conserva los avisos de copyright de HMCL;
- Usa un nombre/versión diferente para distinguirla de la versión oficial;
- No afirmes que es una compilación oficial de HMCL.

## Comentarios

Envía problemas, sugerencias o consultas en [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback).

## Enlaces

- Repositorio：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- Comentarios：<https://github.com/SilentCodeTeams/feedback>
- Upstream：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher es un fork comunitario de HMCL centrado en la descarga y gestión de servidores. No está afiliado ni cuenta con el soporte del equipo de HMCL. Licencia GPL-3.0.
