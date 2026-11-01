package com.merjane.practice.controllers;

import com.merjane.practice.entities.Order;
import com.merjane.practice.entities.Product;
import com.merjane.practice.repositories.OrderRepository;
import com.merjane.practice.repositories.ProductRepository;
import com.merjane.practice.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MyControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    static Clock clock;

    @Test
    void should_decrement_stock_when_normal_product_is_in_stock() throws Exception {
        // GIVEN : un seul produit NORMAL avec 30 en stock
        Product product = productRepository.save(
                new Product(null, 15, 30, "NORMAL", "USB Cable", null, null, null));
        Order order = orderRepository.save(createOrder(Set.of(product)));

        // WHEN
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // THEN
        Product result = productRepository.findById(product.getId()).get();

        assertEquals(29, result.getAvailable());

        Mockito.verifyNoInteractions(notificationService);
        //          (indice : Mockito.verifyNoInteractions)
    }

    // ---------- NORMAL ----------

    @Test
    void should_notify_delay_when_normal_product_is_out_of_stock() throws Exception {
        Product product = productRepository.save(
                new Product(null, 10, 0, "NORMAL", "USB Dongle", null, null, null));

        Product result = processSingleProductOrder(product);

        assertEquals(0, result.getAvailable());
        assertEquals(10, result.getLeadTime());
        Mockito.verify(notificationService).sendDelayNotification(10, "USB Dongle");
        Mockito.verifyNoMoreInteractions(notificationService);
    }

    @Test
    void should_do_nothing_when_normal_product_is_out_of_stock_and_lead_time_is_zero() throws Exception {
        // Comportement legacy : aucune notification si leadTime = 0
        Product product = productRepository.save(
                new Product(null, 0, 0, "NORMAL", "USB Hub", null, null, null));

        Product result = processSingleProductOrder(product);

        assertEquals(0, result.getAvailable());
        Mockito.verifyNoInteractions(notificationService);
    }

    // ---------- SEASONAL ----------

    @Test
    void should_decrement_stock_when_seasonal_product_is_in_season_and_in_stock() throws Exception {
        Product product = productRepository.save(new Product(null, 15, 30, "SEASONAL", "Watermelon",
                null, LocalDate.now(clock).minusDays(2), LocalDate.now(clock).plusDays(58)));

        Product result = processSingleProductOrder(product);

        assertEquals(29, result.getAvailable());
        Mockito.verifyNoInteractions(notificationService);
    }

    @Test
    void should_notify_delay_when_seasonal_product_is_out_of_stock_and_restock_is_within_season() throws Exception {
        Product product = productRepository.save(new Product(null, 15, 0, "SEASONAL", "Watermelon",
                null, LocalDate.now(clock).minusDays(2), LocalDate.now(clock).plusDays(58)));

        Product result = processSingleProductOrder(product);

        assertEquals(0, result.getAvailable());
        Mockito.verify(notificationService).sendDelayNotification(15, "Watermelon");
        Mockito.verifyNoMoreInteractions(notificationService);
    }

    @Test
    void should_notify_out_of_stock_when_seasonal_product_restock_exceeds_season_end() throws Exception {
        Product product = productRepository.save(new Product(null, 30, 0, "SEASONAL", "Strawberry",
                null, LocalDate.now(clock).minusDays(2), LocalDate.now(clock).plusDays(10)));

        Product result = processSingleProductOrder(product);

        assertEquals(0, result.getAvailable());
        Mockito.verify(notificationService).sendOutOfStockNotification("Strawberry");
        Mockito.verifyNoMoreInteractions(notificationService);
    }

    @Test
    void should_notify_out_of_stock_and_keep_stock_when_season_has_not_started() throws Exception {
        Product product = productRepository.save(new Product(null, 15, 30, "SEASONAL", "Grapes",
                null, LocalDate.now(clock).plusDays(180), LocalDate.now(clock).plusDays(240)));

        Product result = processSingleProductOrder(product);

        assertEquals(30, result.getAvailable());
        Mockito.verify(notificationService).sendOutOfStockNotification("Grapes");
        Mockito.verifyNoMoreInteractions(notificationService);
    }

    @Test
    void should_notify_delay_instead_of_selling_on_season_start_day() throws Exception {
        // Comportement legacy (bug probable) : bornes strictes isAfter/isBefore,
        // le jour même du début de saison le produit n'est pas vendu.
        Product product = productRepository.save(new Product(null, 15, 30, "SEASONAL", "Cherries",
                null, LocalDate.now(clock), LocalDate.now(clock).plusDays(60)));

        Product result = processSingleProductOrder(product);

        assertEquals(30, result.getAvailable());
        Mockito.verify(notificationService).sendDelayNotification(15, "Cherries");
        Mockito.verifyNoMoreInteractions(notificationService);
    }

    @Test
    void should_notify_out_of_stock_and_empty_stock_on_season_end_day() throws Exception {
        // Comportement legacy (bug probable) : borne stricte isBefore, le dernier jour de saison
        // le produit n'est pas vendu, et comme now + leadTime dépasse la fin de saison,
        // le stock restant (30) est remis à 0.
        Product product = productRepository.save(new Product(null, 15, 30, "SEASONAL", "Melon",
                null, LocalDate.now(clock).minusDays(10), LocalDate.now(clock)));

        Product result = processSingleProductOrder(product);

        assertEquals(0, result.getAvailable());
        Mockito.verify(notificationService).sendOutOfStockNotification("Melon");
        Mockito.verifyNoMoreInteractions(notificationService);
    }

    // ---------- EXPIRABLE ----------

    @Test
    void should_decrement_stock_when_expirable_product_is_not_expired_and_in_stock() throws Exception {
        Product product = productRepository.save(new Product(null, 15, 30, "EXPIRABLE", "Butter",
                LocalDate.now(clock).plusDays(26), null, null));

        Product result = processSingleProductOrder(product);

        assertEquals(29, result.getAvailable());
        Mockito.verifyNoInteractions(notificationService);
    }

    @Test
    void should_notify_expiration_and_empty_stock_when_expirable_product_is_expired() throws Exception {
        LocalDate expiryDate = LocalDate.now(clock).minusDays(2);
        Product product = productRepository.save(new Product(null, 90, 6, "EXPIRABLE", "Milk",
                expiryDate, null, null));

        Product result = processSingleProductOrder(product);

        assertEquals(0, result.getAvailable());
        Mockito.verify(notificationService).sendExpirationNotification("Milk", expiryDate);
        Mockito.verifyNoMoreInteractions(notificationService);
    }

    @Test
    void should_notify_expiration_when_expirable_product_is_not_expired_but_out_of_stock() throws Exception {
        // Comportement legacy (bug probable) : une rupture de stock est signalée comme une expiration.
        LocalDate expiryDate = LocalDate.now(clock).plusDays(10);
        Product product = productRepository.save(new Product(null, 5, 0, "EXPIRABLE", "Yogurt",
                expiryDate, null, null));

        Product result = processSingleProductOrder(product);

        assertEquals(0, result.getAvailable());
        Mockito.verify(notificationService).sendExpirationNotification("Yogurt", expiryDate);
        Mockito.verifyNoMoreInteractions(notificationService);
    }

    @Test
    void should_consider_expirable_product_as_expired_on_expiry_day() throws Exception {
        // Comportement legacy : borne stricte isAfter, le jour de la date d'expiration
        // le produit est considéré comme expiré (stock 10 remis à 0).
        LocalDate expiryDate = LocalDate.now(clock);
        Product product = productRepository.save(new Product(null, 15, 10, "EXPIRABLE", "Cheese",
                expiryDate, null, null));

        Product result = processSingleProductOrder(product);

        assertEquals(0, result.getAvailable());
        Mockito.verify(notificationService).sendExpirationNotification("Cheese", expiryDate);
        Mockito.verifyNoMoreInteractions(notificationService);
    }

    private Product processSingleProductOrder(Product product) throws Exception {
        Order order = orderRepository.save(createOrder(Set.of(product)));
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        return productRepository.findById(product.getId()).get();
    }

    @Test
    void processOrderShouldReturn() throws Exception {
        List<Product> allProducts = createProducts();
        Set<Product> orderItems = new HashSet<>(allProducts);
        Order order = createOrder(orderItems);
        productRepository.saveAll(allProducts);
        order = orderRepository.save(order);

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Order resultOrder = orderRepository.findById(order.getId()).get();
        assertEquals(resultOrder.getId(), order.getId());
        // TODO: assert on product stock and notifications
    }

    private static Order createOrder(Set<Product> products) {
        Order order = new Order();
        order.setItems(products);
        return order;
    }

    private static List<Product> createProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product(null, 15, 30, "NORMAL", "USB Cable", null, null, null));
        products.add(new Product(null, 10, 0, "NORMAL", "USB Dongle", null, null, null));
        products.add(new Product(null, 15, 30, "EXPIRABLE", "Butter", LocalDate.now(clock).plusDays(26), null, null));
        products.add(new Product(null, 90, 6, "EXPIRABLE", "Milk", LocalDate.now(clock).minusDays(2), null, null));
        products.add(new Product(null, 15, 30, "SEASONAL", "Watermelon", null, LocalDate.now(clock).minusDays(2), LocalDate.now(clock).plusDays(58)));
        products.add(new Product(null, 15, 30, "SEASONAL", "Grapes", null, LocalDate.now(clock).plusDays(180), LocalDate.now(clock).plusDays(240)));
        return products;
    }
}
