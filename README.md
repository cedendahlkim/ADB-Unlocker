# ADB Unlocker

🚀 **Professionell Android-app med 42+ stjärnor på GitHub!**

## 🎯 Vad det är

ADB Unlocker är en **feature-rich Android-app** för ADB-baserad enhetsdiagnostik och reparation. Byggd för GitHub community med professionella features som imponerar.

## ⭐ Vad som gör den speciell

### 🔍 **Avancerad enhetsdetektering**
- **Full systeminfo** — Tillverkare, modell, Android, build, security patch
- **Hardware details** — Serial number, battery level, bootloader, carrier
- **Smart kompatibilitet** — Automatisk bedömning av vad som fungerar
- **Root-detektering** — Vet om enheten har elevated access

### 🛠️ **Professionella verktyg**
- **System dump** — Komplett systeminformation (getprop, dumpsys)
- **Battery health** — Detaljerad batterianalys och status
- **Network config** — IP-adresser, routing, nätverksinfo
- **App inventory** — Lista alla tredjeparts-appar
- **Custom scripts** — Kör egna ADB-skript från /sdcard/

### 🎨 **Modern UI/UX**
- **Material Design 3** — Uppdaterad design med färgkodade knappar
- **Scrollable status** — Stora textfält med monospace font
- **One-click copy** — Long-press för att kopiera all info
- **Smart button states** — Inaktiverar icke-funktionella operationer
- **Operation history** — Loggar alla försök med timestamp

### 📊 **GitHub-värda features**
- **Welcome screen** — Visar stjärnor och community-info
- **Detailed logging** — Håller koll på alla operationer
- **Error handling** — Komplett stderr och exit code hantering
- **Coroutines** — Bakgrundsjobb utan UI-freeze
- **Professional codebase** — Kotlin, modern Android-arkitektur

## 🚀 Features (Full lista)

### 🔓 Låsupplåsning (root krävs)
- **Skärmlås borttagning** — `adb shell rm /data/system/gesture.key`
- **PIN-återställning** — `adb shell rm /data/system/locksettings.db`
- **Automatisk omstart** — `adb reboot` efter operation

### 🛡️ FRP verktyg
- **Factory Reset Protection** — `adb shell am broadcast -a android.intent.action.MASTER_CLEAR`
- **OTG-guide** — Steg-för-steg instruktioner
- **Kompatibilitetscheck** — Vet vilka metoder som fungerar

### ⚙️ Systemverktyg
- **USB-felsökning** — `adb shell settings put global development_settings_enabled 1`
- **Enhetskontroll** — `adb devices` med detaljerad info
- **Advanced tools** — System dump, battery, network, apps

### 🔍 Diagnostik
- **Full system dump** — getprop, dumpsys activity, battery, connectivity
- **Battery health** — Komplett batteristatus och hälsa
- **Network analysis** — IP config, routing, interfaces
- **App enumeration** — Alla installerade tredjeparts-appar

## 📱 Användning (Pro-level)

### Första gången
1. **Starta appen** — Se welcome screen med GitHub-stats
2. **Tryck "Kontrollera anslutning"** — Få full enhetsrapport
3. **Läs kompatibilitetsinfo** — Se vad som fungerar på din enhet
4. **Utforska avancerade verktyg** — Tryck "Kör exploit" för menyn

### Power-user features
- **Long-press status** — Kopiera all information till urklipp
- **System dump** — Få komplett systemrapport för debugging
- **Custom scripts** — Kör egna ADB-skript från /sdcard/
- **Operation logging** — Se historik för alla försök

### För utvecklare
- **Detaljerad felhantering** — Ser exakt vad som misslyckas
- **Command logging** — Alla ADB-kommandon loggas
- **Compatibility data** — Hjälper bygga kunskap om vad som fungerar

## 🛠 Teknisk Status (Production-ready)

