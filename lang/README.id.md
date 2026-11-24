<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher adalah fork pihak ketiga dari [HMCL](https://github.com/HMCL-dev/HMCL) untuk pemain berbahasa Tionghoa, berfokus pada **unduhan dan pengelolaan server**.

> Proyek ini bukan versi resmi HMCL dan tidak berafiliasi dengan tim HMCL. HMCL tidak memberikan dukungan untuk fork ini.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## Baru dibanding upstream

- Unduhan server: server vanilla dan daftar build Paper
- Halaman instalasi Paper: plugin terpasang sebelumnya, mulai setelah unduh, direktori server terpisah
- Peramban plugin server: sumber Modrinth / Hangar dapat dipilih
- Daftar server: pengelolaan multi-direktori, pemeriksaan jar, edit konfigurasi
- Editor server.properties dalam aplikasi
- Perbaikan animasi jendela menempel atas (macOS)

## Build

Membutuhkan JDK 21:

    ./gradlew :HMCL:shadowJar

Artefak ada di HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar.

ID aplikasi Microsoft OAuth khusus:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## Kontributor

### HMCL upstream

139 kontributor, sesuai urutan repositori HMCL:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## Lisensi dan atribusi

Proyek ini memakai **GPL-3.0** dan mempertahankan hak cipta serta atribusi HMCL. Lihat LICENSE dan header sumber.

Saat mendistribusikan versi modifikasi:

- Pertahankan pemberitahuan hak cipta HMCL;
- Gunakan nama/versi berbeda untuk membedakan dari build resmi;
- Jangan mengklaim sebagai build resmi HMCL.

## Umpan balik

Kirim masalah, saran, atau pertanyaan di [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback).

## Tautan

- Repositori：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- Umpan balik：<https://github.com/SilentCodeTeams/feedback>
- Upstream：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher adalah fork komunitas HMCL yang berfokus pada unduhan dan pengelolaan server. Tidak berafiliasi atau didukung tim HMCL. Lisensi GPL-3.0.
