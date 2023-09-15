package com.matrix.apigateway;

import com.matrix.apigateway.util.Aes256;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import javax.crypto.Cipher;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
  private static Cipher rsa;


  public static void main(String[] args) throws Exception {
    SpringApplication.run(ApiGatewayApplication.class, args);

  }

}
