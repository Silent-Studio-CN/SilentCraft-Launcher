<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher は、中国語圏のプレイヤー向けに **サーバーのダウンロードと管理** を強化した [HMCL](https://github.com/HMCL-dev/HMCL) のサードパーティフォークです。

> 本プロジェクトは HMCL の公式版ではなく、HMCL チームとは無関係です。HMCL 公式によるサポートはありません。
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## 上流からの追加点

- サーバーダウンロード：バニラサーバーと Paper ビルド一覧
- Paper インストールページ：プラグインの事前導入、ダウンロード後の起動、独立したサーバーディレクトリ
- サーバープラグインブラウザ：Modrinth / Hangar の2ソースを選択可能
- サーバー一覧：複数ディレクトリ管理、jar 内容の識別、設定編集
- アプリ内 server.properties エディタ
- ウィンドウ上部吸着アニメーションの修正（macOS）

## ビルド

JDK 21 が必要です：

    ./gradlew :HMCL:shadowJar

成果物は HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar です。

カスタム Microsoft OAuth アプリ ID：

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## コントリビューター

### HMCL 上流

HMCL リポジトリ順で 139 名：

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## ライセンスと表記

本プロジェクトは **GPL-3.0** で提供され、HMCL の著作権と表記を保持します。詳細は LICENSE とソースヘッダーを参照してください。

改変版を配布する場合：

- HMCL の著作権表示を保持すること；
- 公式版と区別できるよう別のソフト名/バージョンを使用すること；
- HMCL 公式ビルドであると主張しないこと。

## フィードバック

問題・提案・質問は [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback) へ。

## リンク

- リポジトリ：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- フィードバック：<https://github.com/SilentCodeTeams/feedback>
- 上流：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher は、サーバーのダウンロードと管理に焦点を当てた HMCL のコミュニティフォークです。HMCL チームとは無関係で、サポートもありません。GPL-3.0 ライセンスです。
