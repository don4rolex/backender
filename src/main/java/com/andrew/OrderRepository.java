package com.andrew;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static com.andrew.DistanceCalculator.calculateDistance;
import static com.andrew.Vehicle.ELECTRIC_SCOOTER;
import static com.andrew.Vehicle.MOTORCYCLE;

@Component
class OrderRepository {
    private static final String ORDERS_FILE = "/orders.json";
    private static final List<Order> orders;

    private final CourierRepository courierRepository;
    private final Double longDeliveryDistance;
    private final Double shortPickupDistance;
    private final Double longPickupDistance;
    private final String glovoBoxWords;
    private final Integer shortPickupDistanceOrderPriority;
    private final Integer longPickupDistanceOrderPriority;
    private final Integer vipOrderPriority;
    private final Integer foodOrderPriority;
    private final Integer othersOrderPriority;

    static {
        try (Reader reader = new InputStreamReader(OrderRepository.class.getResourceAsStream(ORDERS_FILE))) {
            Type type = new TypeToken<List<Order>>() {
            }.getType();
            orders = new Gson().fromJson(reader, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    OrderRepository(@Value("${courier.deliver.long.distance}") Double longDeliveryDistance,
                    @Value("${courier.pickup.short.distance}") Double shortPickupDistance,
                    @Value("${courier.pickup.long.distance}") Double longPickupDistance,
                    @Value("${glovo.box.words}") String glovoBoxWords,
                    @Value("${short.pickup.distance.order.priority}") Integer shortPickupDistanceOrderPriority,
                    @Value("${long.pickup.distance.order.priority}") Integer longPickupDistanceOrderPriority,
                    @Value("${vip.order.priority}") Integer vipOrderPriority,
                    @Value("${food.order.priority}") Integer foodOrderPriority,
                    @Value("${others.order.priority}") Integer othersOrderPriority,
                    CourierRepository courierRepository) {
        this.longDeliveryDistance = longDeliveryDistance;
        this.shortPickupDistance = shortPickupDistance;
        this.longPickupDistance = longPickupDistance;
        this.glovoBoxWords = glovoBoxWords;
        this.shortPickupDistanceOrderPriority = shortPickupDistanceOrderPriority;
        this.longPickupDistanceOrderPriority = longPickupDistanceOrderPriority;
        this.vipOrderPriority = vipOrderPriority;
        this.foodOrderPriority = foodOrderPriority;
        this.othersOrderPriority = othersOrderPriority;
        this.courierRepository = courierRepository;
    }

    List<Order> findAll() {
        return new ArrayList<>(orders);
    }

    List<Order> findByCourierId(String courierId) {
        Courier courier = courierRepository.findById(courierId);
        Location courierLocation = courier.getLocation();

        List<Order> filteredOrders = getFilteredOrders(courier);
        List<Order> shortPickupDistanceOrders = getShortPickupDistanceOrders(filteredOrders, courierLocation);
        List<Order> longPickupDistanceOrders = getLongPickupDistanceOrders(filteredOrders, courierLocation);
        List<Order> vipOrders = getVipOrders(filteredOrders, courierLocation);
        List<Order> foodOrders = getFoodOrders(filteredOrders, courierLocation);
        List<Order> otherOrders = getOtherOrders(filteredOrders, courierLocation);

        List<Order> orderPriorities = getOrderPriorities(shortPickupDistanceOrders, longPickupDistanceOrders, vipOrders,
                foodOrders, otherOrders);

        return new ArrayList<>(orderPriorities);
    }

    private List<Order> getFilteredOrders(Courier courier) {
        String[] glovoBoxKeywords = glovoBoxWords.toLowerCase().split(",");
        List<Vehicle> allowedVehicles = Arrays.asList(MOTORCYCLE, ELECTRIC_SCOOTER);

        return orders.stream()
                .filter(order -> {
                    String description = order.getDescription().toLowerCase();
                    if (!courier.getBox()) {
                        return Arrays.stream(glovoBoxKeywords).noneMatch(description::contains);
                    }

                    return true;
                })
                .filter(order -> {
                    Location pickupLocation = order.getPickup();
                    Location deliveryLocation = order.getDelivery();
                    Double distance = calculateDistance(pickupLocation, deliveryLocation);

                    if (distance > longDeliveryDistance) {
                        return allowedVehicles.contains(courier.getVehicle());
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<Order> getShortPickupDistanceOrders(List<Order> filteredOrders, Location courierLocation) {
        return filteredOrders.stream()
                .filter(order -> {
                    Location pickupLocation = order.getPickup();
                    Double distance = calculateDistance(courierLocation, pickupLocation);

                    return distance <= shortPickupDistance;
                }).collect(Collectors.toList());
    }

    private List<Order> getLongPickupDistanceOrders(List<Order> filteredOrders, Location courierLocation) {
        return filteredOrders.stream()
                .filter(order -> {
                    Location pickupLocation = order.getPickup();
                    Double distance = calculateDistance(courierLocation, pickupLocation);

                    return (distance > shortPickupDistance) && (distance <= longPickupDistance);
                }).collect(Collectors.toList());
    }

    private List<Order> getVipOrders(List<Order> filteredOrders, Location courierLocation) {
        return filteredOrders.stream()
                .filter(order -> {
                    Location pickupLocation = order.getPickup();
                    Double distance = calculateDistance(courierLocation, pickupLocation);

                    return (distance > longPickupDistance) && (order.getVip());
                }).collect(Collectors.toList());
    }

    private List<Order> getFoodOrders(List<Order> filteredOrders, Location courierLocation) {
        return filteredOrders.stream()
                .filter(order -> {
                    Location pickupLocation = order.getPickup();
                    Double distance = calculateDistance(courierLocation, pickupLocation);

                    return (distance > longPickupDistance) && (!order.getVip()) && (order.getFood());
                }).collect(Collectors.toList());
    }

    private List<Order> getOtherOrders(List<Order> filteredOrders, Location courierLocation) {
        return filteredOrders.stream()
                .filter(order -> {
                    Location pickupLocation = order.getPickup();
                    Double distance = calculateDistance(courierLocation, pickupLocation);

                    return (distance > longPickupDistance) && (!order.getVip()) && (!order.getFood());
                }).collect(Collectors.toList());
    }

    private List<Order> getOrderPriorities(List<Order> shortPickupDistanceOrders, List<Order> longPickupDistanceOrders,
                                           List<Order> vipOrders, List<Order> foodOrders, List<Order> otherOrders) {

        List<Order> priorityList = new ArrayList<>();

        Map<Integer, List<Order>> priorityMap = new TreeMap<>();
        priorityMap.put(shortPickupDistanceOrderPriority, shortPickupDistanceOrders);
        priorityMap.put(longPickupDistanceOrderPriority, longPickupDistanceOrders);
        priorityMap.put(vipOrderPriority, vipOrders);
        priorityMap.put(foodOrderPriority, foodOrders);
        priorityMap.put(othersOrderPriority, otherOrders);

        priorityMap.keySet().forEach(index -> priorityList.addAll(priorityMap.get(index)));

        return priorityList;
    }
}
