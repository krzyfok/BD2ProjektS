import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ShopView implements ShopViewInterface {
    private JFrame frame;
    private JList<String> productList;
    private DefaultListModel<String> productListModel;
    private JTextArea productDetailsArea;
    private JButton addToCartButton;
    private JButton createServiceRequestButton;
    private JButton viewOrdersButton;
    private JButton viewServiceRequestsButton;
    private JButton viewCartButton;
    private JLabel totalPriceLabel;
    private JButton orderInCartButton;  // Przycisk do składania zamówienia z koszyka
    private JButton clearCartButton;  // Nowy przycisk do czyszczenia koszyka

    private ShopPresenter presenter;
    private JDialog paymentDialog;
    private JComboBox<String> paymentMethodComboBox;
    private JSpinner installmentSpinner;
    private JButton confirmPaymentButton;
    public ShopView() {
        frame = new JFrame("Sklep");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Panel z listą produktów
        productListModel = new DefaultListModel<>();
        productList = new JList<>(productListModel);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productList.addListSelectionListener(e -> presenter.showProductDetails(productList.getSelectedValue()));

        // Panel do wyświetlania szczegółów produktu
        productDetailsArea = new JTextArea();
        productDetailsArea.setEditable(false);
        JScrollPane detailsScrollPane = new JScrollPane(productDetailsArea);

        // Przycisk dodawania produktu do koszyka
        addToCartButton = new JButton("Dodaj do koszyka");
        addToCartButton.addActionListener(e -> presenter.addToCart(productList.getSelectedValue()));

        // Przyciski do zgłoszeń serwisowych i wyświetlania zamówień
        createServiceRequestButton = new JButton("Zgłoś problem");
        createServiceRequestButton.addActionListener(e -> presenter.createServiceRequest());

        viewOrdersButton = new JButton("Zobacz zamówienia");
        viewOrdersButton.addActionListener(e -> presenter.viewOrders());

        viewServiceRequestsButton = new JButton("Zobacz zgłoszenia serwisowe");
        viewServiceRequestsButton.addActionListener(e -> presenter.viewServiceRequests());

        // Przycisk do wyświetlania koszyka
        viewCartButton = new JButton("Koszyk");
        viewCartButton.addActionListener(e -> presenter.viewCart());

        clearCartButton = new JButton("Wyczyść koszyk");
        clearCartButton.addActionListener(e -> presenter.clearCart());
        // Przycisk do składania zamówienia z koszyka
        orderInCartButton = new JButton("Zamów");
        orderInCartButton.addActionListener(e -> showPaymentDialog());


        // Nowy przycisk do czyszczenia koszyka
       // clearCartButton = new JButton("Wyczyść koszyk");

        // Układ okna
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(new JScrollPane(productList), BorderLayout.CENTER);
        leftPanel.add(addToCartButton, BorderLayout.SOUTH);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(1, 6));  // Zmiana, aby dodać przycisk płatności
        bottomPanel.add(createServiceRequestButton);
        bottomPanel.add(viewOrdersButton);
        bottomPanel.add(viewServiceRequestsButton);
        bottomPanel.add(viewCartButton);  // Dodanie przycisku koszyka
        bottomPanel.add(orderInCartButton); // Dodanie przycisku "Zamów w koszyku"
        bottomPanel.add(clearCartButton);

        frame.add(leftPanel, BorderLayout.WEST);
        frame.add(detailsScrollPane, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        // Okno płatności
        paymentDialog = new JDialog(frame, "Wybór Płatności", true);
        paymentDialog.setSize(400, 300);
        paymentDialog.setLayout(new GridLayout(4, 2));

        // Pole wyboru metody płatności
        paymentDialog.add(new JLabel("Metoda Płatności:"));
        paymentMethodComboBox = new JComboBox<>(new String[]{"Karta kredytowa", "Przelew", "Gotówka"});
        paymentDialog.add(paymentMethodComboBox);

        // Pole wyboru liczby rat
        paymentDialog.add(new JLabel("Liczba rat:"));
        installmentSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 12, 1)); // Możliwość wyboru od 1 do 12 rat
        paymentDialog.add(installmentSpinner);
        paymentDialog.add(new JLabel("Łączna cena:"));
        totalPriceLabel = new JLabel("0.00 PLN");  // Domyślnie łączna cena wynosi 0.00 PLN
        paymentDialog.add(totalPriceLabel);
        // Przycisk do potwierdzenia płatności
        confirmPaymentButton = new JButton("Potwierdź płatność");
        confirmPaymentButton.addActionListener(e ->  presenter.orderCart());
        paymentDialog.add(confirmPaymentButton);

        paymentDialog.setVisible(false); // Domyślnie ukryte
        frame.setVisible(true);
    }

    public void setPresenter(ShopPresenter presenter) {
        this.presenter = presenter;
    }
    private void showPaymentDialog() {
        // Oblicz łączną cenę koszyka i ustaw ją w etykiecie
        double totalPrice = presenter.calculateTotalPrice();  // Nowa metoda w presenterze, która oblicza łączną cenę
        totalPriceLabel.setText(String.format("%.2f PLN", totalPrice));

        paymentDialog.setVisible(true);
    }

    private void handlePayment() {
        // Pobieramy wybraną metodę płatności
        String selectedPaymentMethod = (String) paymentMethodComboBox.getSelectedItem();
        int selectedInstallments = (Integer) installmentSpinner.getValue();

        // Możesz przekazać te dane do presenter'a, aby zapisać je w bazie danych
        presenter.handlePayment(selectedPaymentMethod, selectedInstallments);

        // Zamykanie okna płatności
        paymentDialog.setVisible(false);
        JOptionPane.showMessageDialog(frame, "Płatność została zrealizowana.");
    }
    @Override
    public void updateProductList(List<String> products) {
        productListModel.clear();
        for (String product : products) {
            productListModel.addElement(product);
        }
    }

    @Override
    public void showProductDetails(String productDetails) {
        productDetailsArea.setText("Szczegóły produktu: " + productDetails);
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    @Override
    public void closeWindow() {
        frame.dispose();
    }
}
