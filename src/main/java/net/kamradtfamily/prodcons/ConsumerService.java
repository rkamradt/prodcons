package net.kamradtfamily.prodcons;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
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

  @Autowired
  private KafkaSourceFlow kafkaSourceFlow;

  @KafkaListener(id = "${kafka.topic}-${kafka.groupId}",
      topics = "${kafka.topic}",
      groupId = "${kafka.groupId}",
      containerFactory = "kafkaListenerContainerFactory")
  public void listen(Byte [] message, Acknowledgment ack) {
    try {
      ack.acknowledge();
      kafkaSourceFlow.process(Optional.ofNullable(message));
    } catch(Throwable ex) {
      log.error("kafka listener threw exception during processing", ex);
    }
  }
}
