package net.kamradtfamily.prodcons;

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
  private KafkaTemplate<String, HelloWorld> kafkaTemplate;

  public void sendMessage(HelloWorld message) {

    kafkaTemplate.send(topic, message).addCallback(new ListenableFutureCallback<SendResult<String, HelloWorld>>() {

      @Override
      public void onSuccess(SendResult<String, HelloWorld> result) {
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
}
