# PS5 DualSense → Keyboard Mapper

Use your **PS5 DualSense controller** (USB or Bluetooth) as a keyboard for
VLC, YouTube, Spotify, or any other app that responds to keyboard shortcuts.

Built with [JInput](https://github.com/jinput/jinput) and Java's `java.awt.Robot`.

---

## Features

- Works over **USB and Bluetooth**
- **Hold-to-repeat** — holding a button fires the key every 150 ms
- **Fully remappable** — one constant per button at the top of the file
- **Disable any button** by setting its key to `0`
- Natives extracted automatically on first run
- Silent terminal (only the mapping banner prints)
- Releases all held keys on Ctrl+C (no stuck keys)

---

## Default Mapping

```
+--------------------------------------------------+
|         PS5 DualSense -> Keyboard Mapper         |
+--------------------------------------------------+
          D-Pad  Up          ->  V
          D-Pad  Down        ->  (disabled)
          D-Pad  Left        ->  C
          D-Pad  Right       ->  B
          Square             ->  Left
          Circle             ->  Right
          Cross              ->  Down
          Triangle           ->  Up
          L1                 ->  M
          R1                 ->  Space
          L2                 ->  (disabled)
          R2                 ->  (disabled)
          L3                 ->  (disabled)
          R3                 ->  (disabled)
+--------------------------------------------------+
|  Running...  (Ctrl+C to stop)                    |
+--------------------------------------------------+
```

| Controller        | Keyboard       |
|-------------------|----------------|
| D-Pad Up          | `V`            |
| D-Pad Down        | *(disabled)*   |
| D-Pad Left        | `C`            |
| D-Pad Right       | `B`            |
| Square            | `←` Left       |
| Circle            | `→` Right      |
| Cross             | `↓` Down       |
| Triangle          | `↑` Up         |
| L1                | `M`            |
| R1                | `Space`        |
| L2 / R2 / L3 / R3 | *(disabled)*   |

---

## Requirements

- **Java JDK 17 or newer** — [Adoptium Temurin 17 or 21](https://adoptium.net/)
- **Windows 10 / 11** (Linux & macOS also work — see *Other Platforms*)
- A **PS5 DualSense controller** connected via USB or Bluetooth
- **VLC** or another app that reacts to keyboard shortcuts

Check Java is installed:

```powershell
java -version
javac -version
```

Both should print the same version (17+).

---

## Setup (First Time)

### 1. Create the project folder

```powershell
cd c:\Users\johns\OneDrive\Documents\vscode\JAVA
mkdir ps5-controller
cd ps5-controller
mkdir lib, scr
```

### 2. Download the two JARs into `lib\`

```powershell
cd lib

# Core JInput API
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/net/java/jinput/jinput/2.0.9/jinput-2.0.9.jar" `
    -OutFile "jinput-2.0.9.jar"

# Native binaries (hosted by ImageJ — Maven Central doesn't publish natives-all for 2.0.9)
Invoke-WebRequest -Uri "https://sites.imagej.net/SciView/jars/win64/net.java.jinput.jinput-2.0.9-natives-all.jar-20210622120136" `
    -OutFile "jinput-platform-2.0.9-natives-all.jar"
```

Verify the sizes:

```powershell
Get-ChildItem
```

Expected:

```
jinput-2.0.9.jar                        ~207 KB
jinput-platform-2.0.9-natives-all.jar   ~240 KB
```

If either is under 10 KB, the download failed (you got an HTML error page). Re-download.

### 3. Save the source

Create `scr\PS5ControllerVLC.java` with the code from this repo.

### 4. Compile

```powershell
cd c:\Users\johns\OneDrive\Documents\vscode\JAVA\ps5-controller
javac -cp "lib\jinput-2.0.9.jar" -d out scr\PS5ControllerVLC.java
```

Silence = success.

### 5. Run

```powershell
java --enable-native-access=ALL-UNNAMED -cp "out;lib\jinput-2.0.9.jar" PS5ControllerVLC
```

You'll see the banner, then it sits waiting for input.

> **First run only:** native DLLs are extracted into `lib\natives\`. Later runs reuse them.

---

## Daily Use

Two lines. That's it.

```powershell
cd c:\Users\johns\OneDrive\Documents\vscode\JAVA\ps5-controller
java --enable-native-access=ALL-UNNAMED -cp "out;lib\jinput-2.0.9.jar" PS5ControllerVLC
```

You only recompile if you change the `.java` file.

### Steps

1. **Connect** the controller (USB or Bluetooth).
2. **Start** the program (the two lines above).
3. **Open VLC** (or YouTube, Spotify, anything).
4. **Click on the VLC window** so it's the active window.
5. **Press buttons** — VLC reacts.
6. **Ctrl+C** in PowerShell to stop.

> ⚠️ **Keystrokes go to the focused window.** If VLC isn't on top, the keys go somewhere else. Click VLC first.

> ⚠️ **Don't mix UAC levels.** If VLC runs as Administrator, PowerShell must too (or vice versa). Windows blocks synthetic keys across that boundary.

---

## One-Click Launch

Create `run.bat` in the project root:

```batch
@echo off
cd /d "%~dp0"
java --enable-native-access=ALL-UNNAMED -cp "out;lib\jinput-2.0.9.jar" PS5ControllerVLC
pause
```

Double-click `run.bat` to launch. Right-click → **Send to → Desktop (create shortcut)** for one-click access.

If you edit the code often, use this version — it recompiles before running:

```batch
@echo off
cd /d "%~dp0"
javac -cp "lib\jinput-2.0.9.jar" -d out scr\PS5ControllerVLC.java
if errorlevel 1 (
    echo.
    echo Compile failed.
    pause
    exit /b 1
)
java --enable-native-access=ALL-UNNAMED -cp "out;lib\jinput-2.0.9.jar" PS5ControllerVLC
pause
```

---

## Pairing the Controller

### USB
Plug it in. Windows detects it within seconds.

### Bluetooth
1. On the controller, hold **PS + Create** until the lightbar flashes rapidly.
2. On Windows: **Settings → Bluetooth & devices → Add device → Bluetooth**.
3. Select **DualSense Wireless Controller**.

Confirm under **Settings → Bluetooth & devices** — it should appear under Input.

---

## Customizing the Mapping

Open `scr\PS5ControllerVLC.java`. The constants at the top control everything:

```java
private static final int KEY_DPAD_UP    = KeyEvent.VK_V;
private static final int KEY_DPAD_DOWN  = 0;             // disabled
private static final int KEY_DPAD_LEFT  = KeyEvent.VK_C;
private static final int KEY_DPAD_RIGHT = KeyEvent.VK_B;
private static final int KEY_SQUARE     = KeyEvent.VK_LEFT;
private static final int KEY_CIRCLE     = KeyEvent.VK_RIGHT;
private static final int KEY_CROSS      = KeyEvent.VK_DOWN;
private static final int KEY_TRIANGLE   = KeyEvent.VK_UP;
private static final int KEY_L1         = KeyEvent.VK_M;
private static final int KEY_R1         = KeyEvent.VK_SPACE;
private static final int KEY_L2         = 0;
private static final int KEY_R2         = 0;
private static final int KEY_L3         = 0;
private static final int KEY_R3         = 0;
```

### Common keycodes

| You want           | Constant                                                    |
|--------------------|-------------------------------------------------------------|
| Letters `A` – `Z`  | `KeyEvent.VK_A` … `VK_Z`                                    |
| Digits `0` – `9`   | `KeyEvent.VK_0` … `VK_9`                                    |
| Arrow keys         | `VK_LEFT`, `VK_RIGHT`, `VK_UP`, `VK_DOWN`                   |
| Space              | `VK_SPACE`                                                  |
| Enter              | `VK_ENTER`                                                  |
| Escape             | `VK_ESCAPE`                                                 |
| Tab                | `VK_TAB`                                                    |
| Backspace          | `VK_BACK_SPACE`                                             |
| Shift / Ctrl / Alt | `VK_SHIFT`, `VK_CONTROL`, `VK_ALT`                          |
| Function keys      | `VK_F1` … `VK_F12`                                          |
| Media keys         | `VK_MEDIA_PLAY_PAUSE`, `VK_MEDIA_NEXT_TRACK`, `VK_MEDIA_PREV_TRACK`, `VK_MEDIA_STOP` |
| Volume             | `VK_VOLUME_UP`, `VK_VOLUME_DOWN`, `VK_VOLUME_MUTE`          |

### Disable a button

```java
private static final int KEY_L2 = 0;
```

The banner will show `(disabled)`.

### Enable a disabled button

```java
private static final int KEY_L2 = KeyEvent.VK_SHIFT;
```

Recompile:

```powershell
javac -cp "lib\jinput-2.0.9.jar" -d out scr\PS5ControllerVLC.java
```

---

## Auto-Repeat Speed

While a button is held, its key is resent every `REPEAT_DELAY_MS` milliseconds:

```java
private static final long REPEAT_DELAY_MS = 150;
```

| Value | Feel              |
|-------|-------------------|
| 50    | Very fast         |
| 80    | Snappy            |
| 150   | Moderate (default)|
| 300   | Slow / precise    |

This is what lets you hold Triangle to scrub through a VLC timeline, or hold Up to scroll a menu continuously.

---

## Troubleshooting

| Symptom | Fix |
|---|---|
| `file not found: scr\PS5ControllerVLC.java` | You're not in the project root. `cd` there first. |
| `package net.java.games.input does not exist` | JAR missing or wrong classpath. Check `lib\jinput-2.0.9.jar`. |
| `Missing: lib\jinput-platform-2.0.9-natives-all.jar` | Natives JAR wasn't downloaded. See **Setup step 2**. |
| `No PS5 controller found.` | Controller not connected, or JInput can't see it. Try USB, replug, restart the program. |
| `WARNING: A restricted method in java.lang.System has been called` | Harmless JDK 24+ warning. The `--enable-native-access=ALL-UNNAMED` flag silences it. |
| Buttons work but VLC doesn't react | Click VLC first (it must be focused). Or check VLC's **Tools → Preferences → Hotkeys**. |
| Keys get stuck in VLC | Press Ctrl+C — the shutdown hook releases them. |
| Native DLL error on startup | Delete `lib\natives\` and run again to re-extract. |

---

## Other Platforms (Linux / macOS)

Same setup, but use `:` instead of `;` in the classpath:

```bash
javac -cp "lib/jinput-2.0.9.jar" -d out scr/PS5ControllerVLC.java
java --enable-native-access=ALL-UNNAMED -cp "out:lib/jinput-2.0.9.jar" PS5ControllerVLC
```

- **macOS** — grant Accessibility permission to your terminal (System Settings → Privacy & Security → Accessibility).
- **Linux X11** — works out of the box.
- **Linux Wayland** — `Robot` is often blocked. Run under XWayland or an X11 session.

---

## Project Layout

```
ps5-controller/
├── lib/
│   ├── jinput-2.0.9.jar                       # Core JInput API
│   ├── jinput-platform-2.0.9-natives-all.jar  # Native DLLs (extracted on first run)
│   └── natives/                               # Auto-created on first run
├── scr/
│   └── PS5ControllerVLC.java                  # The program
├── out/                                       # Auto-created by javac
├── run.bat                                    # Optional launcher
└── README.md
```

---

## How It Works

1. **JInput** discovers connected controllers and produces a stream of `Event`s.
2. Each event is matched to a `Component` (`Button.1`, `Axis.POV`, …).
3. A lookup translates the component to an AWT keycode (`KeyEvent.VK_*`).
4. `java.awt.Robot` sends `keyPress` / `keyRelease` to the OS.
5. A 10 ms poll loop drains the JInput event queue; held keys re-fire every 150 ms.
6. A shutdown hook releases any held keys on Ctrl+C.

---

## License

Public domain. Do whatever you want.

## Credits

- [JInput](https://github.com/jinput/jinput) — controller input library
- [ImageJ](https://imagej.net/) — mirror for the `natives-all` JAR
