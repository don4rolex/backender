package com.andrew;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRepositoryTest {

    private static OrderRepository orderRepository;

    @BeforeAll
    static void setup() {
        orderRepository = new OrderRepository(
                5.0,
                0.5,
                1.0,
                "pizza,cake,flamingo",
                0,
                1,
                2,
                3,
                4,
                new CourierRepository());
    }

    @Test
    void findAll() {
        List<Order> orders = orderRepository.findAll();

        assertFalse(orders.isEmpty());

        Order firstOrder = orders.get(0);

        Order expected = new Order().withId("order-1")
                .withDescription("I want a pizza cut into very small slices")
                .withFood(true)
                .withVip(false)
                .withPickup(new Location(41.3965463, 2.1963997))
                .withDelivery(new Location(41.407834, 2.1675979));

        assertEquals(expected, firstOrder);
    }

    @Test
    void findByCourierId_withGlovoBoxAndMotorcyle() {
        List<Order> orders = orderRepository.findByCourierId("courier-1");

        assertFalse(orders.isEmpty());

        assertEquals(5, orders.size());

        Order firstOrder = orders.get(0);

        Order expected = new Order().withId("order-1")
                .withDescription("I want a pizza cut into very small slices")
                .withFood(true)
                .withVip(false)
                .withPickup(new Location(41.3965463, 2.1963997))
                .withDelivery(new Location(41.407834, 2.1675979));

        assertEquals(expected, firstOrder);
    }

    @Test
    void findByCourierId_withoutGlovoBoxAndMotorcyle() {
        List<Order> orders = orderRepository.findByCourierId("courier-3");

        assertFalse(orders.isEmpty());

        assertEquals(2, orders.size());

        Order firstOrder = orders.get(0);
        Order secondOrder = orders.get(1);

        Order firstExpected = new Order().withId("order-5")
                .withDescription("Hot dog")
                .withFood(true)
                .withVip(false)
                .withPickup(new Location(41.40638606893252,2.166255699226159))
                .withDelivery(new Location(41.40052857611856,2.17474693857396));

        Order secondExpected = new Order().withId("order-3")
                .withDescription("Envelope")
                .withFood(false)
                .withVip(true)
                .withPickup(new Location(41.37790607439139,2.1801331715968426))
                .withDelivery(new Location(41.480661712089115,2.1760928408928155));

        assertEquals(firstExpected, firstOrder);
        assertEquals(secondExpected, secondOrder);
    }

    @Test
    void findByCourierId_withGlovoBoxAndBicycle() {
        List<Order> orders = orderRepository.findByCourierId("courier-6");

        assertFalse(orders.isEmpty());

        assertEquals(4, orders.size());

        Order firstOrder = orders.get(0);

        Order expected = new Order().withId("order-2")
                .withDescription("1x Hot dog with Fries\n2x Kebab with Fries\nChocolate cake")
                .withFood(true)
                .withVip(false)
                .withPickup(new Location(41.38412925150105, 2.1870953755511464))
                .withDelivery(new Location(41.39265932307547, 2.1743998837459806));

        assertEquals(expected, firstOrder);
    }

    @Test
    void findByCourierId_withoutGlovoBoxAndBicycle() {
        List<Order> orders = orderRepository.findByCourierId("courier-2");

        assertFalse(orders.isEmpty());

        assertEquals(1, orders.size());

        Order firstOrder = orders.get(0);

        Order expected = new Order().withId("order-5")
                .withDescription("Hot dog")
                .withFood(true)
                .withVip(false)
                .withPickup(new Location(41.40638606893252,2.166255699226159))
                .withDelivery(new Location(41.40052857611856,2.17474693857396));

        assertEquals(expected, firstOrder);
    }

    @Test
    void findByCourierId_updatedGlovoBoxWords() {
        OrderRepository orderRepository = new OrderRepository(
                5.0,
                0.5,
                1.0,
                "pizza,cake,flamingo,hot dog",
                0,
                1,
                2,
                3,
                4,
                new CourierRepository());

        List<Order> orders = orderRepository.findByCourierId("courier-2");

        assertTrue(orders.isEmpty());
    }

    @Test
    void findByCourierId_updatedOrderPriority() {
        OrderRepository orderRepository = new OrderRepository(
                5.0,
                0.5,
                1.0,
                "pizza,cake,flamingo,hot dog",
                4,
                1,
                2,
                3,
                0,
                new CourierRepository());

        List<Order> orders = orderRepository.findByCourierId("courier-1");

        assertFalse(orders.isEmpty());

        assertEquals(5, orders.size());

        Order firstOrder = orders.get(0);

        Order expected = new Order().withId("order-3")
                .withDescription("Envelope")
                .withFood(false)
                .withVip(true)
                .withPickup(new Location(41.37790607439139,2.1801331715968426))
                .withDelivery(new Location(41.480661712089115,2.1760928408928155));

        assertEquals(expected, firstOrder);
    }
}