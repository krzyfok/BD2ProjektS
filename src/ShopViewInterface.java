import java.util.List;

public interface ShopViewInterface {
    void displayProducts(List<String> products);
    void showProductDetails(String details);
    void showMessage(String message);
    void closeWindow();
}
