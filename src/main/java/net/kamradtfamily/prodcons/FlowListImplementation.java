package net.kamradtfamily.prodcons;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

@Slf4j
public class FlowListImplementation<T> implements Flow<T> {

  Sink sink;
  List<Transform> transforms = new ArrayList<>();

  public void process(T message) {
    try {
      Object [] container = new Object[1];
      container[0] = message;
      transforms.forEach(t -> container[0] = t.transform(container[0]));
      sink.process(container[0]);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error processing bytes", e);
    }
  }

  public <O> void addSink(Sink<O> s) {
    sink = s;
  }

  public <O> Flow<O> addTransform(Transform<T, O> t) {
    transforms.add(t);
    return (Flow<O>)this;
  };
}
