package net.kamradtfamily.prodcons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Slf4j
@Component
public class ProducerService {
  @Value(value = "${kafka.topic}")
  private String topic;
  @Autowired
  private KafkaTemplate<String, byte []> kafkaTemplate;
  @Autowired
  private AvroCodec avroCodec;
  @Autowired
  private EncryptionBean encryptionBean;
  @Autowired
  private ObjectMapper objectMapper;

  public void sendMessage(Byte [] message) {
    byte[] data = new byte[message.length];
    for(int i = 0; i < message.length; i++)
      data[i] = message[i];

    kafkaTemplate.send(topic, data).addCallback(new ListenableFutureCallback<SendResult<String, byte []>>() {

      @Override
      public void onSuccess(SendResult<String, byte []> result) {
        log.info("Sent message=[" + message +
            "] with offset=[" + result.getRecordMetadata().offset() + "]");
      }
      @Override
      public void onFailure(Throwable ex) {
        log.info("Unable to send message=["
            + message + "] due to : " + ex.getMessage());
      }
    });
  }
  public Byte[] generateMessages(String requestId, String payload, String eventAction, String eventAt)
      throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
    byte[] bytes = payload.getBytes();
    KafkaMetadata kafkaMetadata = KafkaMetadata.builder()
        .eventAction(eventAction)
        .eventAt(eventAt)
        .requestId(requestId)
        .build();
    KafkaMessage encryptedMessage = KafkaMessage.builder()
        .content(encryptionBean.encryptByteArray(bytes))
        .metadata(kafkaMetadata)
        .build();
    return avroCodec.kafkaMessageToAvroBytes(encryptedMessage);
  }

  public void sendKafkaMessage(HelloWorld message)
      throws IOException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException {
    String data = objectMapper.writeValueAsString(message);
    sendMessage(generateMessages("requestId", data, "action", Instant.now().toString()));
  }
}
