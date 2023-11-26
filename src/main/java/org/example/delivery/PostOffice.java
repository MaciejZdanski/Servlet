package org.example.delivery;

public class PostOfficeDelivery implements DeliveryStrategy {
    private static final int DELIVERY_TIME_IN_DAYS = 30;

    @Override
    public void deliver(String customerName) {
        System.out.println("Delivering via post office to " + customerName + " within " + DELIVERY_TIME_IN_DAYS + " days");
    }
}