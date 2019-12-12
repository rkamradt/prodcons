package net.kamradtfamily.prodcons;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ConsumerService {
  @Value(value = "${kafka.topic}")
  private String topic;
  @Value(value = "${kafka.groupId}")
  private String groupId;
  @Value(value = "${kafka.topic}-${kafka.groupId}")
  private String listenerId;
  @Value(value = "${kafka.cacheSize:10}")
  private Integer cacheSize;

  @Autowired
  KafkaListenerEndpointRegistry registry;
  @Autowired
  private AvroCodec avroCodec;
  @Autowired
  private EncryptionBean encryptionBean;
  @Autowired
  private ObjectMapper objectMapper;

  private ConcurrentLinkedDeque<HelloWorld> cache = new ConcurrentLinkedDeque<>();

  @KafkaListener(id = "${kafka.topic}-${kafka.groupId}",
      topics = "${kafka.topic}",
      groupId = "${kafka.groupId}",
      containerFactory = "kafkaListenerContainerFactory")
  public void listen(byte [] message, Acknowledgment ack) {
    try {
      ack.acknowledge();
      ImmutablePair<String, String> payloadWithRequestId =
          getDecryptedPayload(message);
      cache.add(objectMapper.readValue(payloadWithRequestId.getRight(), HelloWorld.class));
      if(cache.size() >= cacheSize) {
        pause();
      }
    } catch(Throwable ex) {
      log.error("kafka listener threw exception during processing", ex);
    }
  }

  public ImmutablePair<String, String> getDecryptedPayload(
      byte[] data)
      throws BadPaddingException, IllegalBlockSizeException, IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    KafkaMessage kafkaMessage =
        avroCodec.avroBytesToKafkaMessage(
            encryptionBean.decryptByteArray(data));
    return new ImmutablePair<>(
        kafkaMessage.getMetadata().getRequestId(),
        new String(kafkaMessage.getContent()));
  }

  public HelloWorld pull() {
    if(cache.isEmpty()) {
      return null;
    }
    if (cache.size() == 1) {
      resume();
    }
    return cache.pop();
  }
  private void pause() {
    log.info("pausing " + listenerId);
    registry.getListenerContainer(listenerId).pause();
    try {
      Thread.sleep(1000); // give pause time to engage
    } catch (InterruptedException e) {
      log.info("interrupted");
    }
  }
  private void resume() {
    if(!registry.getListenerContainer(listenerId).isContainerPaused())
      return;
    log.info("resuming " + listenerId);
    registry.getListenerContainer(listenerId).resume();
  }
}
