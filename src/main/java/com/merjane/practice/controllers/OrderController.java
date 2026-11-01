package com.merjane.practice.controllers;

import com.merjane.practice.dtos.ProcessOrderResponse;
import com.merjane.practice.entities.Order;
import com.merjane.practice.entities.Product;
import com.merjane.practice.repositories.OrderRepository;
import com.merjane.practice.repositories.ProductRepository;
import com.merjane.practice.services.OrderService;
import com.merjane.practice.services.impl.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Set;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository pr;

    @Autowired
    private OrderRepository or;

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {

        return new ProcessOrderResponse(orderService.processOrder(orderId));
    }


}
