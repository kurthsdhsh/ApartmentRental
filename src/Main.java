import database.DatabaseInitializer;
import java.sql.SQLException;
import javax.swing.UIManager;
import ui.LoginFrame;
import util.ErrorHelper;

public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
        try {
            DatabaseInitializer.ensureDefaultAdmin();        // admin / admin123 on first run
        } catch (SQLException e) {
            ErrorHelper.show(null, e);                       // shows "Is the Derby server running?"
            return;
        }
        java.awt.EventQueue.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
