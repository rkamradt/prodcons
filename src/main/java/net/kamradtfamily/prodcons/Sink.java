package net.kamradtfamily.prodcons;

public interface Sink<T> {
  void process(T t);
}
