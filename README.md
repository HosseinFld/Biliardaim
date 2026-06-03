# 🎱 Billiard Aim Pro

An Android overlay app that draws aim lines and bounce predictions on top of any billiard game — no root required.

---

## ✨ Features

- **Draw over any app** — works on top of 8 Ball Pool and similar games
- **Magnifier loupe** — hold your finger on the cue ball to precisely select its center (3× zoom with crosshair)
- **Smart bounce calculation** — up to 4 bounces calculated from real table borders, not screen edges
- **Table calibration** — tap the 4 corners of the table once; saved permanently across sessions
- **Pause / Resume** — disable touch passthrough with one tap so you can play your shot, then resume
- **Clear aim** — reset the current line instantly
- **5 line colors** — cycle through green, red, blue, yellow, pink
- **Draggable control panel** — move the floating buttons anywhere on screen

---

## 📱 How to Use

### First time only — calibrate the table
1. Open the app and tap **START OVERLAY**
2. Grant the *Display over other apps* permission
3. Open your billiard game
4. Tap the 🟠 **Calibrate** button on the floating panel
5. Tap the **4 corners** of the table in order: Top-Left → Top-Right → Bottom-Right → Bottom-Left
6. The green dashed border confirms calibration (saved for next time)

### Every game
1. **Hold your finger** on the cue ball — the magnifier loupe appears above your finger
2. Slide to center the crosshair on the ball, then **lift your finger** to confirm
3. **Drag** in the direction you want to aim — the bounce line updates in real time
4. Tap **⏸ Pause** — all touches pass through to the game so you can play your shot
5. Tap **▶ Resume** — the overlay is active again for your next shot
6. Tap 🟡 **Clear** to reset the aim line

---

## 🔨 Build from Source

### Requirements
- Android Studio Hedgehog or newer
- JDK 17+
- Android SDK 26+

### Steps
```bash
git clone https://github.com/YOUR_USERNAME/BilliardAim.git
cd BilliardAim
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Build via GitHub Actions (no Android Studio needed)
1. Fork or push this repo to GitHub
2. Go to **Actions → Build APK → Run workflow**
3. Wait ~3 minutes for the green checkmark ✅
4. Download the APK from **Artifacts → BilliardAim-APK**

---

## 📂 Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/billardaim/
│   ├── MainActivity.java        ← launcher screen & permission request
│   ├── OverlayService.java      ← foreground service managing the overlay
│   ├── AimView.java             ← canvas: loupe, aim lines, bounces
│   └── TableCalibration.java   ← 4-corner table model & ray-border math
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   └── overlay_controls.xml
    ├── drawable/
    │   ├── control_bg.xml
    │   └── btn_bg.xml
    └── values/styles.xml
```

---

## ⚙️ Permissions

| Permission | Why |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Draw the overlay on top of other apps |
| `FOREGROUND_SERVICE` | Keep the overlay alive while you play |

---

## 📋 Control Panel Reference

| Button | Color | Action |
|---|---|---|
| ⏸ / ▶ | Grey / Green | Pause or resume overlay touch |
| 🗑 | Yellow | Clear current aim line |
| 📐 | Orange | Re-calibrate table corners |
| ✏️ | Green | Cycle line color |
| ✕ | Red | Close overlay |

---

## 🛠 Tech Stack

- Pure Java (no Kotlin, no external libraries beyond AndroidX AppCompat)
- Custom `View` with hardware-accelerated `Canvas` drawing
- `WindowManager` overlay with `TYPE_APPLICATION_OVERLAY`
- `SharedPreferences` for persistent table calibration
- Ray–segment intersection math for accurate wall bounces

---

## 📄 License

MIT — free to use, modify, and distribute.

---
---
---

# 🎱 Billiard Aim Pro — راهنمای فارسی

یک اپ Android که روی هر بازی بیلیارد، خط aim و bounce را نمایش می‌دهد — بدون نیاز به root.

---

## ✨ قابلیت‌ها

- **Overlay روی هر اپ** — روی 8 Ball Pool و بازی‌های مشابه کار می‌کند
- **ذره‌بین (Loupe)** — انگشت را روی توپ سفید نگه دارید تا مرکز آن را با دقت ۳× انتخاب کنید
- **محاسبه bounce هوشمند** — تا ۴ بازتاب از لبه‌های واقعی میز، نه لبه صفحه نمایش
- **کالیبراسیون میز** — یک بار چهار گوشه میز را tap کنید؛ برای همیشه ذخیره می‌شود
- **Pause / Resume** — با یک دکمه لمس‌ها به بازی منتقل می‌شود تا بتوانید ضربه بزنید
- **پاک کردن aim** — خط فعلی را فوری پاک کنید
- **۵ رنگ خط** — بین سبز، قرمز، آبی، زرد و صورتی جابجا شوید
- **پانل کنترل شناور** — دکمه‌های شناور را هرجای صفحه بکشید

---

## 📱 نحوه استفاده

### اول بار — کالیبراسیون میز (یک بار کافی است)
1. اپ را باز کنید و **START OVERLAY** را بزنید
2. اجازه *نمایش روی اپ‌های دیگر* را بدهید
3. بازی بیلیارد را باز کنید
4. دکمه 🟠 **Calibrate** را بزنید
5. **چهار گوشه** میز را به ترتیب tap کنید: بالا-چپ ← بالا-راست ← پایین-راست ← پایین-چپ
6. خط سبز نقطه‌چین تأیید کالیبراسیون را نشان می‌دهد (ذخیره می‌شود)

### هر بار بازی
1. **انگشت را روی توپ سفید نگه دارید** — ذره‌بین بالای انگشت ظاهر می‌شود
2. انگشت را بلغزانید تا crosshair وسط توپ باشد، سپس **انگشت را بردارید** تا تأیید شود
3. **بکشید** در جهت هدف — خط bounce به‌صورت زنده آپدیت می‌شود
4. **⏸ Pause** را بزنید — حالا می‌توانید ضربه بزنید
5. **▶ Resume** را بزنید — برای ضربه بعدی آماده است
6. 🟡 **Clear** را بزنید تا خط پاک شود

---

## 🔨 ساخت از سورس

### نیازمندی‌ها
- Android Studio Hedgehog یا جدیدتر
- JDK 17+
- Android SDK 26+

### دستورات
```bash
git clone https://github.com/YOUR_USERNAME/BilliardAim.git
cd BilliardAim
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### ساخت با GitHub Actions (بدون Android Studio)
1. این repo را Fork یا push کنید
2. به **Actions → Build APK → Run workflow** بروید
3. حدود ۳ دقیقه صبر کنید تا تیک سبز ✅ بیاید
4. APK را از **Artifacts → BilliardAim-APK** دانلود کنید

---

## 📋 راهنمای دکمه‌های پانل

| دکمه | رنگ | عملکرد |
|---|---|---|
| ⏸ / ▶ | خاکستری / سبز | Pause یا Resume کردن overlay |
| 🗑 | زرد | پاک کردن خط aim فعلی |
| 📐 | نارنجی | کالیبراسیون مجدد میز |
| ✏️ | سبز | تغییر رنگ خط |
| ✕ | قرمز | بستن overlay |

---

## ⚠️ مجوزها

| مجوز | دلیل |
|---|---|
| `SYSTEM_ALERT_WINDOW` | نمایش overlay روی اپ‌های دیگر |
| `FOREGROUND_SERVICE` | فعال ماندن overlay حین بازی |
