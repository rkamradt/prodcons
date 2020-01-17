package net.kamradtfamily.prodcons;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EncryptionBean {
  @Value("${encryption.key.value}")
  private String encryptionKeyValue;

  public byte[] decryptByteArray(
      byte[] input)
      throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Cipher decoder = getCipher(Cipher.DECRYPT_MODE);
    return decoder.doFinal(input);
  }

  public Cipher getCipher(int cipherMode)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Cipher cipher = null;
    SecretKeySpec secretKeySpec = new SecretKeySpec(encryptionKeyValue.getBytes(), "AES");
    cipher = Cipher.getInstance("AES");
    cipher.init(cipherMode, secretKeySpec);
    return cipher;
  }

  public byte[] encryptByteArray(
      byte[] input)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    Cipher encoder = getCipher(Cipher.ENCRYPT_MODE);
    return encoder.doFinal(input);
  }

}
