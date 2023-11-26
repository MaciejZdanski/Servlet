package org.example.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.util.DatabaseUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "ProductServlet", urlPatterns = {"/product"})
public class ProductServlet extends HttpServlet {


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        Connection connection = null;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        if ("remove".equals(action)) {
            int productIdToRemove = Integer.parseInt(request.getParameter("productId"));

            try {
                connection = DatabaseUtils.getConnection();
                removeProduct(connection, productIdToRemove);
                response.sendRedirect(request.getContextPath() + "/product");
            } catch (SQLException e) {
                handleDatabaseError(e, response);
            } finally {
                closeConnection(connection);
            }
        } else if ("update".equals(action)) {
            int productIdToUpdate = Integer.parseInt(request.getParameter("productId"));
            String updatedName = request.getParameter("updatedName");
            String updatedDescription = request.getParameter("updatedDescription");

            try {
                connection = DatabaseUtils.getConnection();
                updateProduct(connection, productIdToUpdate, updatedName, updatedDescription);
                response.sendRedirect(request.getContextPath() + "/product");
            } catch (SQLException e) {
                handleDatabaseError(e, response);
            } finally {
                closeConnection(connection);
            }
        } else {
            String productName = request.getParameter("productName");
            String productDescription = request.getParameter("productDescription");
            String productPrice = request.getParameter("productPrice");

            try {
                connection = DatabaseUtils.getConnection();
                addProduct(connection, productName, productDescription, productPrice);
            } catch (SQLException e) {
                handleDatabaseError(e, response);
            } finally {
                closeConnection(connection);
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        try (Connection connection = DatabaseUtils.getConnection()) {
            String productName = request.getParameter("product_name");
            if (productName != null && !productName.isEmpty()) {
                retrieveProductByName(connection, productName, response);
                return;
            }

            String productIdParam = request.getParameter("product_id");
            if (productIdParam != null && !productIdParam.isEmpty()) {
                int productId = Integer.parseInt(productIdParam);
                retrieveProductById(connection, productId, response);
                return;
            }

            retrieveAllProducts(connection, response);
        } catch (SQLException | NumberFormatException e) {
            handleDatabaseError(e, response);
        }
    }

    private void retrieveProductByName(Connection connection, String productName, HttpServletResponse response) throws SQLException, IOException {
        String sql = "SELECT * FROM products WHERE product_name = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, productName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                processProductResultSet(resultSet, response);
            }
        }
    }

    private void removeProduct(Connection connection, int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, productId);
            preparedStatement.executeUpdate();
        }
    }

    private void retrieveProductById(Connection connection, int productId, HttpServletResponse response) throws SQLException, IOException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, productId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                processProductResultSet(resultSet, response);
            }
        }
    }

    private void updateProduct(Connection connection, int productId, String updatedName, String updatedDescription) throws SQLException {
        String sql = "UPDATE products SET product_name = ?, product_description = ? WHERE product_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, updatedName);
            preparedStatement.setString(2, updatedDescription);
            preparedStatement.setInt(3, productId);
            preparedStatement.executeUpdate();
        }
    }

    private void handleDatabaseError(Exception e, HttpServletResponse response) throws IOException {
        e.printStackTrace();
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private void addProduct(Connection connection, String productName, String productDescription, String productPrice) throws SQLException {
        String sql = "INSERT INTO products (product_name, product_description, product_price) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, productName);
            preparedStatement.setString(2, productDescription);
            preparedStatement.setDouble(3, Double.parseDouble(productPrice));
            preparedStatement.executeUpdate();
        }
    }

    private void processProductResultSet(ResultSet resultSet, HttpServletResponse response) throws SQLException, IOException {
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>Product Details</h1>");

        while (resultSet.next()) {
            int productId = resultSet.getInt("product_id");
            String productName = resultSet.getString("product_name");
            String productDescription = resultSet.getString("product_description");

            out.println("<p>Product ID: " + productId + "</p>");
            out.println("<p>Name: " + productName + "</p>");
            out.println("<p>Description: " + productDescription + "</p>");
            out.println("<hr>");
        }

        out.println("</body></html>");
    }

    private void retrieveAllProducts(Connection connection, HttpServletResponse response) throws SQLException, IOException {
        String sql = "SELECT * FROM products";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            PrintWriter out = response.getWriter();
            out.println("<html><body>");
            out.println("<h1>Product Details</h1>");

            while (resultSet.next()) {
                int productId = resultSet.getInt("product_id");
                String productName = resultSet.getString("product_name");
                String productDescription = resultSet.getString("product_description");

                out.println("<p>Product ID: " + productId + "</p>");
                out.println("<p>Name: " + productName + "</p>");
                out.println("<p>Description: " + productDescription + "</p>");
                out.println("<hr>");
            }

            out.println("</body></html>");
        }
    }
}