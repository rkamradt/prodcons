package net.kamradtfamily.prodcons;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MainBean {
  @Autowired
  ProducerService producer;
  @Autowired
  KafkaSourceFlow kafkaSourceFlow;
  @Autowired
  KafkaProduceFlow kafkaProduceFlow;
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  EncryptionBean encryptionBean;
  @Autowired
  AvroCodec avroCodec;

  void run() {
    Random random = new Random();

    kafkaProduceFlow
        .addTransform(b -> b.orElse(null))
        .addFilter(b -> random.nextBoolean())
        .addSink(b -> sendAndReportMessage(b));

    kafkaSourceFlow.addTransform(b -> getDecodedPayload(b))
        .addTransform(b -> getDecryptedPayload(b))
        .addTransform(b -> b.map(o -> o.getRight()))
        .addTransform(b -> parseHelloWorld(b))
        .addFlow(kafkaProduceFlow)
        .addSink(hw -> log.info("Sunk messasge " + hw));

    IntStream.range(0, 50).forEach(i -> sendAndReportMessage(HelloWorld.builder()
        .message("hello world " + i)
        .name("me")
        .build()));
  }

  public void sendAndReportMessage(HelloWorld hw) {
    try {
      log.info("Sending message " + hw);
      producer.sendKafkaMessage(hw);
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }

  public Optional<HelloWorld> parseHelloWorld(Optional<String> json) {
    return json.flatMap(o -> {
      try {
        return Optional.of(objectMapper.readValue(o, HelloWorld.class));
      } catch (IOException e) {
        log.error("Error parsing json", e);
      }
      return Optional.empty();
    });
  }
  public Optional<KafkaMessage> getDecodedPayload(Optional<Byte[]> data) {
     return data.flatMap(d -> {
       try {
         return Optional.of(avroCodec.avroBytesToKafkaMessage(d));
       } catch (IOException e) {
         log.error("Error decoding payload", e);
       }
       return Optional.empty();
     });
  }
  public Optional<ImmutablePair<String, String>> getDecryptedPayload(Optional<KafkaMessage> message) {
    return message.flatMap(m -> {
      try {
        return Optional.of(ImmutablePair.of(m.getMetadata().getRequestId(),new String(encryptionBean.decryptByteArray(m.getContent()))));
      } catch (Exception e) {
        log.error("Error decrypting payload", e);
      }
      return Optional.empty();
    });
  }


}
