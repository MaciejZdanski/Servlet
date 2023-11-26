package org.example.models;

public class Order {
    private int orderId;
    private String customerName;
    private String orderDescription;
    private double orderPrice;
    private String productName;

    public Order(int orderId, String customerName, String orderDescription, double orderPrice, String productName) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.orderDescription = orderDescription;
        this.orderPrice = orderPrice;
        this.productName = productName;
    }


    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getOrderDescription() {
        return orderDescription;
    }

    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
    }

    public double getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(double orderPrice) {
        this.orderPrice = orderPrice;
    }
    public void productName(String productName) {
        this.productName = productName;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                ", orderDescription='" + orderDescription + '\'' +
                ", orderPrice=" + orderPrice +
                ", productName=" + productName +
                '}';
    }

}
