import net.java.games.input.*;
import java.io.File;
import java.util.Enumeration;
import java.util.jar.*;

public class DumpController {
    public static void main(String[] args) throws Exception {
        File natives = new File("lib/natives");
        System.setProperty("net.java.games.input.librarypath", natives.getAbsolutePath());
        System.setProperty("java.library.path", natives.getAbsolutePath());

        ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
        for (Controller c : env.getControllers()) {
            if (!c.getName().toLowerCase().contains("dualsense")) continue;
            System.out.println("Controller: " + c.getName() + "  type=" + c.getType());
            System.out.println("--- Components ---");
            for (Component comp : c.getComponents()) {
                System.out.printf("  name='%s'  id=%s  analog=%s%n",
                        comp.getName(), comp.getIdentifier(), comp.isAnalog());
            }
        }
    }
}