package net.kamradtfamily.prodcons;

import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class KafkaSourceFlow extends FlowListImplementation<Optional<Byte[]>> {

}
