import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PS5ControllerVLC {

    // ============================================================
    // KEY MAPPINGS — change these to remap any button
    // Set to 0 to disable.
    // ============================================================
    private static final int KEY_DPAD_UP    = KeyEvent.VK_V;
    private static final int KEY_DPAD_DOWN  = 0;
    private static final int KEY_DPAD_LEFT  = KeyEvent.VK_C;
    private static final int KEY_DPAD_RIGHT = KeyEvent.VK_B;

    private static final int KEY_SQUARE     = KeyEvent.VK_LEFT;
    private static final int KEY_CROSS      = KeyEvent.VK_DOWN;
    private static final int KEY_CIRCLE     = KeyEvent.VK_RIGHT;
    private static final int KEY_TRIANGLE   = KeyEvent.VK_UP;

    private static final int KEY_L1         = KeyEvent.VK_M;
    private static final int KEY_R1         = KeyEvent.VK_SPACE;
    private static final int KEY_L2         = 0;
    private static final int KEY_R2         = 0;
    private static final int KEY_L3         = 0;
    private static final int KEY_R3         = 0;

    private static final int KEY_CREATE     = 0;
    private static final int KEY_OPTIONS    = 0;
    private static final int KEY_PS         = 0;
    private static final int KEY_TOUCHPAD   = 0;
    private static final int KEY_MUTE       = 0;

    // ---------- Tunables ----------
    private static final int  POLL_INTERVAL_MS = 10;
    private static final long REPEAT_DELAY_MS  = 120;

    private static final String NATIVES_DIR = "lib/natives";
    private static final String NATIVES_JAR = "lib/jinput-platform-2.0.9-natives-all.jar";

    // ---------- State ----------
    // buttonName -> timestamp of last keyPress sent
    private static final Map<String, Long> heldKeys = new HashMap<>();
    private static int lastPovDir = 0;

    public static void main(String[] args) throws Exception {

        java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.OFF);
        java.util.logging.Logger.getLogger("net.java.games.input").setLevel(java.util.logging.Level.OFF);

        prepareNatives();

        Robot robot = new Robot();
        robot.setAutoDelay(0);

        // Release every held key on Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (String name : heldKeys.keySet()) {
                int k = keyForName(name);
                if (k != 0) robot.keyRelease(k);
            }
        }));

        Controller controller = findPS5Controller();
        if (controller == null) {
            System.out.println("No PS5 controller found.");
            System.exit(1);
        }

        printBanner();

        while (true) {
            controller.poll();
            EventQueue queue = controller.getEventQueue();
            Event event = new Event();

            // 1. Process all events from this poll
            while (queue.getNextEvent(event)) {
                Component component = event.getComponent();
                float value = event.getValue();

                if (component.getIdentifier() == Component.Identifier.Axis.POV) {
                    handlePov(value, robot);
                } else if (!component.isAnalog()) {
                    handleButton(component, value > 0.5f, robot);
                }
            }

            // 2. Auto-repeat every held key
            autoRepeat(robot);

            Thread.sleep(POLL_INTERVAL_MS);
        }
    }

    // ============================================================
    // Banner
    // ============================================================

    private static void printBanner() {
        final String L = "            %-18s ->  %s\n";

        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("  +--------------------------------------------------+\n");
        sb.append("  |         PS5 DualSense -> Keyboard Mapper         |\n");
        sb.append("  +--------------------------------------------------+\n");
        sb.append(String.format(L, "D-Pad  Up",    keyName(KEY_DPAD_UP)));
        sb.append(String.format(L, "D-Pad  Down",  keyName(KEY_DPAD_DOWN)));
        sb.append(String.format(L, "D-Pad  Left",  keyName(KEY_DPAD_LEFT)));
        sb.append(String.format(L, "D-Pad  Right", keyName(KEY_DPAD_RIGHT)));
        sb.append(String.format(L, "Square",       keyName(KEY_SQUARE)));
        sb.append(String.format(L, "Circle",       keyName(KEY_CIRCLE)));
        sb.append(String.format(L, "Cross",        keyName(KEY_CROSS)));
        sb.append(String.format(L, "Triangle",     keyName(KEY_TRIANGLE)));
        sb.append(String.format(L, "L1",           keyName(KEY_L1)));
        sb.append(String.format(L, "R1",           keyName(KEY_R1)));
        sb.append(String.format(L, "L2",           keyName(KEY_L2)));
        sb.append(String.format(L, "R2",           keyName(KEY_R2)));
        sb.append(String.format(L, "L3",           keyName(KEY_L3)));
        sb.append(String.format(L, "R3",           keyName(KEY_R3)));
        sb.append("  +--------------------------------------------------+\n");
        sb.append("  |  Running...  (Ctrl+C to stop)                    |\n");
        sb.append("  +--------------------------------------------------+\n");
        sb.append("\n");
        System.out.print(sb);
    }

    private static String keyName(int keyCode) {
        if (keyCode == 0) return "(disabled)";
        String s = KeyEvent.getKeyText(keyCode);
        if (s.length() > 14) s = s.substring(0, 14);
        return s;
    }

    // ============================================================
    // Native library handling
    // ============================================================

    private static void prepareNatives() throws IOException {
        File nativesDir = new File(NATIVES_DIR);
        if (!nativesDir.exists()) nativesDir.mkdirs();

        File nativesJar = new File(NATIVES_JAR);
        if (!nativesJar.exists()) {
            System.err.println("Missing: " + nativesJar.getAbsolutePath());
            System.exit(1);
        }

        File[] existing = nativesDir.listFiles();
        if (existing == null || existing.length == 0) {
            try (JarFile jar = new JarFile(nativesJar)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.endsWith(".dll") || name.endsWith(".so")
                            || name.endsWith(".dylib") || name.endsWith(".jnilib")) {
                        String fileName = name.substring(name.lastIndexOf('/') + 1);
                        File out = new File(nativesDir, fileName);
                        try (InputStream in = jar.getInputStream(entry);
                             FileOutputStream fos = new FileOutputStream(out)) {
                            byte[] buf = new byte[8192];
                            int n;
                            while ((n = in.read(buf)) != -1) fos.write(buf, 0, n);
                        }
                    }
                }
            }
        }

        String abs = nativesDir.getAbsolutePath();
        System.setProperty("net.java.games.input.librarypath", abs);
        System.setProperty("java.library.path", abs);
    }

    // ============================================================
    // Controller discovery
    // ============================================================

    private static Controller findPS5Controller() {
        ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
        for (Controller c : env.getControllers()) {
            String n = c.getName().toLowerCase();
            if (n.contains("dualsense") || n.contains("ps5")
                    || n.contains("wireless controller")
                    || n.contains("sony") || n.contains("playstation")) {
                return c;
            }
        }
        return null;
    }

    // ============================================================
    // D-Pad (POV hat) — tap + auto-repeat
    // ============================================================

    private static void handlePov(float value, Robot robot) {
        int dir = povToDirection(value);
        if (dir == lastPovDir) return;

        // Release previous direction
        if (lastPovDir != 0) {
            int prevKey = povKeyFor(lastPovDir);
            if (prevKey != 0) robot.keyRelease(prevKey);
            heldKeys.remove("POV_" + lastPovDir);
        }

        // Press new direction (instant tap so quick taps register)
        if (dir != 0) {
            int key = povKeyFor(dir);
            if (key != 0) {
                robot.keyPress(key);
                robot.keyRelease(key);
                heldKeys.put("POV_" + dir, System.currentTimeMillis());
            }
        }
        lastPovDir = dir;
    }

    private static int povKeyFor(int dir) {
        switch (dir) {
            case 1: return KEY_DPAD_UP;
            case 2: return KEY_DPAD_RIGHT;
            case 3: return KEY_DPAD_DOWN;
            case 4: return KEY_DPAD_LEFT;
            default: return 0;
        }
    }

    private static int povToDirection(float v) {
        if (v == 0.0f) return 0;
        float step = Math.round(v * 4f) / 4f;
        if (step == 0.25f) return 1;
        if (step == 0.5f)  return 2;
        if (step == 0.75f) return 3;
        if (step == 1.0f)  return 4;
        return 0;
    }

    // ============================================================
    // Digital buttons — tap + auto-repeat
    // ============================================================

    private static void handleButton(Component component, boolean pressed, Robot robot) {
        String name = component.getName();
        int key = keyForButton(component);
        if (key == 0) return;

        if (pressed) {
            if (!heldKeys.containsKey(name)) {
                robot.keyPress(key);
                robot.keyRelease(key);
                heldKeys.put(name, System.currentTimeMillis());
            }
        } else {
            heldKeys.remove(name);
        }
    }

    private static void autoRepeat(Robot robot) {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = heldKeys.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> e = it.next();
            if (now - e.getValue() < REPEAT_DELAY_MS) continue;

            int key = keyForName(e.getKey());
            if (key != 0) {
                robot.keyPress(key);
                robot.keyRelease(key);
            }
            e.setValue(now);
        }
    }

    private static int keyForName(String name) {
        switch (name) {
            case "Button 0":  return KEY_SQUARE;
            case "Button 1":  return KEY_CROSS;
            case "Button 2":  return KEY_CIRCLE;
            case "Button 3":  return KEY_TRIANGLE;
            case "Button 4":  return KEY_L1;
            case "Button 5":  return KEY_R1;
            case "Button 6":  return KEY_L2;
            case "Button 7":  return KEY_R2;
            case "Button 8":  return KEY_CREATE;
            case "Button 9":  return KEY_OPTIONS;
            case "Button 10": return KEY_L3;
            case "Button 11": return KEY_R3;
            case "Button 12": return KEY_PS;
            case "Button 13": return KEY_TOUCHPAD;
            case "Button 14": return KEY_MUTE;
        }
        if (name.startsWith("POV_")) {
            return povKeyFor(Integer.parseInt(name.substring(4)));
        }
        return 0;
    }

    private static int keyForButton(Component component) {
        Component.Identifier id = component.getIdentifier();

        if (id == Component.Identifier.Button._0)  return KEY_SQUARE;
        if (id == Component.Identifier.Button._1)  return KEY_CROSS;
        if (id == Component.Identifier.Button._2)  return KEY_CIRCLE;
        if (id == Component.Identifier.Button._3)  return KEY_TRIANGLE;
        if (id == Component.Identifier.Button._4)  return KEY_L1;
        if (id == Component.Identifier.Button._5)  return KEY_R1;
        if (id == Component.Identifier.Button._6)  return KEY_L2;
        if (id == Component.Identifier.Button._7)  return KEY_R2;
        if (id == Component.Identifier.Button._8)  return KEY_CREATE;
        if (id == Component.Identifier.Button._9)  return KEY_OPTIONS;
        if (id == Component.Identifier.Button._10) return KEY_L3;
        if (id == Component.Identifier.Button._11) return KEY_R3;
        if (id == Component.Identifier.Button._12) return KEY_PS;
        if (id == Component.Identifier.Button._13) return KEY_TOUCHPAD;
        if (id == Component.Identifier.Button._14) return KEY_MUTE;
        return 0;
    }
}