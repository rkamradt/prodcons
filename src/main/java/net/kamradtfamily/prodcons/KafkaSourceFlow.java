package net.kamradtfamily.prodcons;

import java.util.Optional;
import net.kamradtfamily.flow.FlowListImplementation;
import org.springframework.stereotype.Component;

@Component
public class KafkaSourceFlow extends FlowListImplementation<Optional<Byte[]>> {

}
