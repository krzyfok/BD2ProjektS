import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnector {
    private Connection connection;

    // Metoda łączenia z bazą danych dla konkretnej roli
    public void connect(String role) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/mydb";
        String username = "root";
        String password = "";

        // Ustalanie poświadczeń na podstawie roli
        switch (role) {
            case "klient":
                username = "root";
                password = "";
                break;
            case "pracownik":
                username = "root";
                password = "";
                break;

            default:
                throw new SQLException("Nieznana rola: " + role);
        }

        // Nawiązanie połączenia z bazą danych
        this.connection = DriverManager.getConnection(url, username, password);
        System.out.println("Połączenie z bazą danych nawiązane dla roli: " + role);
    }

    // Metoda zamykania połączenia
    public void disconnect() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
            System.out.println("Połączenie z bazą danych zostało zamknięte.");
        }
    }

    // Getter dla obiektu Connection
    public Connection getConnection() {
        return this.connection;
    }

    // Weryfikacja danych logowania w bazie danych
    public boolean verifyCredentials(String username, String password, String role) {
        String sql;
        if(role.equals("klient")) {
             sql = "SELECT * FROM klient WHERE login = ? AND haslo = ? ";
        }
        else {
             sql = "SELECT * FROM pracownik WHERE login = ? AND haslo = ? ";
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);


            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // Zwraca true, jeśli znaleziono użytkownika
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    // Metoda do pobierania produktów z bazy danych
    public List<String> getProducts() throws SQLException {
        List<String> products = new ArrayList<>();
        String sql = "SELECT idsprzet, nazwa, cena FROM sprzet";  // Zapytanie SQL do pobrania produktów z nazwą i ceną
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                int id= resultSet.getInt("idsprzet");
                String productName = resultSet.getString("nazwa");
                double productPrice = resultSet.getDouble("cena");
                products.add(id+" " +productName + " - " + productPrice + " PLN");  // Łączymy nazwę produktu z ceną
            }
        }
        return products;
    }



    public List<String> getClientOrders(int clientId) throws SQLException {
        List<String> orders = new ArrayList<>();

        // Zapytanie pobierające zakupy klienta
        String sql = "SELECT idzakup FROM zakup WHERE klient_idklient = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int orderId = resultSet.getInt("idzakup");

                // Zapytanie pobierające sprzęt dla danego zakupu
                String productSql = "SELECT s.nazwa, s.cena FROM zakup_has_sprzet zh " +
                        "JOIN sprzet s ON zh.sprzet_idsprzet = s.idsprzet " +
                        "WHERE zh.zakup_idzakup = ?";
                try (PreparedStatement productStatement = connection.prepareStatement(productSql)) {
                    productStatement.setInt(1, orderId);
                    ResultSet productResultSet = productStatement.executeQuery();

                    StringBuilder products = new StringBuilder();
                    while (productResultSet.next()) {
                        String productName = productResultSet.getString("nazwa");
                        int productPrice = productResultSet.getInt("cena");
                        products.append(productName).append(" - Cena: ").append(productPrice).append(" zł, ");
                    }

                    if (products.length() > 0) {
                        products.setLength(products.length() - 2);  // Usuwanie ostatniego przecinka i spacji
                    }

                    orders.add("Id Zakupu: " + orderId + " - Sprzęt: " + products);
                }
            }
        }

        return orders;
    }



    public List<String> getClientServiceRequests(int clientId) throws SQLException {
        List<String> requests = new ArrayList<>();

        // Zapytanie pobierające zgłoszenia serwisowe klienta
        String sql = "SELECT z.idserwis, z.sprzet_nr_seryjny, z.pracownik_idpracownik " +
                "FROM zgloszenie_serwisowe z " +
                "WHERE z.klient_idklient = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                int serviceRequestId = resultSet.getInt("idserwis");
                int equipmentSerialNumber = resultSet.getInt("sprzet_nr_seryjny");
                int employeeId = resultSet.getInt("pracownik_idpracownik");

                // Formatowanie zgłoszenia serwisowego
                String requestDetails = "Zgłoszenie serwisowe ID: " + serviceRequestId +
                        ", Sprzęt nr seryjny: " + equipmentSerialNumber +
                        ", Pracownik ID: " + employeeId;

                requests.add(requestDetails);
            }
        }

        return requests;
    }
    public List<String> getClientProducts(int clientId) throws SQLException {
        List<String> products = new ArrayList<>();
        String sql = "SELECT s.nazwa, zhs.nr_seryjny, z.idzakup, zhs.sprzet_idsprzet, z.pracownik_idpracownik FROM zakup z " +
                "JOIN zakup_has_sprzet zhs ON z.idzakup = zhs.zakup_idzakup " +
                "JOIN sprzet s ON zhs.sprzet_idsprzet = s.idsprzet " +
                "WHERE z.klient_idklient = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, clientId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // Formatowanie wyniku z dodatkowymi informacjami, w tym pracownik_idpracownik
                String productInfo = "Nazwa: " + resultSet.getString("nazwa") +
                        ", Numer seryjny: " + resultSet.getInt("nr_seryjny") +
                        ", Zakup ID: " + resultSet.getInt("idzakup") +
                        ", Sprzęt ID: " + resultSet.getInt("sprzet_idsprzet") +
                        ", Pracownik ID: " + resultSet.getInt("pracownik_idpracownik");
                products.add(productInfo);
            }
        }
        return products;
    }

    public int getSerialNumberByProductName(String productName) throws SQLException {
        String sql = "SELECT s.nrseryjny FROM zakup_has_sprzet s WHERE s.nazwa = ? and s.zakup_idzakup = ? and s.sprzet_idsprzet=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, productName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("idsprzet");
            } else {
                throw new SQLException("Nie znaleziono sprzętu o nazwie: " + productName);
            }
        }
    }
    public void addServiceRequest(int workerId,int clientId, int serialNumber) throws SQLException {
        String sql = "INSERT INTO zgloszenie_serwisowe (pracownik_idpracownik,klient_idklient, sprzet_nr_seryjny) " +
                "VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, workerId);
            preparedStatement.setInt(2, clientId);
            preparedStatement.setInt(3, serialNumber);
            preparedStatement.executeUpdate();
        }
    }
    public int getUserIdByUsernameAndRole(String username, String password) throws SQLException {

           String sql = "SELECT idklient FROM klient WHERE login = ? and haslo=? ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2,password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);  // Pierwsza kolumna zawiera ID użytkownika
            } else {
                throw new SQLException("Nie znaleziono użytkownika o podanym username.");
            }
        }
    }


    public int getProductId(String name)
    {
        return 0;
    }

    public void placeOrder(int clientId, List<String> cart, int installments) throws SQLException {


        connection.setAutoCommit(false); // Wyłączamy autokompletowanie

        try {
            // Tworzymy zamówienie w tabeli 'zakup' - zapisywanie ogólnych danych o zamówieniu
            String orderSql = "INSERT INTO zakup (pracownik_idpracownik, klient_idklient) VALUES (?, ?)";
            try (PreparedStatement orderStatement = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStatement.setInt(1, 1); // Przykładowy pracownik ID, można zmienić na dynamiczny
                orderStatement.setInt(2, clientId);
                orderStatement.executeUpdate();

                // Pobieramy ID ostatniego dodanego zamówienia
                ResultSet generatedKeys = orderStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1); // Ostatni wstawiony ID zamówienia

                    // Dodajemy każdy produkt z koszyka do tabeli zakup_has_sprzet
                    for (String product : cart) {
                        String[] parts = product.split(" "); // Dzielimy po ciągu " - "
                        if (parts.length < 1) {
                            throw new SQLException("Nieprawidłowy format produktu: " + product);
                        }

                        int productId =    Integer.parseInt(parts[0]);

                                // Dodajemy produkt do zamówienia
                                String cartSql = "INSERT INTO zakup_has_sprzet (nr_seryjny, zakup_idzakup, sprzet_idsprzet) VALUES (?, ?, ?)";
                                try (PreparedStatement cartStatement = connection.prepareStatement(cartSql)) {
                                    cartStatement.setInt(1,11);
                                    cartStatement.setInt(2, orderId);
                                    cartStatement.setInt(3, productId);
                                    cartStatement.executeUpdate();
                                }


                    }
                } else {
                    throw new SQLException("Nie udało się utworzyć zamówienia - brak ID zamówienia.");
                }

                // Zatwierdzamy transakcję
                connection.commit();
                System.out.println("Zamówienie zostało złożone.");
            } catch (SQLException e) {
                // W przypadku błędu, wycofujemy zmiany
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Błąd przy składaniu zamówienia: " + e.getMessage());
        } finally {
            // Przywracamy autokompletowanie do pierwotnego stanu
            connection.setAutoCommit(true);
        }


    }
    public int getCena(int id) throws SQLException
    {

        String sql = "SELECT cena FROM sprzet WHERE idsprzet = ?";


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);  // Pierwsza kolumna zawiera ID użytkownika
            } else {
                throw new SQLException("Nie znaleziono użytkownika o podanym username.");
            }
        }
    }



}


