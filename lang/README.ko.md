<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher는 중국어권 플레이어를 위해 **서버 다운로드 및 관리** 기능을 강화한 [HMCL](https://github.com/HMCL-dev/HMCL)의 서드파티 포크입니다.

> 이 프로젝트는 HMCL 공식 버전이 아니며 HMCL 팀과 무관합니다. HMCL 공식 지원은 제공되지 않습니다.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## 업스트림 대비 추가 사항

- 서버 다운로드: 바닐라 서버 및 Paper 빌드 목록
- Paper 설치 페이지: 플러그인 사전 설치, 다운로드 후 실행, 독립 서버 디렉터리
- 서버 플러그인 브라우저: Modrinth / Hangar 두 소스 선택 가능
- 서버 목록: 다중 디렉터리 관리, jar 내용 식별, 설정 편집
- 앱 내 server.properties 편집기
- 창 상단 밀착 애니메이션 수정(macOS)

## 빌드

JDK 21 필요:

    ./gradlew :HMCL:shadowJar

결과물은 HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar 입니다.

사용자 지정 Microsoft OAuth 앱 ID:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## 기여자

### HMCL 업스트림

HMCL 저장소 순서로 139명:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## 라이선스 및 저작자 표시

이 프로젝트는 **GPL-3.0**을 따르며 HMCL의 저작권과 표시를 유지합니다. 자세한 내용은 LICENSE와 소스 헤더를 참고하세요.

수정 버전을 배포할 때:

- HMCL 저작권 고지를 유지하세요;
- 공식 빌드와 구분되는 다른 소프트웨어 이름/버전을 사용하세요;
- HMCL 공식 빌드라고 주장하지 마세요.

## 피드백

문제, 제안, 문의는 [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback)에 제출하세요.

## 링크

- 저장소：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- 피드백：<https://github.com/SilentCodeTeams/feedback>
- 업스트림：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher는 서버 다운로드 및 관리에 중점을 둔 HMCL 커뮤니티 포크입니다. HMCL 팀과 무관하며 지원되지 않습니다. GPL-3.0 라이선스입니다.
