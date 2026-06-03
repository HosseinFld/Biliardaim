# 🎱 Billiard Aim Pro

An Android overlay app that draws aim lines and bounce predictions on top of any billiard game — no root required.

---

## ✨ Features

- **Draw over any app** — works on top of 8 Ball Pool and similar games
- **Magnifier loupe** — hold your finger on the cue ball to precisely select its center (3× zoom with crosshair)
- **Bounce prediction** — up to 4 bounces calculated in real time
- **Pause / Resume** — disable touch with one tap so you can play your shot, then resume
- **Clear aim** — reset the current line instantly
- **5 line colors** — cycle through green, red, blue, yellow, pink
- **Draggable control panel** — move the floating buttons anywhere on screen

---

## 📱 How to Use

1. Open the app and tap **START OVERLAY**
2. Grant the *Display over other apps* permission
3. Open your billiard game
4. **Hold your finger** on the cue ball — the magnifier loupe appears above your finger
5. Slide to center the crosshair on the ball, then **lift your finger** to confirm
6. **Drag** in the direction you want to aim — the bounce line updates in real time
7. Tap **⏸ Pause** — all touches pass through to the game so you can play your shot
8. Tap **▶ Resume** — the overlay is active again for your next shot
9. Tap 🟡 **Clear** to reset the aim line

---

## 📋 Control Panel Reference

| Button | Color | Action |
|---|---|---|
| ⏸ / ▶ | Grey / Green | Pause or resume overlay touch |
| 🗑 | Yellow | Clear current aim line |
| ✏️ | Green | Cycle line color |
| ✕ | Red | Close overlay |

---

## ⚙️ Permissions

| Permission | Why |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Draw the overlay on top of other apps |
| `FOREGROUND_SERVICE` | Keep the overlay alive while you play |

---

## 🛠 Tech Stack

- Pure Java — no external libraries beyond AndroidX AppCompat
- Custom `View` with hardware-accelerated `Canvas` drawing
- `WindowManager` overlay with `TYPE_APPLICATION_OVERLAY`
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
- **پیش‌بینی bounce** — تا ۴ بازتاب به‌صورت زنده محاسبه می‌شود
- **Pause / Resume** — با یک دکمه لمس‌ها به بازی منتقل می‌شود تا بتوانید ضربه بزنید
- **پاک کردن aim** — خط فعلی را فوری پاک کنید
- **۵ رنگ خط** — بین سبز، قرمز، آبی، زرد و صورتی جابجا شوید
- **پانل کنترل شناور** — دکمه‌های شناور را هرجای صفحه بکشید

---

## 📱 نحوه استفاده

1. اپ را باز کنید و **START OVERLAY** را بزنید
2. اجازه *نمایش روی اپ‌های دیگر* را بدهید
3. بازی بیلیارد را باز کنید
4. **انگشت را روی توپ سفید نگه دارید** — ذره‌بین بالای انگشت ظاهر می‌شود
5. انگشت را بلغزانید تا crosshair وسط توپ باشد، سپس **انگشت را بردارید** تا تأیید شود
6. **بکشید** در جهت هدف — خط bounce به‌صورت زنده آپدیت می‌شود
7. **⏸ Pause** را بزنید — حالا می‌توانید ضربه بزنید
8. **▶ Resume** را بزنید — برای ضربه بعدی آماده است
9. 🟡 **Clear** را بزنید تا خط پاک شود

---

## 📋 راهنمای دکمه‌های پانل

| دکمه | رنگ | عملکرد |
|---|---|---|
| ⏸ / ▶ | خاکستری / سبز | Pause یا Resume کردن overlay |
| 🗑 | زرد | پاک کردن خط aim فعلی |
| ✏️ | سبز | تغییر رنگ خط |
| ✕ | قرمز | بستن overlay |

---

## ⚠️ مجوزها

| مجوز | دلیل |
|---|---|
| `SYSTEM_ALERT_WINDOW` | نمایش overlay روی اپ‌های دیگر |
| `FOREGROUND_SERVICE` | فعال ماندن overlay حین بازی |
