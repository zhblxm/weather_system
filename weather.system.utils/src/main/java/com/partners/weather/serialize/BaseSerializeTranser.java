package com.partners.weather.serialize;

import java.io.Closeable;
import java.util.Objects;

import lombok.SneakyThrows;

public abstract class BaseSerializeTranser {

    public abstract byte[] serialize(Object value);

    public abstract Object deserialize(byte[] in);

    @SneakyThrows
    public void close(Closeable closeable) {
        if (Objects.isNull(closeable)) {
            return;
        }
        closeable.close();
    }
}
