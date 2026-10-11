package tests;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import javax.imageio.ImageIO;
import javax.swing.*;
import model.User;
import ui.*;
import util.Session;

/** Opens every screen as Admin and as Staff and checks the role rules. Needs a database with the schema loaded. */
public class GuiSmokeTest {
    static int fail = 0;

    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) fail++;
    }

    static void login(String role, int id) {
        User u = new User(); u.setUserId(id); u.setUsername(role.toLowerCase()); u.setFullName("Test " + role);
        u.setRole(role); u.setStatus("Active");
        Session.login(u);
    }

    static JButton button(JFrame f, String field) throws Exception {
        Field fld = f.getClass().getDeclaredField(field);
        fld.setAccessible(true);
        return (JButton) fld.get(f);
    }

    static void shot(JFrame f, String file) throws Exception {
        BufferedImage img = new BufferedImage(f.getWidth(), f.getHeight(), BufferedImage.TYPE_INT_RGB);
        f.paint(img.getGraphics());
        ImageIO.write(img, "png", new File(file));
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                login("Admin", 1);
                JFrame[] all = {new UnitFrame(), new TenantFrame(), new ContractFrame(), new PaymentFrame(),
                    new MaintenanceFrame(), new UserFrame(), new ReportsFrame(), new DashboardFrame()};
                for (JFrame f : all) { f.setVisible(true); }
                check("Admin: all 8 screens open without errors", true);
                check("Admin: Delete is enabled in Units", button(all[0], "btnDelete").isEnabled());
                if (args.length > 0) {
                    shot(all[0], args[0] + "/units.png"); shot(all[2], args[0] + "/contracts.png");
                    shot(all[3], args[0] + "/payments.png"); shot(all[7], args[0] + "/dashboard.png");
                }
                for (JFrame f : all) f.dispose();

                login("Staff", 2);
                UnitFrame u = new UnitFrame(); TenantFrame t = new TenantFrame(); ContractFrame c = new ContractFrame();
                PaymentFrame p = new PaymentFrame(); MaintenanceFrame m = new MaintenanceFrame();
                check("Staff: Delete disabled in Units", !button(u, "btnDelete").isEnabled());
                check("Staff: Delete disabled in Tenants", !button(t, "btnDelete").isEnabled());
                check("Staff: Delete disabled in Contracts", !button(c, "btnDelete").isEnabled());
                check("Staff: Delete disabled in Payments", !button(p, "btnDelete").isEnabled());
                check("Staff: Delete disabled in Maintenance", !button(m, "btnDelete").isEnabled());
                DashboardFrame d = new DashboardFrame();
                JMenuBar bar = d.getJMenuBar();
                boolean usersDisabled = false;
                for (int i = 0; i < bar.getMenuCount(); i++)
                    if ("Users".equals(bar.getMenu(i).getText())) usersDisabled = !bar.getMenu(i).getItem(0).isEnabled();
                check("Staff: Users menu item is disabled", usersDisabled);
                for (Window w : Window.getWindows()) w.dispose();

                Session.logout();
                LoginFrame l = new LoginFrame();
                l.setVisible(true);
                check("Login screen opens", l.isShowing());
                l.dispose();
            } catch (Throwable e) {
                e.printStackTrace();
                check("exception: " + e, false);
            }
        });
        System.out.println(fail == 0 ? "\nGUI smoke test: ALL PASSED" : "\nGUI smoke test: " + fail + " FAILED");
        System.exit(fail == 0 ? 0 : 1);
    }
}
