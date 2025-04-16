package com.cartservice.client.decoder;

import com.cartservice.exception.FeignClientException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import static com.cartservice.exception.ExceptionConstants.CLIENT_ERROR;
import static com.cartservice.client.decoder.JsonNodeFieldName.MESSAGE;

@Slf4j
public class ClientServiceErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        var errorMessage = CLIENT_ERROR.getMessage();

        JsonNode jsonNode;
        try(var body = response.body().asInputStream()) {
            jsonNode = new ObjectMapper().readValue(body, JsonNode.class);
        }catch (Exception e){
            throw new FeignClientException(CLIENT_ERROR.getMessage(),response.status());
        }

        log.error("ClientService decode error Message: {}, Method {}",errorMessage,methodKey);
        if(jsonNode.has(MESSAGE.getValue()))
            errorMessage=jsonNode.get(MESSAGE.getValue()).asText();

        return new FeignClientException(errorMessage,response.status());
    }
}
