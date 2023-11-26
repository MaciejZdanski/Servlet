package org.example.Servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

@WebServlet(name = "ProductServlet", urlPatterns = {"/product"})
public class ProductServlet extends HttpServlet {

    private static final String JSON_FILE_PATH = "src\\main\\java\\org\\example\\products.json";
    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Read and return products from JSON
        List<Product> products = readProductsFromFile();
        sendJsonResponse(response, products);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Add a new product to JSON
        Product product = objectMapper.readValue(request.getInputStream(), Product.class);
        List<Product> products = readProductsFromFile();
        products.add(product);
        writeProductsToFile(products);
        sendJsonResponse(response, product);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Update an existing product in JSON
        Product updatedProduct = objectMapper.readValue(request.getInputStream(), Product.class);
        List<Product> products = readProductsFromFile();
        updateProductInList(products, updatedProduct);
        writeProductsToFile(products);
        sendJsonResponse(response, updatedProduct);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Delete a product from JSON
        String productId = request.getParameter("productId");
        List<Product> products = readProductsFromFile();
        deleteProductFromList(products, productId);
        writeProductsToFile(products);
        response.getWriter().write("Product deleted successfully");
    }

    private List<Product> readProductsFromFile() throws IOException {
        // Read the JSON file and return the list of products
        File file = new File(JSON_FILE_PATH);
        if (file.exists()) {
            return Arrays.asList(objectMapper.readValue(file, Product[].class));
        } else {
            return new ArrayList<>();
        }
    }

    private void writeProductsToFile(List<Product> products) throws IOException {
        // Write the updated list of products to the JSON file
        objectMapper.writeValue(new File(JSON_FILE_PATH), products);
    }

    private void updateProductInList(List<Product> products, Product updatedProduct) {
        // Find and update the product in the list
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getProductId() == updatedProduct.getProductId()) {
                products.set(i, updatedProduct);
                break;
            }
        }
    }

    private void deleteProductFromList(List<Product> products, String productId) {
        // Remove the product from the list
        products.removeIf(p -> p.getProductId() == Integer.parseInt(productId));
    }

    private void sendJsonResponse(HttpServletResponse response, Object object) throws IOException {
        // Send a JSON response
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(object));
    }
}