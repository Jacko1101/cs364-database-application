
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        /* db connect in MainFrame constructor so functions can access it */
        //Database db = new Database();
        //db.connect();

        MainFrame mf = new MainFrame();
        //SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
