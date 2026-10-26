package swing;

import javax.swing.UIManager;

public final class Apariencia {

    private Apariencia() {
    }

    public static void aplicar() {
        UIManager.put("Component.arc", 10);
        UIManager.put("Button.arc", 10);
        UIManager.put("TextComponent.arc", 8);
    }
}