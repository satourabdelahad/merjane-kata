package com.merjane.practice.services.impl;

import com.merjane.practice.entities.Order;
import com.merjane.practice.entities.Product;
import com.merjane.practice.entities.ProductType;
import com.merjane.practice.repositories.OrderRepository;
import com.merjane.practice.repositories.ProductRepository;
import com.merjane.practice.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;

@Service
public class OrderServiceImpl implements OrderService {

    private final Clock clock;
    private final OrderRepository orderRepository;
    private final ProductService productService;

    public OrderServiceImpl(Clock clock,
                            OrderRepository orderRepository,
                            ProductService productService){
        this.clock = clock;
        this.orderRepository = orderRepository;
        this.productService = productService;
    }


    @Override
    public Long processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).get();
        System.out.println(order);
        Set<Product> products = order.getItems();
        for (Product product : products) {

            if (product.getType() == ProductType.NORMAL) {
                if (product.getAvailable() > 0) {
                    product.setAvailable(product.getAvailable() - 1);
                    productService.save(product);
                } else {
                    int leadTime = product.getLeadTime();
                    if (leadTime > 0) {
                        productService.notifyDelay(leadTime, product);
                    }
                }
            } else if (product.getType() == ProductType.SEASONAL) {
                // Add new season rules
                if ((LocalDate.now(clock).isAfter(product.getSeasonStartDate()) && LocalDate.now(clock).isBefore(product.getSeasonEndDate())
                        && product.getAvailable() > 0)) {
                    product.setAvailable(product.getAvailable() - 1);
                    productService.save(product);
                } else {
                    productService.handleSeasonalProduct(product);
                }
            } else if (product.getType() == ProductType.EXPIRABLE) {
                if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now(clock))) {
                    product.setAvailable(product.getAvailable() - 1);
                    productService.save(product);
                } else {
                    productService.handleExpiredProduct(product);
                }
            }
        }
        return order.getId();
    }
}
