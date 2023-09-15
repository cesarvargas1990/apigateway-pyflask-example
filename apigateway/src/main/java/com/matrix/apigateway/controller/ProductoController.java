package com.matrix.apigateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.matrix.apigateway.util.Aes256;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Controller
public class ProductoController {

    @Value("${AES256_SECRET_KEY}")
    private String secretKey;

    @Value("${AES256_SALT}")
    private String salt;
    private final WebClient.Builder webClientBuilder;

    @Autowired

    public ProductoController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private static final String PRODUCT_SERVICE_URL = "http://flask-app:5000/productos";

    @ResponseBody
    @PostMapping(value = "/crearProducto", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<JsonNode> callProductService(@RequestHeader("Authorization")
                                                 String encryptedTokenHeader, @RequestBody String productData) {
        Aes256 aes256 = new Aes256(salt, secretKey);
       
        try {
            String decryptedToken = aes256.decrypt(encryptedTokenHeader.replace("Bearer ", ""));
            return webClientBuilder.build()
                    .method(HttpMethod.POST)
                    .uri(PRODUCT_SERVICE_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + decryptedToken)
                    .body(BodyInserters.fromValue(productData))
                    .exchange()
                    .flatMap(response -> response.bodyToMono(JsonNode.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
