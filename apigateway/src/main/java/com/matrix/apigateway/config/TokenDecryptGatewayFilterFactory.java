package com.matrix.apigateway.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.matrix.apigateway.util.Aes256;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

@Component
public class TokenDecryptGatewayFilterFactory extends
        AbstractGatewayFilterFactory<TokenDecryptGatewayFilterFactory.Config> {

  public TokenDecryptGatewayFilterFactory() {
    super(Config.class);
  }

  private Aes256 aes256;

  @Value("${AES256_SECRET_KEY}")
  private String secretKey;

  @Value("${AES256_SECRET_KEY_BODY}")
  private String secretKeyBody;

  @Value("${AES256_SALT}")
  private String salt;

  @Value("${AES256_SALT_BODY}")
  private String saltBody;

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();
      HttpHeaders headers = request.getHeaders();
      String authorizationHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);
      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        Aes256 aes256H = new Aes256(salt,secretKey);
        Aes256 aes256B = new Aes256(saltBody,secretKeyBody);
        String decryptedToken = aes256H.decrypt(authorizationHeader.replace("Bearer ", ""));
        ServerHttpRequest modifiedRequest = request.mutate()
                .headers(h -> h.set(HttpHeaders.AUTHORIZATION, "Bearer " + decryptedToken))
                .build();

        ServerHttpRequest finalRequest = getServerHttpRequest(aes256B, modifiedRequest);
        return chain.filter(exchange.mutate().request(finalRequest).build());
      } else {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
      }
    };
  }

  private static ServerHttpRequest getServerHttpRequest(Aes256 aes256B, ServerHttpRequest modifiedRequest) {
    ServerHttpRequest finalRequest = new ServerHttpRequestDecorator(modifiedRequest) {
      @Override
      public Flux<DataBuffer> getBody() {
        Flux<DataBuffer> modifiedBody = super.getBody();
        return modifiedBody.map(buffer -> {
          byte[] bytes = new byte[buffer.readableByteCount()];
          buffer.read(bytes);
          String bodyContent = new String(bytes, StandardCharsets.UTF_8);
          Gson gson = new Gson();
          JsonObject jsonObject = gson.fromJson(bodyContent, JsonObject.class);
          String data = jsonObject.get("data").getAsString();
          String dec = aes256B.decrypt(data);
          byte[] modifiedBytes = dec.getBytes(StandardCharsets.UTF_8);
          return new DefaultDataBufferFactory().wrap(modifiedBytes);
        });
      }
    };
    return finalRequest;
  }

  static class Config {
    private boolean enabled;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }
}