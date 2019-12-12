package net.kamradtfamily.prodcons;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class KafkaMessage implements Serializable {
    private KafkaMetadata metadata;
    private byte[] content;

}
