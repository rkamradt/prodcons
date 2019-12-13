package net.kamradtfamily.prodcons;

public interface Transform<I, O> {
  O transform(I i);
}
