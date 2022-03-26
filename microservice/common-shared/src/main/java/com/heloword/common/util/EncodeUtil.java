package com.heloword.common.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncodeUtil {

  /**
   * Encode String with MD5
   *
   * @param source
   * @return
   */
  public static String encode(String source) {

    if (source == null || "".equals(source)) {
      throw new RuntimeException("source empty");
    }

    String algorithm = "md5";

    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance(algorithm);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    byte[] input = source.getBytes();

    byte[] output = messageDigest.digest(input);

    int signum = 1;
    BigInteger bigInteger = new BigInteger(signum, output);

    int radix = 16;
    String encoded = bigInteger.toString(radix).toUpperCase();

    return encoded;
  }

}
