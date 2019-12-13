package net.kamradtfamily.prodcons;

public interface Flow<T> {
  public void process(T message);
  public <O> void addSink(Sink<O> s);
  public <O> Flow<O> addTransform(Transform<T, O> o);
}
