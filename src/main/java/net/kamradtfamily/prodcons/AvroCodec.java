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

  public Byte[] kafkaMessageToAvroBytes(KafkaMessage message) throws IOException {
    Schema schema = ReflectData.get().getSchema(KafkaMessage.class);
    final ReflectData reflectData = new ReflectData();
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    BinaryEncoder binaryEncoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null);
    ReflectDatumWriter<KafkaMessage> datumWriter = new ReflectDatumWriter<>(schema, reflectData);
    datumWriter.write(message, binaryEncoder);
    binaryEncoder.flush();
    byteArrayOutputStream.flush();
    byteArrayOutputStream.close();
    byte [] data = byteArrayOutputStream.toByteArray();
    Byte[] ret = new Byte[data.length];
    for(int i = 0; i < data.length; i++)
      ret[i] = data[i];
    return ret;
  }

  public KafkaMessage avroBytesToKafkaMessage(Byte[] data) throws IOException {
    if(data == null) {
      return null;
    }
    Schema schema = ReflectData.get().getSchema(KafkaMessage.class);
    final ReflectData reflectData = new ReflectData();
    byte[] ret = new byte[data.length];
    for(int i = 0; i < data.length; i++)
      ret[i] = data[i];
    Decoder decoder = DecoderFactory.get().binaryDecoder(ret, null);
    ReflectDatumReader<KafkaMessage> datumReader = new ReflectDatumReader<>(schema, schema, reflectData);
    return datumReader.read(null, decoder);
  }


}
