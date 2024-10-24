package za.ac.cput.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import za.ac.cput.domain.*;
import za.ac.cput.factory.OrderFactory;
import za.ac.cput.factory.OrderItemFactory;
import za.ac.cput.service.AddressService;
import za.ac.cput.service.OrderService;
import za.ac.cput.service.ProductService;
import za.ac.cput.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderControllerTest {

    @Autowired
    private OrderController orderController;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private ProductService productService;

    private Orders order;
    private User user;
    private Address address;
    private Product product;

    @BeforeEach
    void setUp() {
        user = userService.read(1L);
        address = addressService.read(1L);
        product = productService.read(1L);

        // Create an initial order without items
        order = OrderFactory.buildOrder(
                1L,
                user,
                address,
                "Pending",
                150.0,
                new ArrayList<>()
        );

        // Save the order to generate an orderID
        order = orderService.create(order);

        // Create test OrderItems for the saved order
        OrderItem orderItem1 = OrderItemFactory.buildOrderItem(
                null,
                product,
                12,
                12.00,
                order
        );

        OrderItem orderItem2 = OrderItemFactory.buildOrderItem(
                null,
                product,
                5,
                10.00,
                order
        );

        OrderItem orderItem3 = OrderItemFactory.buildOrderItem(
                null,
                product,
                20,
                20.00,
                order
        );

        List<OrderItem> orderItems = new ArrayList<>();
        orderItems.add(orderItem1);
        orderItems.add(orderItem2);
        orderItems.add(orderItem3);

        // Update the order with OrderItems and save it
        order = new Orders.Builder()
                .setId(order.getId())
                .setUser(order.getUser())
                .setAddress(order.getAddress())
                .setTotalPrice(order.getTotalPrice())
                .setStatus(order.getStatus())
                .setOrderDate(order.getOrderDate())
                .setOrderItems(orderItems)
                .build();

        orderService.update(order);
    }

    @AfterEach
    void tearDown() {
        // Cleanup: Delete test order
        if (order != null && order.getId() != null) {
            orderService.deleteByOrderID(order.getId());
        }
    }

    @Test
    @Order(1)
    void createOrder() {
        // Arrange
        List<OrderItem> orderItems = List.of(
            OrderItemFactory.buildOrderItem(null, product, 12, 12.00, null),
            OrderItemFactory.buildOrderItem(null, product, 5, 10.00, null),
            OrderItemFactory.buildOrderItem(null, product, 20, 20.00, null)
        );

        Orders newOrder = OrderFactory.buildOrder(
                null,
                user,
                address,
                "pending",
                234,
                orderItems
        );

        // Act
        ResponseEntity<Orders> response = orderController.createOrder(newOrder);

        // Assert
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newOrder.getTotalPrice(), response.getBody().getTotalPrice());
        System.out.println(response.getBody());

        // Cleanup
        orderService.deleteByOrderID(response.getBody().getId());
    }

    @Test
    @Order(2)
    void read() {
        // Act
        ResponseEntity<Orders> response = orderController.read(order.getId());
        System.out.println(response.getBody());

        // Assert
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(order.getId(), response.getBody().getId());
    }

    @Test
    @Order(3)
    void updateOrder() {
        // Arrange
        Orders updatedOrder = new Orders.Builder()
                .copy(order)
                .setTotalPrice(200.0)
                .build();

        // Act
        ResponseEntity<Orders> response = orderController.updateOrder(order.getId(), updatedOrder);
        System.out.println(response.getBody());
        // Assert
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200.0, response.getBody().getTotalPrice());
    }

    @Test
    @Order(4)
    void deleteOrder() {
        System.out.println((order.getId()));
        // Act
        ResponseEntity<Void> response = orderController.deleteOrder(order.getId());

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify that the order is deleted by trying to read it
        ResponseEntity<Orders> readResponse = orderController.read(order.getId());
        assertNull(readResponse.getBody());
    }

    @Test
    @Order(5)
    void getAllOrders() {
        // Act
        ResponseEntity<List<Orders>> response = orderController.getAllOrders();
        System.out.println(response.getBody());

        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(6)
    void getOrdersByUserID() {
        // Act
        ResponseEntity<List<Orders>> response = orderController.getOrdersByUserID(order.getUser().getId());

        System.out.println(response.getBody());

        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(7)
    void getOrdersByStatus() {
        // Act
        ResponseEntity<List<Orders>> response = orderController.getOrdersByStatus("Pending");
        System.out.println(response.getBody());
        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(8)
    void getOrdersByDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        // Act
        ResponseEntity<List<Orders>> response = orderController.getOrdersByDateRange(startDate, endDate);
        System.out.println(response.getBody());
        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(9)
    void getOrdersByAddressID() {
        // Act
        ResponseEntity<List<Orders>> response = orderController.getOrdersByAddressID(order.getAddress().getId());
        System.out.println(response.getBody());

        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(10)
    void getOrdersByTotalPriceGreaterThan() {
        // Act
        ResponseEntity<List<Orders>> response = orderController.getOrdersByTotalPriceGreaterThan(50.0);
        System.out.println(response.getBody());

        // Assert
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
