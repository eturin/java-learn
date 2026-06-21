package ture;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ProcessDeliveryOrders {

    private static Stream<DeliveryOrder> stream(List<DeliveryOrder> orders) {
        return orders.stream()
                     .sorted(DeliveryOrder.getComparatorByDeliveryDate())
                     .filter(new HashSet<>()::add);
    }

    public static DeliveryOrder findFirstOrder(List<DeliveryOrder> orders) {
        return stream(orders).findFirst().orElse(new DeliveryOrder());
    }

    public static void printAddressesToDeliver(List<DeliveryOrder> orders) {
        stream(orders)
                .map(DeliveryOrder::getAddress)
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);

        List<DeliveryOrder> orders = List.of(
                new DeliveryOrder(1L, "112 Mammoth Street, Colorado Springs, CO 80911", LocalDate.of(2021, 9, 3)),
                new DeliveryOrder(2L, "369 Woodside Court, Troy, NY 12180", LocalDate.of(2021, 9, 5)),
                new DeliveryOrder(3L, "837 Bowman Street, Helena, MT 59601", LocalDate.of(2021, 9, 2)),
                new DeliveryOrder(4L, "112 Mammoth Street, Colorado Springs, CO 80911", LocalDate.of(2021, 9, 3))
        );

        System.out.println(findFirstOrder(orders));
        System.out.println();
        printAddressesToDeliver(orders);
    }
}

class DeliveryOrder {
    private final long orderId;
    private final String address;
    private final LocalDate deliveryDate;
    // there are even more fields: customer name, phone, products info, etc

    public DeliveryOrder() {
        this.orderId = -1;
        this.address = "No address";
        this.deliveryDate = LocalDate.MIN;
    }

    public DeliveryOrder(long orderId, String address, LocalDate deliveryDate) {
        this.orderId = orderId;
        this.address = address;
        this.deliveryDate = deliveryDate;
    }

    public static Comparator<DeliveryOrder> getComparatorByDeliveryDate() {
        return Comparator.comparing(DeliveryOrder::getDeliveryDate);
    }

    public long getOrderId() {
        return orderId;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getDeliveryDate() {
        return deliveryDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeliveryOrder that = (DeliveryOrder) o;
        return address.equals(that.address) &&
                deliveryDate.equals(that.deliveryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, deliveryDate);
    }

    @Override
    public String toString() {
        return orderId + "|" + deliveryDate + "|" + address;
    }
}