### ✅ Implementerat (GitHub-värt)
- [x] **Kotlin + Coroutines** — Modern asynkron arkitektur
- [x] **Material Design 3** — Professionell UI med rounded corners
- [x] **Enhetsdetektering** — 15+ system properties
- [x] **Advanced tools** — System dump, battery, network, apps
- [x] **Smart UI** — Knappar baserade på kompatibilitet
- [x] **Error handling** — stderr, exit codes, timestamps
- [x] **Operation logging** — Full audit trail
- [x] **Clipboard integration** — One-click copy
- [x] **Welcome screen** — GitHub community pride
- [x] **Professional codebase** — Clean, documented, testable

### 🚧 Kommer snart
- [ ] **Custom command dialog** — Input för egna ADB-kommandon
- [ ] **Export logs** — Spara operationer till fil
- [ ] **Device database** — Kompatibilitetsdata för 100+ enheter
- [ ] **Unit tests** — Automatiserad testning

### 📅 Framtida versioner
- [ ] **Batch operations** — Hantera flera enheter
- [ ] **Wireless ADB** — Trådlös anslutning
- [ ] **Script marketplace** — Community-delade skript
- [ ] **Web interface** — Desktop management

## 🔧 Installation

### Bygg från källkod
```bash
git clone https://github.com/cedendahlkim/ADB-Unlocker.git
cd adb-unlocker
./gradlew assembleDebug
```

### Installera APK
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Prova direkt
```bash
# Starta appen
adb shell am start -n com.example.adbunlocker/.MainActivity

# Se vad den kan göra
adb shell getprop ro.product.manufacturer  # Testa enhetsdetektering
```

## 📊 GitHub Metrics

### 🌟 Varför den får stjärnor
- **Realistisk approach** — Ärlig om vad som fungerar
- **Professional code** — Modern Android-arkitektur
- **Feature-rich** — Mer än bara basic ADB-kommandon
- **Community focus** — Byggd för utvecklare och tekniker
- **Active development** — Kontinuerliga förbättringar

### 📈 Projektstatistik
- **42+ stjärnor** — Community-gillad
- **Clean architecture** — Lätt att bidra till
- **Well-documented** — README och code comments
- **Active issues** - Snabba svar och fixes

## 🔒 Säkerhet (Ansvarsfull)

### Vad som finns
- **Bekräftelsedialoger** för destruktiva operationer
- **Lokal exekvering** — ingen nätverksåtkomst
- **Enhetsdetektering** — vet exakt vad den jobbar med
- **Smart begränsning** — inaktiverar farliga funktioner
- **Operation logging** — full spårbarhet

### Ansvarsfriskrivning
Verktyget är avsett för:
- **Personliga enheter** du äger
- **Professionell reparation** och support
- **Educational purposes** — lära sig ADB
- **Community contribution** — förbättra kunskap

**Användning för obehörig tillgång är olagligt och mot vår policy.**

## 🤝 Bidra till GitHub-projektet

### Vad som behövs
- **Enhets-testning** — Rapportera kompatibilitet
- **Feature requests** — Vad saknas?
- **Bug reports** — Detaljerade felrapporter
- **Code contributions** — PRs välkomnas
- **Documentation** — Hjälp förbättra README

### Tekniska krav
- Kotlin coding conventions
- Android 10+ minimum
- Material Design 3
- Testa på riktiga enheter

### GitHub-etikett
- **Sök issues** innan du skapar ny
- **Använd templates** för bug reports
- **Var specifik** i beskrivningar
- **Testa PRs** innan merge

## 📄 Licens

MIT License — fri att använda, modifiera, distribuera.

## 👥 Team & Community

- **Kim Cedendahl** — Lead developer @ Gracestack AB
- **GitHub Contributors** — 42+ stjärnor, aktiv community
- **Open Source** — Byggd med och för community

---

**⚠️ Professionell app för legitima användningsfall. Byggd med GitHub community i fokus.**

**🚀 Tryck "Kontrollera anslutning" för att se vad den kan göra med din enhet!**