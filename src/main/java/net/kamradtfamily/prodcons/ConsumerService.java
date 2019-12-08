package net.kamradtfamily.prodcons;

import java.util.concurrent.ConcurrentLinkedDeque;
import lombok.extern.slf4j.Slf4j;
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

  private ConcurrentLinkedDeque<HelloWorld> cache = new ConcurrentLinkedDeque<>();

  @KafkaListener(id = "${kafka.topic}-${kafka.groupId}",
      topics = "${kafka.topic}",
      groupId = "${kafka.groupId}",
      containerFactory = "kafkaListenerContainerFactory")
  public void listen(HelloWorld message, Acknowledgment ack) {
    cache.add(message);
    ack.acknowledge();
    if(cache.size() >= cacheSize) {
      pause();
    }
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
