<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher é um fork de terceiros do [HMCL](https://github.com/HMCL-dev/HMCL) para jogadores chineses, focado em **download e gerenciamento de servidores**.

> Este projeto não é uma versão oficial do HMCL e não é afiliado à equipe do HMCL. O HMCL não oferece suporte a este fork.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## Novidades em relação ao upstream

- Download de servidores: servidor vanilla e lista de builds do Paper
- Página de instalação do Paper: plugins pré-instalados, iniciar após o download, diretório de servidor isolado
- Navegador de plugins de servidor: fontes Modrinth / Hangar selecionáveis
- Lista de servidores: gerenciamento de vários diretórios, inspeção de jar, edição de configuração
- Editor de server.properties integrado
- Correção da animação da janela colada ao topo (macOS)

## Compilação

Requer JDK 21:

    ./gradlew :HMCL:shadowJar

O artefato está em HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar.

ID de aplicativo OAuth da Microsoft personalizado:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## Contribuidores

### HMCL upstream

139 contribuidores, na ordem do repositório HMCL:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## Licença e atribuição

Este projeto usa **GPL-3.0** e mantém os direitos autorais e a atribuição do HMCL. Veja LICENSE e os cabeçalhos dos arquivos.

Ao distribuir uma versão modificada:

- Mantenha os avisos de copyright do HMCL;
- Use um nome/versão diferente para distinguir da versão oficial;
- Não afirme que é uma compilação oficial do HMCL.

## Feedback

Envie problemas, sugestões ou dúvidas em [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback).

## Links

- Repositório：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- Feedback：<https://github.com/SilentCodeTeams/feedback>
- Upstream：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher é um fork comunitário do HMCL focado em download e gerenciamento de servidores. Não é afiliado nem suportado pela equipe do HMCL. Licenciado sob GPL-3.0.
