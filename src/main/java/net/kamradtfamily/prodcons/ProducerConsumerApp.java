package net.kamradtfamily.prodcons;

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

    MainBean mainBean = context.getBean(MainBean.class);
    mainBean.run();
    System.out.println("hello world");
    Thread.sleep(10000);

    context.close();
  }


  @Override
  public void onApplicationEvent(KafkaEvent event) {
    log.info(event.toString());
  }

}
