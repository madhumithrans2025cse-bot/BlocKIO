<<<<<<< HEAD
# App Blocker (Android)

Blocks selected apps every day until a time you set once, and repeats
automatically for 30 days — no need to re-enter anything daily.

## How it works
1. Open the app, pick the apps you want blocked, and set one **unblock time**
   (e.g. 18:00).
2. Tap **Start 30-Day Block**.
3. Every day, from midnight until your unblock time, the selected apps are
   blocked. After the unblock time, they're free to use for the rest of the
   day. This repeats automatically for 30 days from the start date.
4. Tap **Stop** any time to cancel early.

Blocking works via an **Accessibility Service**: when a blocked app is
opened during the block window, the app detects it and shows a full-screen
"blocked" screen instead, with a button to go back to the home screen.

## Setup (build it yourself)
1. Install **Android Studio** (Hedgehog or newer).
2. Open the `AppBlocker` folder as a project (`File → Open`).
3. Let Gradle sync (needs internet the first time to download dependencies).
4. Connect an Android phone (USB debugging on) or use an emulator, then
   press **Run**.
5. On first launch:
   - Tap **Open Accessibility Settings** and enable "App Blocker" in
     Settings → Accessibility → Installed apps. Android will show a
     warning dialog about accessibility permissions — this is expected
     for any app-blocking app since that's the only way Android lets an
     app see which app is currently in the foreground.
   - Go back, select apps, set your time, tap **Start 30-Day Block**.

## Notes / limitations
- Minimum Android version: 7.0 (API 24).
- The schedule is stored locally on-device (SharedPreferences) — nothing is
  sent anywhere.
- If the user disables the Accessibility Service from system settings,
  blocking stops until it's re-enabled (Android doesn't allow apps to
  prevent this, by design, so it can't be fully tamper-proof — that's true
  of any app blocker).
- "30 days" starts the moment you tap Start, and counts full calendar days.
- Uninstalling/reinstalling resets the schedule.

## Project structure
- `MainActivity.kt` — app picker, time picker, start/stop.
- `BlockerPrefs.kt` — all scheduling logic (what "blocked right now" means).
- `AppBlockerService.kt` — Accessibility Service that detects the
  foreground app and triggers the block screen.
- `BlockActivity.kt` — the full-screen block UI.
- `BootReceiver.kt` — placeholder for post-reboot behavior (schedule logic
  is stateless/date-based so it needs no boot-time recomputation).
=======
# BlocKIO
An application that blocks the specified applications between the given time
>>>>>>> d376f7ac0a407983a10345d52c6cafdb97120ecf
