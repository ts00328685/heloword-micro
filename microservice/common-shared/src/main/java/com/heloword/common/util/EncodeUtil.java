package com.heloword.common.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;
import org.springframework.util.Base64Utils;
import com.heloword.common.exception.HeloServiceException;
import com.heloword.common.type.ResponseCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EncodeUtil {

  private static final String PADDING = "AES/CBC/PKCS5Padding";

  public static String encodeMd5(String source) {

    String algorithm = "md5";

    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      throw HeloServiceException.of(ResponseCode.SYSTEM_ERROR);
    }

    byte[] input = source.getBytes();
    byte[] output = messageDigest.digest(input);

    int signum = 1;
    BigInteger bigInteger = new BigInteger(signum, output);

    int radix = 16;
    String encoded = bigInteger.toString(radix).toUpperCase();

    return encoded;
  }

  public static String encryptAES(String text, String key, String iv) {
    Properties properties = new Properties();
    final ByteBuffer outBuffer;
    final int bufferSize = 1024;
    final int updateBytes;
    final int finalBytes;
    byte[] encoded;
    try (CryptoCipher encipher = Utils.getCipherInstance(PADDING, properties)) {

      ByteBuffer inBuffer = ByteBuffer.allocateDirect(bufferSize);
      outBuffer = ByteBuffer.allocateDirect(bufferSize);
      inBuffer.put(getUTF8Bytes(text));

      inBuffer.flip();
      encipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(getUTF8Bytes(key), "AES"), new IvParameterSpec(getUTF8Bytes(iv)));
      updateBytes = encipher.update(inBuffer, outBuffer);
      finalBytes = encipher.doFinal(inBuffer, outBuffer);
      outBuffer.flip();
      encoded = new byte[updateBytes + finalBytes];
      outBuffer.duplicate().get(encoded);
    } catch (Exception e) {
      log.error("encryption error", e);
      throw HeloServiceException.of(ResponseCode.SYSTEM_ERROR);
    }

    return Base64Utils.encodeToString(encoded);
  }

  public static String decryptAES(String encryptedText, String key, String iv) {
    Properties properties = new Properties();
    final ByteBuffer outBuffer;
    final int BUFFER_SIZE = 1024;
    ByteBuffer decoded = ByteBuffer.allocateDirect(BUFFER_SIZE);
    try (CryptoCipher decipher = Utils.getCipherInstance(PADDING, properties)) {
      decipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(getUTF8Bytes(key), "AES"), new IvParameterSpec(getUTF8Bytes(iv)));
      outBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
      outBuffer.put(Base64Utils.decode(getUTF8Bytes(encryptedText)));
      outBuffer.flip();
      decipher.update(outBuffer, decoded);
      decipher.doFinal(outBuffer, decoded);
      decoded.flip();
    } catch (Exception e) {
      log.error("decryption error", e);
      throw HeloServiceException.of(ResponseCode.SYSTEM_ERROR);
    }
    return asString(decoded);
  }

  public static void verifyCV(String cv, String key, String iv) {
    String decryptedText = decryptAES(cv, key, iv);
    long cvl = Long.parseLong(decryptedText);
    LocalDateTime parsedDateTime = Instant.ofEpochMilli(cvl).atZone(ZoneId.systemDefault()).toLocalDateTime();
    if (LocalDateTime.now().minusSeconds(5).isAfter(parsedDateTime)) {
      throw HeloServiceException.of(ResponseCode.INVALID_REQUEST);
    }
  }

  private static byte[] getUTF8Bytes(String input) {
    return input.getBytes(StandardCharsets.UTF_8);
  }

  private static String asString(ByteBuffer buffer) {
    final ByteBuffer copy = buffer.duplicate();
    final byte[] bytes = new byte[copy.remaining()];
    copy.get(bytes);
    return new String(bytes, StandardCharsets.UTF_8);
  }

}
