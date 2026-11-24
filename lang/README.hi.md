<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher, चीनी खिलाड़ियों के लिए [HMCL](https://github.com/HMCL-dev/HMCL) का तृतीय-पक्ष फोर्क है, जो **सर्वर डाउनलोड और प्रबंधन** पर केंद्रित है।

> यह परियोजना HMCL का आधिकारिक संस्करण नहीं है और HMCL टीम से संबद्ध नहीं है। HMCL इस फोर्क का समर्थन नहीं करता।
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## अपस्ट्रीम की तुलना में नया

- सर्वर डाउनलोड: वनीला सर्वर और Paper बिल्ड सूची
- Paper इंस्टॉल पेज: पूर्व-स्थापित प्लगइन, डाउनलोड के बाद लॉन्च, अलग सर्वर निर्देशिका
- सर्वर प्लगइन ब्राउज़र: Modrinth / Hangar दो स्रोत चुनने योग्य
- सर्वर सूची: मल्टी-डायरेक्टरी प्रबंधन, jar निरीक्षण, कॉन्फ़िग संपादन
- ऐप में server.properties संपादक
- विंडो टॉप-फ्लश एनिमेशन सुधार (macOS)

## बिल्ड

JDK 21 आवश्यक:

    ./gradlew :HMCL:shadowJar

आर्टिफ़ैक्ट HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar में है।

कस्टम Microsoft OAuth ऐप ID:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## योगदानकर्ता

### HMCL अपस्ट्रीम

HMCL रिपॉज़िटरी क्रम में 139 योगदानकर्ता:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## लाइसेंस और श्रेय

यह परियोजना **GPL-3.0** के अंतर्गत है और HMCL के कॉपीराइट व श्रेय को बनाए रखती है। विवरण LICENSE और स्रोत हेडर में देखें।

संशोधित संस्करण वितरित करते समय:

- HMCL के कॉपीराइट नोटिस रखें;
- आधिकारिक बिल्ड से अलग पहचान के लिए भिन्न नाम/संस्करण रखें;
- HMCL का आधिकारिक बिल्ड होने का दावा न करें।

## प्रतिक्रिया

समस्याएँ, सुझाव या प्रश्न [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback) पर भेजें।

## लिंक

- रिपॉज़िटरी：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- प्रतिक्रिया：<https://github.com/SilentCodeTeams/feedback>
- अपस्ट्रीम：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher, सर्वर डाउनलोड और प्रबंधन पर केंद्रित HMCL का सामुदायिक फोर्क है। यह HMCL टीम से संबद्ध या समर्थित नहीं है। GPL-3.0 के अंतर्गत लाइसेंस प्राप्त।
