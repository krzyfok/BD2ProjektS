import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShopView implements LoginViewInterface {
    private JFrame frame;
    private JList<String> productList;
    private DefaultListModel<String> productListModel;
    private JTextArea productDetailsArea;
    private JButton addToCartButton;
    private JButton createServiceRequestButton;
    private JButton viewOrdersButton;
    private JButton viewServiceRequestsButton;

    // Obiekt połączenia z bazą danych
    private DatabaseConnector databaseConnector;

    public ShopView(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;

        // Konfiguracja okna widoku sklepu
        frame = new JFrame("Sklep");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Panel z listą produktów
        productListModel = new DefaultListModel<>();
        productList = new JList<>(productListModel);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productList.addListSelectionListener(e -> showProductDetails());

        // Panel do wyświetlania szczegółów produktu
        productDetailsArea = new JTextArea();
        productDetailsArea.setEditable(false);
        JScrollPane detailsScrollPane = new JScrollPane(productDetailsArea);

        // Przycisk dodawania produktu do koszyka
        addToCartButton = new JButton("Dodaj do koszyka");
        addToCartButton.addActionListener(e -> addToCart());

        // Przyciski do zgłoszeń serwisowych i wyświetlania zamówień
        createServiceRequestButton = new JButton("Zgłoś problem");
        createServiceRequestButton.addActionListener(e -> createServiceRequest());

        viewOrdersButton = new JButton("Zobacz zamówienia");
        viewOrdersButton.addActionListener(e -> viewOrders());

        viewServiceRequestsButton = new JButton("Zobacz zgłoszenia serwisowe");
        viewServiceRequestsButton.addActionListener(e -> viewServiceRequests());

        // Układ okna
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(new JScrollPane(productList), BorderLayout.CENTER);
        leftPanel.add(addToCartButton, BorderLayout.SOUTH);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 3));
        bottomPanel.add(createServiceRequestButton);
        bottomPanel.add(viewOrdersButton);
        bottomPanel.add(viewServiceRequestsButton);

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(detailsScrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // Ładowanie produktów z bazy danych
        loadProducts();
    }

    // Pobranie listy produktów z bazy danych
    private void loadProducts() {
        try {
            List<String> products = databaseConnector.getProducts();
            productListModel.clear(); // Wyczyść listę przed dodaniem nowych danych
            for (String product : products) {
                productListModel.addElement(product);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Błąd podczas ładowania produktów: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Wyświetlanie szczegółów produktu
    private void showProductDetails() {
        String selectedProduct = productList.getSelectedValue();
        if (selectedProduct != null) {
            productDetailsArea.setText("Szczegóły produktu: " + selectedProduct + "\nOpis: To jest świetny produkt!");
        }
    }

    // Dodanie produktu do koszyka (symulacja)
    private void addToCart() {
        String selectedProduct = productList.getSelectedValue();
        if (selectedProduct != null) {
            JOptionPane.showMessageDialog(frame, selectedProduct + " został dodany do koszyka.");
        } else {
            JOptionPane.showMessageDialog(frame, "Wybierz produkt, aby dodać do koszyka.");
        }
    }

    // Tworzenie zgłoszenia serwisowego
    private void createServiceRequest() {
        try {
            int clientId = UserSession.getLoggedInUserId();  // Zakładając, że ID klienta jest 1 (to trzeba zmienić w prawdziwej aplikacji)

            // Pobieranie listy sprzętów zakupionych przez klienta
            List<String> clientProducts = databaseConnector.getClientProducts(clientId);

            if (clientProducts.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Nie masz żadnego sprzętu zakupionego.");
                return;
            }

            // Wyświetlenie listy sprzętu z dodatkowymi danymi (np. numer seryjny, ID zakupu)
            // Aby użytkownik mógł wybrać sprzęt, który ma być przypisany do zgłoszenia serwisowego
            String selectedProduct = (String) JOptionPane.showInputDialog(
                    frame,
                    "Wybierz sprzęt do zgłoszenia serwisowego:",
                    "Wybór sprzętu",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    clientProducts.toArray(),
                    clientProducts.get(0)  // Domyślny wybór (pierwszy sprzęt w liście)
            );

            if (selectedProduct == null || selectedProduct.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Nie wybrano sprzętu.");
                return;
            }

            // Rozdzielenie danych produktu, aby uzyskać nr seryjny i ID zakupu
            String[] productDetails = selectedProduct.split(", ");
            int serialNumber = Integer.parseInt(productDetails[1].split(": ")[1]);
            int purchaseId = Integer.parseInt(productDetails[2].split(": ")[1]);
            int equipmentId = Integer.parseInt(productDetails[3].split(": ")[1]);
            int workerId = Integer.parseInt(productDetails[4].split(": ")[1]);



                // Tworzenie zgłoszenia serwisowego
                databaseConnector.addServiceRequest(workerId,clientId, serialNumber);
                JOptionPane.showMessageDialog(frame, "Zgłoszenie zostało utworzone.");


        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Błąd podczas dodawania zgłoszenia: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    // Pobieranie numeru seryjnego sprzętu na podstawie jego nazwy (można to zmienić w zależności od implementacji)
    private int getSerialNumberFromProduct(String productName) throws SQLException {
        // Zakładamy, że metoda pobierająca sprzęt na podstawie nazwy jest zaimplementowana
        return databaseConnector.getSerialNumberByProductName(productName);
    }


    // Wyświetlanie zamówień klienta
    private void viewOrders() {
        try {
            int clientId = 1;  // Zakładając, że ID klienta jest 1
            List<String> orders = databaseConnector.getClientOrders(clientId);
            if (orders.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Brak zamówień.");
            } else {
                JOptionPane.showMessageDialog(frame, "Zamówienia:\n" + String.join("\n", orders));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Błąd podczas pobierania zamówień: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Wyświetlanie zgłoszeń serwisowych klienta
    private void viewServiceRequests() {
        try {
            int clientId = 1;  // Zakładając, że ID klienta jest 1
            List<String> requests = databaseConnector.getClientServiceRequests(clientId);
            if (requests.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Brak zgłoszeń serwisowych.");
            } else {
                JOptionPane.showMessageDialog(frame, "Zgłoszenia serwisowe:\n" + String.join("\n", requests));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(frame, "Błąd podczas pobierania zgłoszeń: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Implementacja metod z interfejsu LoginViewInterface
    @Override
    public void closeWindow() {
        frame.dispose();
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    @Override
    public void clearPasswordField() {
        // Nie ma pola hasła w tym widoku, więc ta metoda nie jest wykorzystywana
    }
}
