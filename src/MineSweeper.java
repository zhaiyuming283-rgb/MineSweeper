//import controller.Game;
//import javax.swing.*;
//
//public class MineSweeper {
//    public static void main() {
//        SwingUtilities.invokeLater(() -> {
//            UIManager.put("OptionPane.yesButtonText", "Yes");
//            UIManager.put("OptionPane.noButtonText", "No");
//            UIManager.put("OptionPane.okButtonText", "OK");
//            UIManager.put("OptionPane.cancelButtonText", "Cancel");
//
//            Game game = new Game();
//            game.show();
//        });
//    }
//}

import controller.Game;
import javax.swing.*;

public class MineSweeper {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("OptionPane.yesButtonText", "Yes");
            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.okButtonText", "OK");
            UIManager.put("OptionPane.cancelButtonText", "Cancel");

            Game game = new Game();
            game.show();
        });
    }
}