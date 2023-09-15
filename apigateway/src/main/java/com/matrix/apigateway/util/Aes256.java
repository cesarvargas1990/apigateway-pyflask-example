package com.matrix.apigateway.util;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

public class Aes256 {

  public Aes256(String salt, String secretKey) {
    this.salt = salt;
    this.secretKey = secretKey;
    this.secret = generateSecretKey();
  }

  private String salt;
  private String secretKey;

  private static final byte[] IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
  private SecretKey secret;



  private SecretKey generateSecretKey() {
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
      SecretKey tmp = factory.generateSecret(spec);
      return new SecretKeySpec(tmp.getEncoded(), "AES");
    } catch (Exception e) {
      throw new RuntimeException("Error while generating secret key: " + e.toString(), e);
    }
  }

  public String encrypt(String strToEncrypt) {
    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(IV));
      byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(encryptedBytes);
    } catch (Exception e) {
      throw new RuntimeException("Error while encrypting: " + e.toString(), e);
    }
  }

  public String decrypt(String strToDecrypt) {
    try {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(IV));
      byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
      return new String(decryptedBytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Error while decrypting: " + e.toString(), e);
    }
  }
}
