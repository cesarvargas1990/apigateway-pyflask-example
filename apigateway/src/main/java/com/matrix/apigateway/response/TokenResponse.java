package com.matrix.apigateway.response;

public class TokenResponse {
  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  private String accessToken;
}