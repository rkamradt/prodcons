package net.kamradtfamily.prodcons;

import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.event.KafkaEvent;

@Slf4j
@SpringBootApplication
public class ProducerConsumerApp implements ApplicationListener<KafkaEvent> {
  public static void main(String[] args) throws Exception {
    ConfigurableApplicationContext context = SpringApplication.run(ProducerConsumerApp.class, args);

    ProducerService producer = context.getBean(ProducerService.class);
    ConsumerService consumer = context.getBean(ConsumerService.class);


    IntStream.range(0, 10).forEach(i ->
    {
      try {
        producer.sendKafkaMessage(HelloWorld.builder()
            .message("hello world " + i)
            .name("me")
            .build());
      } catch (Exception e) {
        throw new RuntimeException();
      }
    });

    Thread.sleep(10000);

    IntStream.range(0, 100).forEach(i -> {
      HelloWorld hw = consumer.pull();
      log.info("cached Messasge " + hw);
    });

    context.close();
  }

  @Override
  public void onApplicationEvent(KafkaEvent event) {
    log.info(event.toString());
  }

}
