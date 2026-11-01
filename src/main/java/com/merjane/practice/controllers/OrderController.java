package com.merjane.practice.controllers;

import com.merjane.practice.dtos.ProcessOrderResponse;
import com.merjane.practice.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("{orderId}/processOrder")
    @ResponseStatus(HttpStatus.OK)
    public ProcessOrderResponse processOrder(@PathVariable Long orderId) {

        return new ProcessOrderResponse(orderService.processOrder(orderId));
    }


}
