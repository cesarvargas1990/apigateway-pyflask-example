package com.matrix.apigateway.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.matrix.apigateway.request.LoginRequest;
import com.matrix.apigateway.util.Aes256;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Controller
public class TokenController {

  @Value("${url}")
  private String url;

  @Value("${clientId}")
  private String clientId;

  @Value("${grantType}")
  private String grantType;

  @Value("${clientSecret}")
  private String clientSecret;

  private final WebClient.Builder webClientBuilder;

  private Aes256 aes256;

  @Value("${AES256_SECRET_KEY}")
  private String secretKey;

  @Value("${AES256_SALT}")
  private String salt;


  @Autowired
  public TokenController(WebClient.Builder webClientBuilder) {
    this.webClientBuilder = webClientBuilder;
  }

  @ResponseBody
  @PostMapping(value = "/token", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Map<String, String>> getToken(@RequestBody LoginRequest loginRequest) {
    String username = loginRequest.getUsername();
    String password = loginRequest.getPassword();

    return WebClient.builder().build()
            .post()
            .uri(url)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(BodyInserters
                    .fromFormData("client_id", clientId)
                    .with("username", username)
                    .with("password", password)
                    .with("grant_type", grantType)
                    .with("client_secret", clientSecret)
            )
            .exchange()
            .flatMap(response -> response.bodyToMono(JsonNode.class))
            .map(node -> {
              String accessToken = node.get("access_token").asText();
                 System.out.println("accessToken");
                                    System.out.println(accessToken);
              Map<String, String> response = new HashMap<>();
              try {
                Aes256 aes256 = new Aes256(salt,secretKey);
                response.put("access_token", aes256.encrypt(accessToken));
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
              return response;
            });
  }

}