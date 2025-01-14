import javax.swing.*;
import java.awt.*;

public class LoginView {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private LoginHandler loginHandler;

    public LoginView() {
        // Konfiguracja okna logowania
        frame = new JFrame("Logowanie");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(4, 2));

        // Pola do wprowadzania danych logowania
        frame.add(new JLabel("Nazwa użytkownika:"));
        usernameField = new JTextField();
        frame.add(usernameField);

        frame.add(new JLabel("Hasło:"));
        passwordField = new JPasswordField();
        frame.add(passwordField);

        frame.add(new JLabel("Rola:"));
        roleComboBox = new JComboBox<>(new String[]{"klient", "pracownik"});
        frame.add(roleComboBox);

        // Przycisk logowania
        JButton loginButton = new JButton("Zaloguj");
        loginButton.addActionListener(e -> handleLogin());
        frame.add(loginButton);

        frame.setVisible(true);

        // Inicjalizacja login handler
        loginHandler = new LoginHandler(this);
    }

    // Obsługa logowania
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = (String) roleComboBox.getSelectedItem();

        loginHandler.handleLogin(username, password, role);
    }

    // Metody do manipulacji oknem
    public void closeWindow() {
        frame.dispose();
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    public void clearPasswordField() {
        passwordField.setText("");
    }
}
