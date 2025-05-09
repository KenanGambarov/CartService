package com.cartservice.queue;

import com.cartservice.dto.request.queue.StockRequestDto;
import com.cartservice.exception.QueueException;
import com.cartservice.services.CartService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockListener {

    private final static String QUEUE_NAME="PRODUCT_UPDATE";
    private final ObjectMapper objectMapper;
    private final CartService cartService;


    @RabbitListener(queues = QUEUE_NAME)
    public void consume(String message){
        try {
            var data = objectMapper.readValue(message, StockRequestDto.class);
            cartService.removeOutOfStockItems(data);
        } catch (JsonProcessingException e) {
            log.error("Consume message invalid format: {}", e.getMessage());
        }catch (Exception ex){
            throw new QueueException();
        }
    }



}
