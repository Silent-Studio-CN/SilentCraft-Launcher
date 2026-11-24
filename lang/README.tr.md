<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher, Çince oyuncular için [HMCL](https://github.com/HMCL-dev/HMCL) tabanlı, **sunucu indirme ve yönetimine** odaklanan bir üçüncü taraf çataldır.

> Bu proje resmî bir HMCL sürümü değildir ve HMCL ekibiyle bağlantılı değildir. HMCL bu çatala destek vermez.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## Upstream’e göre yenilikler

- Sunucu indirme: vanilla sunucu ve Paper yapı listesi
- Paper kurulum sayfası: önceden kurulu eklentiler, indirdikten sonra başlatma, ayrı sunucu dizini
- Sunucu eklenti tarayıcısı: Modrinth / Hangar iki kaynak seçilebilir
- Sunucu listesi: çoklu dizin yönetimi, jar inceleme, yapılandırma düzenleme
- Uygulama içi server.properties düzenleyici
- Pencere üst kenara yapışma animasyonu düzeltmesi (macOS)

## Derleme

JDK 21 gerekir:

    ./gradlew :HMCL:shadowJar

Çıktı HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar konumunda.

Özel Microsoft OAuth Uygulama Kimliği:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## Katkıda bulunanlar

### HMCL upstream

HMCL deposu sırasıyla 139 katkıda bulunan:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## Lisans ve atıf

Bu proje **GPL-3.0** altındadır ve HMCL telif hakkı ile atfını korur. Ayrıntılar için LICENSE ve kaynak başlıklarına bakın.

Değiştirilmiş sürümü dağıtırken:

- HMCL telif bildirimlerini koruyun;
- Resmî yapıdan ayırmak için farklı yazılım adı/sürümü kullanın;
- HMCL’nin resmî yapısı olduğunu iddia etmeyin.

## Geri bildirim

Sorun, öneri veya sorularınızı [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback) adresine gönderin.

## Bağlantılar

- Depo：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- Geri bildirim：<https://github.com/SilentCodeTeams/feedback>
- Upstream：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher, sunucu indirme ve yönetimine odaklanan bir HMCL topluluk çatalıdır. HMCL ekibiyle bağlantılı değildir ve desteklenmez. GPL-3.0 lisanslıdır.
