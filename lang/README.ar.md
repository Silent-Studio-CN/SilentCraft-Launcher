<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher هو فرع طرف ثالث من [HMCL](https://github.com/HMCL-dev/HMCL) موجه للاعبين الناطقين بالصينية، ويركز على **تنزيل الخوادم وإدارتها**.

> هذا المشروع ليس إصدارًا رسميًا من HMCL وليس تابعًا لفريق HMCL. لا يقدم HMCL دعمًا لهذا الفرع.
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## إضافات مقارنة بالمشروع الأصلي

- تنزيل الخوادم: خادم vanilla وقائمة إصدارات Paper
- صفحة تثبيت Paper: إضافات مثبتة مسبقًا، التشغيل بعد التنزيل، مجلد خادم مستقل
- متصفح إضافات الخادم: مصدرا Modrinth / Hangar قابلان للاختيار
- قائمة الخوادم: إدارة مجلدات متعددة، فحص محتوى jar، تحرير الإعدادات
- محرر server.properties داخل التطبيق
- إصلاح حركة النافذة الملتصقة بالأعلى (macOS)

## البناء

يتطلب JDK 21:

    ./gradlew :HMCL:shadowJar

الناتج في HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar.

معرف تطبيق Microsoft OAuth مخصص:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## المساهمون

### HMCL الأصلي

139 مساهمًا بترتيب مستودع HMCL:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## الترخيص والنسبة

يستخدم هذا المشروع **GPL-3.0** ويحتفظ بحقوق النشر والنسبة الخاصة بـ HMCL. راجع LICENSE ورؤوس الملفات.

عند توزيع نسخة معدلة:

- احتفظ بإشعارات حقوق نشر HMCL؛
- استخدم اسمًا/إصدارًا مختلفًا لتمييزها عن النسخة الرسمية؛
- لا تدّعِ أنها نسخة رسمية من HMCL.

## التعليقات

أرسل المشكلات أو الاقتراحات أو الاستفسارات إلى [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback).

## الروابط

- المستودع：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- التعليقات：<https://github.com/SilentCodeTeams/feedback>
- الأصلي：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher فرع مجتمعي من HMCL يركز على تنزيل الخوادم وإدارتها. ليس تابعًا لفريق HMCL ولا يدعمه. مرخص بموجب GPL-3.0.
