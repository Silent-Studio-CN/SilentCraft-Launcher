<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher — сторонний форк [HMCL](https://github.com/HMCL-dev/HMCL) для китайских игроков с упором на **загрузку и управление серверами**.

> Это не официальная версия HMCL, проект не связан с командой HMCL. HMCL не поддерживает этот форк.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## Новое по сравнению с upstream

- Загрузка серверов: ванильный сервер и список сборок Paper
- Страница установки Paper: предустановленные плагины, запуск после загрузки, изолированный каталог сервера
- Браузер плагинов сервера: источники Modrinth / Hangar с выбором
- Список серверов: управление несколькими каталогами, анализ jar, редактирование конфигурации
- Встроенный редактор server.properties
- Исправление анимации окна у верхнего края (macOS)

## Сборка

Требуется JDK 21:

    ./gradlew :HMCL:shadowJar

Артефакт: HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar.

Свой Microsoft OAuth App ID:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## Участники

### HMCL upstream

139 участников в порядке репозитория HMCL:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## Лицензия и атрибуция

Проект распространяется по **GPL-3.0** с сохранением авторских прав и атрибуции HMCL. Подробности в LICENSE и заголовках исходников.

При распространении изменённой версии:

- Сохраняйте уведомления об авторских правах HMCL;
- Используйте другое имя/версию, чтобы отличить от официальной сборки;
- Не заявляйте, что это официальная сборка HMCL.

## Обратная связь

Сообщайте о проблемах и предложениях на [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback).

## Ссылки

- Репозиторий：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- Обратная связь：<https://github.com/SilentCodeTeams/feedback>
- Upstream：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher — общественный форк HMCL для загрузки и управления серверами. Не связан с командой HMCL и не поддерживается ею. Лицензия GPL-3.0.
