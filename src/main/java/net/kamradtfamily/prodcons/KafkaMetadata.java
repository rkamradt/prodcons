package net.kamradtfamily.prodcons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMetadata {
  private String requestId;
  private String payload;
  private String eventAction;
  private String eventAt;
}
