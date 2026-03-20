<div align="center">

# xSkill

**Paper · Config-driven item skills · Global level & XP**

[![Paper](https://img.shields.io/badge/Paper-1.21.4-004080?style=flat-square)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-21-ea2d2d?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/build-Maven-C71A2B?style=flat-square&logo=apachemaven&logoColor=white)](https://maven.apache.org/)

</div>

---

## Dil | Language

| 🇹🇷 Türkçe | 🇬🇧 English |
| :--- | :--- |
| **xSkill**, YAML ile tanımlanan eşya setlerine yetenekler, cooldown, seviye gereksinimi ve global XP/level sistemi ekler. Ana elde **herhangi bir item** tutarak `/give` ve `/set` ile tanım uygulayabilirsiniz (sadece kılıç değil). | **xSkill** adds abilities, cooldowns, level gates, and a global XP/level system to YAML-defined item sets. Use `/give` and `/set` on **any item** in your main hand—not only swords. |

---

## Özet kartı | At a glance

| Alan | Field | Değer | Value |
| :--- | :--- | :--- | :--- |
| Sunucu | Server | **Paper** | **Paper** |
| API | API | `1.21` | `1.21` |
| Test hedefi | Tested target | `1.21.4` | `1.21.4` |
| Dil sürümü | Language level | **Java 21** | **Java 21** |
| Yapı | Build | **Maven** | **Maven** |
| Çıktı | Output | `target/xSkill-1.0.0.jar` | `target/xSkill-1.0.0.jar` |
| Opsiyonel | Optional | **PlaceholderAPI** (yer tutucular) | **PlaceholderAPI** (placeholders) |

---

## Özellikler | Features

| # | 🇹🇷 Türkçe | 🇬🇧 English |
|:-:|:---|:---|
| 1 | YAML altında `swords:` ile çoklu **eşya tanımı** (id, malzeme, isim, lore, CMD) | Multiple **item definitions** under `swords:` (id, material, name, lore, custom model data) |
| 2 | Sağ tık / shift+sağ tık ile tetiklenen **yetenekler**, sunucu genelinde **cooldown** | **Abilities** triggered by right-click / shift+right-click, per-player **cooldowns** |
| 3 | **Global seviye & XP**: vuruş, öldürme, yetenek kullanımı ile XP; seviyeye göre ölçekleme | **Global level & XP** from hits, kills, ability use; **scaling** by level |
| 4 | Dünya bazlı devre dışı, isteğe bağlı **combat tag** (yetenek kilidi) | Per-world **disabled worlds**, optional **combat tag** (ability lock) |
| 5 | **PAPI**: yüklüyse yer tutucular kaydolur | **PlaceholderAPI** registers when present |
| 6 | Eldeki iteme **tam uygulama** (`give <id>`) veya sadece **kimlik** (`set <id>`) | **Full apply** (`give <id>`) or **ID-only** (`set <id>`) on held item |

---

## Gereksinimler | Requirements

| Bileşen | Component | Not | Note |
| :--- | :--- | :--- | :--- |
| Sunucu | Server | Paper (veya Paper türevi) | Paper (or fork) recommended |
| Sürüm | Version | **1.21.x** (API `1.21`) | **1.21.x** (API `1.21`) |
| Java | Java | **21+** | **21+** |

---

## Kurulum | Installation

| Adım | Step | 🇹🇷 Türkçe | 🇬🇧 English |
|:-:|:---|:---|:---|
| 1 | Jar | `target/xSkill-1.0.0.jar` dosyasını sunucunun `plugins/` klasörüne kopyalayın | Copy `target/xSkill-1.0.0.jar` into the server `plugins/` folder |
| 2 | İlk çalıştırma | Sunucuyu başlatın; `plugins/xSkill/config.yml` oluşur | Start once; `plugins/xSkill/config.yml` is created |
| 3 | Yapılandırma | `config.yml` içinde `swords:` altına kendi id’lerinizi ekleyin veya örnekleri düzenleyin | Edit entries under `swords:` or add your own IDs |
| 4 | Yenileme | Oyunda `/xskill reload` veya yeniden başlatma | Run `/xskill reload` or restart |

---

## Derleme | Build

```bash
mvn -DskipTests package
```

| Çıktı | Output | Yol | Path |
| :--- | :--- | :--- | :--- |
| JAR | JAR | `target/xSkill-1.0.0.jar` | `target/xSkill-1.0.0.jar` |

---

## Komutlar | Commands

| Komut | Command | 🇹🇷 Açıklama | 🇬🇧 Description |
| :--- | :--- | :--- | :--- |
| `/xskill give <id>` | same | Ana eldeki **herhangi bir iteme** config’teki tanımı **tam** uygular (isim, lore, model, PDC) | **Fully** applies the definition to the **held item** (name, lore, model, PDC) |
| `/xskill set <id>` | same | Görünümü koruyarak sadece **tanım kimliği** (PDC) yazar | Writes **definition ID** (PDC) only; keeps appearance |
| `/xskill give <player> <id> [amount]` | same | Oyuncuya yapılandırmaya göre **yeni item** verir (adet üst sınırı: 1024) | Gives **new items** to a player per config (amount cap: **1024**) |
| `/xskill reload` | same | `config.yml` yeniden yüklenir | Reloads `config.yml` |
| `/xskill stats [player]` | same | Seviye / XP istatistikleri | Level / XP **stats** |
| `/xskill level [player]` | same | `stats` ile aynı | Alias of **stats** |

---

## Yetkiler | Permissions

| Yetki | Permission | Varsayılan | Default | 🇹🇷 | 🇬🇧 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `xskill.admin` | `xskill.admin` | OP | OP | Alt yetkileri kapsar | Bundles child perms |
| `xskill.give` | `xskill.give` | OP | OP | give (el + oyuncu) | give (hand + player) |
| `xskill.set` | `xskill.set` | OP | OP | set | set |
| `xskill.reload` | `xskill.reload` | OP | OP | reload | reload |
| `xskill.stats.others` | `xskill.stats.others` | OP | OP | Başka oyuncunun stats’ı | Other players’ stats |

---

## Yapılandırma | Configuration

| Dosya | File | 🇹🇷 | 🇬🇧 |
| :--- | :--- | :--- | :--- |
| Ana config | Main | `plugins/xSkill/config.yml` | `plugins/xSkill/config.yml` |
| Varsayılan kaynak | Default source | `src/main/resources/config.yml` (jar içi) | `src/main/resources/config.yml` (in JAR) |

### Seviye sistemi | Leveling (`leveling:`)

| Anahtar | Key | 🇹🇷 | 🇬🇧 |
| :--- | :--- | :--- | :--- |
| `enabled` | | Açık/kapalı | On/off |
| `maxLevel` | | Üst seviye sınırı | Max level |
| `xp.baseToNext` / `perLevelAdd` | | Seviye başına XP eğrisi | XP curve per level |
| `sources.hit` / `kill` / `abilityUse` | | XP kaynakları | XP sources |
| `scaling.*` | | Cooldown, yarıçap, hasar vb. seviye ölçeklemesi | Per-level scaling (cooldown, radius, damage, …) |

### Global ayarlar | Global (`swords.settings:`)

| Anahtar | Key | 🇹🇷 | 🇬🇧 |
| :--- | :--- | :--- | :--- |
| `disabledWorlds` | | Bu dünyalarda etkileşim yok | No abilities in listed worlds |
| `combatTag` | | Etiket süresi, yetenek engeli | Tag duration, optional ability block |

### Eşya tanımı | Item definition (`swords.<id>:`)

| Alan | Field | 🇹🇷 | 🇬🇧 |
| :--- | :--- | :--- | :--- |
| `enabled` | | Tanım aktif mi | Definition active |
| `material` | | Varsayılan üretim malzemesi | Default material when **giving** items |
| `name` / `lore` / `customModelData` | | Görünüm | Display |
| `requirements.minLevel` | | Minimum oyuncu seviyesi | Min **player** level |
| `combat.damageBonus` | | Ek hasar (vuruşta) | Bonus damage on hit |
| `abilities.<abilityId>` | | Tetikleyici, tip, süre, yarıçap, partikül, ses | Trigger, type, duration, radius, particles, sound |

---

## Yetenek tipleri (örnek) | Ability types (examples)

| Tip | Type | 🇹🇷 Kısa açıklama | 🇬🇧 Short description |
| :--- | :--- | :--- | :--- |
| `AOE_FREEZE` | | Alan dondurma / yavaşlatma | Area freeze / slow |
| `AOE_POTION` | | Alan iksiri | Area potion cloud |
| `AOE_DAMAGE` / `CONE_DAMAGE` | | Alan / koni hasarı | AoE / cone damage |
| `PULL_PUSH` | | Çek / it | Pull / push |
| `DOT` | | Süreli hasar | Damage over time |
| `LIFESTEAL` | | Can çalma | Lifesteal |
| `SHIELD` | | Kalkan | Shield |
| `RAY` / `CHAIN_LIGHTNING` | | Işın / zincir şimşek | Ray / chain lightning |
| `SELF_POTION` / `SELF_HEAL` | | Kendine iksir / iyileşme | Self buff / heal |
| `BLINK` | | Işınlanma | Blink |
| `AOE_KNOCKUP` / `AOE_LIGHTNING` | | Savurma / yıldırım | Knock-up / lightning |
| `VFX_*` | | Halka, dalga, sarmal, sütun, cast bar | Ring, wave, spiral, column, cast bar |

> Tam liste ve alanlar için `AbilityExecutor.java` ve örnek `config.yml` bölümlerine bakın.  
> See `AbilityExecutor.java` and sample `config.yml` sections for the full list and fields.

---

## Sorun giderme | Troubleshooting

| Sorun | Issue | 🇹🇷 Olası çözüm | 🇬🇧 Possible fix |
| :--- | :--- | :--- | :--- |
| Yetenek çıkmıyor | No ability | Dünya `disabledWorlds` içinde mi, seviye yeterli mi, cooldown var mı | Check **disabled worlds**, **min level**, **cooldown** |
| PAPI yok | No placeholders | PlaceholderAPI kurulu ve aktif mi | Install & enable **PlaceholderAPI** |
| Config yüklenmedi | Config not applied | `/xskill reload` veya yeniden başlat | `/xskill reload` or **restart** |

---

## Katkı & özel lisans | Contributing & custom license

| | 🇹🇷 Türkçe | 🇬🇧 English |
| :--- | :--- | :--- |
| **Lisans dosyası** | Tüm haklar ve koşullar için depodaki **[`LICENSE`](LICENSE)** dosyasına bakın (**xSkill Custom License v1.0**). | See **[`LICENSE`](LICENSE)** in this repo (**xSkill Custom License v1.0**). |
| **Özet** | Derlenmiş **resmi JAR**’ı sunucunuzda çalıştırabilirsiniz; kaynak şeffaflık ve katkı içindir. Ticari yeniden satış, resmi dağıtımı taklit eden fork veya izinsiz yeniden dağıtım **yasaktır**. Katkılar (PR) birleştirme ve dağıtım için projeye lisans verir. | You may run the **official built JAR** on your server; source is for transparency and contributions. **No** commercial resale as a standalone product, **no** misleading forks as “official”, **no** redistribution without permission. **PRs** grant the project rights to merge and ship your changes. |
| **Katkı** | İyileştirme önerileri ve PR’lar GitHub üzerinden; gönderdiğiniz içerik `LICENSE` §5 kapsamındadır. | Improvements and **pull requests** via GitHub; submissions are under **§5** of `LICENSE`. |
| **Garanti** | Yazılım “olduğu gibi” sunulur; garanti verilmez (ayrıntı `LICENSE` §4). | Provided **as-is**; no warranty (see **§4**). |

> **Not:** Bu tablo hukuki özet niteliğindedir; anlaşmazlıkta bağlayıcı metin İngilizce **`LICENSE`** dosyasıdır.  
> **Note:** This table is a summary; the binding text is the English **`LICENSE`** file.

---

<div align="center">

**xSkill** · *Paper plugin · YAML-driven item skills*

</div>
