package org.example.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.delivery.CourierDelivery;
import org.example.delivery.DeliveryStrategy;
import org.example.delivery.PostOfficeDelivery;
import org.example.util.DatabaseUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "OrderServlet", urlPatterns = "/order")
public class OrderServlet extends HttpServlet {

    private DeliveryStrategy deliveryStrategy;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        Connection connection = null;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        if ("remove".equals(action)) {
            int orderIdToRemove = Integer.parseInt(request.getParameter("orderId"));

            try {
                connection = DatabaseUtils.getConnection();
                removeOrder(connection, orderIdToRemove);
                response.sendRedirect(request.getContextPath() + "/order");
            } catch (SQLException e) {
                handleDatabaseError(e, response);
            } finally {
                closeConnection(connection);
            }
        } else if ("update".equals(action)) {
            int orderIdToUpdate = Integer.parseInt(request.getParameter("orderId"));
            String updatedCustomerName = request.getParameter("updatedCustomerName");
            String updatedCustomerEmail = request.getParameter("updatedCustomerEmail");

            try {
                connection = DatabaseUtils.getConnection();
                updateOrder(connection, orderIdToUpdate, updatedCustomerName, updatedCustomerEmail);
                response.sendRedirect(request.getContextPath() + "/order");
            } catch (SQLException e) {
                handleDatabaseError(e, response);
            } finally {
                closeConnection(connection);
            }
        } else {
            String productName = request.getParameter("productName");
            String customerName = request.getParameter("customerName");
            String customerEmail = request.getParameter("customerEmail");
            String deliveryService = request.getParameter("deliveryService");

            try {
                connection = DatabaseUtils.getConnection();
                String sql = "INSERT INTO orders (customer_name, customer_email, product_id, delivery_service) VALUES (?, ?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, customerName);
                    preparedStatement.setString(2, customerEmail);
                    int productId = getProductIdByName(connection, productName);
                    preparedStatement.setInt(3, productId);
                    preparedStatement.setString(4, deliveryService);
                    preparedStatement.executeUpdate();

                    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                    int orderId = -1;
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    }
                    if ("Courier".equalsIgnoreCase(deliveryService)) {
                        deliveryStrategy = new CourierDelivery();
                    } else if ("PostOffice".equalsIgnoreCase(deliveryService)) {
                        deliveryStrategy = new PostOfficeDelivery();
                    }

                    if (deliveryStrategy != null) {
                        deliveryStrategy.deliver(customerName);
                        updateOrderWithDeliveryService(connection, orderId, deliveryService);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error placing the order");
            } finally {
                closeConnection(connection);
            }
        }
    }

    private void updateOrderWithDeliveryService(Connection connection, int orderId, String deliveryService) throws SQLException {
        String updateSql = "UPDATE orders SET delivery_service = ? WHERE order_id = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
            updateStatement.setString(1, deliveryService);
            updateStatement.setInt(2, orderId);
            updateStatement.executeUpdate();
        }
    }

    private void removeOrder(Connection connection, int orderId) throws SQLException {
        String sql = "DELETE FROM orders WHERE order_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, orderId);
            preparedStatement.executeUpdate();
        }
    }

    private void updateOrder(Connection connection, int orderId, String updatedCustomerName, String updatedCustomerEmail) throws SQLException {
        String sql = "UPDATE orders SET customer_name = ?, customer_email = ? WHERE order_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, updatedCustomerName);
            preparedStatement.setString(2, updatedCustomerEmail);
            preparedStatement.setInt(3, orderId);
            preparedStatement.executeUpdate();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        try (Connection connection = DatabaseUtils.getConnection()) {
            String customerEmail = request.getParameter("customer_email");
            if (customerEmail != null && !customerEmail.isEmpty()) {
                retrieveOrdersByCustomerEmail(connection, customerEmail, response);
                return;
            }

            String orderIdParam = request.getParameter("order_id");
            if (orderIdParam != null && !orderIdParam.isEmpty()) {
                int orderId = Integer.parseInt(orderIdParam);
                retrieveOrderById(connection, orderId, response);
                return;
            }

            retrieveAllOrders(connection, response);
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving order details");
        }
    }

    private void retrieveOrdersByCustomerEmail(Connection connection, String customerEmail, HttpServletResponse response) throws SQLException, IOException {
        String sql = "SELECT * FROM orders WHERE customer_email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, customerEmail);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                processResultSet(resultSet, response);
            }
        }
    }

    private void retrieveOrderById(Connection connection, int orderId, HttpServletResponse response) throws SQLException, IOException {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, orderId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                processResultSet(resultSet, response);
            }
        }
    }

    private void retrieveAllOrders(Connection connection, HttpServletResponse response) throws SQLException, IOException {
        String sql = "SELECT * FROM orders";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            processResultSet(resultSet, response);
        }
    }

    private int getProductIdByName(Connection connection, String productName) {
        int productId = -1;

        try {
            String sql = "SELECT product_id FROM products WHERE product_name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, productName);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        productId = resultSet.getInt("product_id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productId;
    }

    private void handleDatabaseError(Exception e, HttpServletResponse response) throws IOException {
        e.printStackTrace();
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error: " + e.getMessage());
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
        }
    }

    private void processResultSet(ResultSet resultSet, HttpServletResponse response) throws SQLException, IOException {
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>Order Details</h1>");

        while (resultSet.next()) {
            int orderId = resultSet.getInt("order_id");
            String customerName = resultSet.getString("customer_name");
            String customerEmail = resultSet.getString("customer_email");
            int productId = resultSet.getInt("product_id");

            out.println("<p>Order ID: " + orderId + "</p>");
            out.println("<p>Customer: " + customerName + " (" + customerEmail + ")</p>");
            out.println("<p>Product ID: " + productId + "</p>");
            out.println("<hr>");
        }

        out.println("</body></html>");
    }
}