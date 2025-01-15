import java.util.List;

public interface ShopViewInterface {
    void updateProductList(List<String> products);
    void showMessage(String message);
    void closeWindow();
    void showProductDetails(String productDetails);
}
