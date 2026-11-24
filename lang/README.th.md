<div align="center">

简体中文 · [English](README.en.md) · [Español](README.es.md) · [Français](README.fr.md) · [Deutsch](README.de.md) · [Русский](README.ru.md) · [日本語](README.ja.md) · [한국어](README.ko.md) · [العربية](README.ar.md) · [Português](README.pt.md) · [Italiano](README.it.md) · [हिन्दी](README.hi.md) · [ไทย](README.th.md) · [Tiếng Việt](README.vi.md) · [Bahasa Indonesia](README.id.md) · [Türkçe](README.tr.md) · [Nederlands](README.nl.md)

</div>

# SilentCraft Launcher

SilentCraft Launcher เป็นฟอร์กจากบุคคลที่สามของ [HMCL](https://github.com/HMCL-dev/HMCL) สำหรับผู้เล่นภาษาจีน เน้น **การดาวน์โหลดและจัดการเซิร์ฟเวอร์**

> โปรเจกต์นี้ไม่ใช่เวอร์ชันทางการของ HMCL และไม่เกี่ยวข้องกับทีม HMCL HMCL ไม่ให้การสนับสนุนฟอร์กนี้
>
> 
<img width="2048" height="713" alt="model" src="https://github.com/user-attachments/assets/fe7023e8-d896-4c49-b601-814da84448ca" />


## สิ่งที่เพิ่มจากต้นน้ำ

- ดาวน์โหลดเซิร์ฟเวอร์: เซิร์ฟเวอร์ vanilla และรายการบิลด์ Paper
- หน้าติดตั้ง Paper: ติดตั้งปลั๊กอินล่วงหน้า เริ่มหลังดาวน์โหลด โฟลเดอร์เซิร์ฟเวอร์แยก
- เบราว์เซอร์ปลั๊กอินเซิร์ฟเวอร์: แหล่ง Modrinth / Hangar เลือกได้
- รายการเซิร์ฟเวอร์: จัดการหลายโฟลเดอร์ ตรวจสอบ jar แก้ไขคอนฟิก
- ตัวแก้ไข server.properties ในแอป
- แก้ไขแอนิเมชันหน้าต่างชิดขอบบน (macOS)

## การสร้าง

ต้องใช้ JDK 21:

    ./gradlew :HMCL:shadowJar

ไฟล์ผลลัพธ์อยู่ที่ HMCL/build/libs/HMCL-3.18.SNAPSHOT.jar

ID แอป Microsoft OAuth ที่กำหนดเอง:

    MICROSOFT_AUTH_ID=<your-client-id> ./gradlew :HMCL:shadowJar

## ผู้มีส่วนร่วม

### HMCL ต้นน้ำ

139 คน ตามลำดับในรีโป HMCL:

[![Contributors](https://contrib.rocks/image?repo=HMCL-dev/HMCL&max=200)](https://github.com/HMCL-dev/HMCL/graphs/contributors)
### SilentCraft

- @Silent-xiaomiao

## สัญญาอนุญาตและเครดิต

โปรเจกต์นี้ใช้ **GPL-3.0** และคงลิขสิทธิ์และเครดิตของ HMCL ดู LICENSE และส่วนหัวซอร์สโค้ด

เมื่อเผยแพร่เวอร์ชันดัดแปลง:

- คงประกาศลิขสิทธิ์ของ HMCL;
- ใช้ชื่อ/เวอร์ชันที่แตกต่างเพื่อแยกจากบิลด์ทางการ;
- อย่าอ้างว่าเป็นบิลด์ทางการของ HMCL

## ข้อเสนอแนะ

ส่งปัญหา ข้อเสนอแนะ หรือคำถามได้ที่ [SilentCodeTeams/feedback](https://github.com/SilentCodeTeams/feedback)

## ลิงก์

- รีโป：<https://github.com/Silent-Studio-CN/SilentCraft-Launcher>
- ข้อเสนอแนะ：<https://github.com/SilentCodeTeams/feedback>
- ต้นน้ำ：<https://github.com/HMCL-dev/HMCL>

---

SilentCraft Launcher เป็นฟอร์กชุมชนของ HMCL ที่เน้นการดาวน์โหลดและจัดการเซิร์ฟเวอร์ ไม่เกี่ยวข้องและไม่ได้รับการสนับสนุนจากทีม HMCL ใช้สัญญา GPL-3.0
