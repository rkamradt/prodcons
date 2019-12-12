package net.kamradtfamily.prodcons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AvroCodec {

  public byte[] kafkaMessageToAvroBytes(KafkaMessage message) throws IOException {
    Schema schema = ReflectData.get().getSchema(KafkaMessage.class);
    final ReflectData reflectData = new ReflectData();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    BinaryEncoder binaryEncoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null);
    ReflectDatumWriter<KafkaMessage> datumWriter = new ReflectDatumWriter<>(schema, reflectData);
    datumWriter.write(message, binaryEncoder);
    binaryEncoder.flush();
    byteArrayOutputStream.flush();
    byteArrayOutputStream.close();
    return byteArrayOutputStream.toByteArray();
  }

  public KafkaMessage avroBytesToKafkaMessage(byte[] data) throws IOException {
    Schema schema = ReflectData.get().getSchema(KafkaMessage.class);
    final ReflectData reflectData = new ReflectData();
    Decoder decoder = DecoderFactory.get().binaryDecoder(data, null);
    ReflectDatumReader<KafkaMessage> datumReader = new ReflectDatumReader<>(schema, schema, reflectData);
    return datumReader.read(null, decoder);
  }


}
