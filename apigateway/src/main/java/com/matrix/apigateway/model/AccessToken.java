package com.matrix.apigateway.model;

public class AccessToken {
  private String accessToken;

  public AccessToken() {}

  public AccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
