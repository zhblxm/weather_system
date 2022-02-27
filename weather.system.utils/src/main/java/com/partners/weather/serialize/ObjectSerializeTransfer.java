package com.partners.weather.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectSerializeTransfer<T extends Serializable> extends BaseSerializeTranser {

    @Override
    public byte[] serialize(Object value) {
        if (Objects.isNull(value)) {
            throw new NullPointerException("Can't serialize null");
        }
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject((T) value);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Non-serializable object", e);
        }
    }

    @Override
    public Object deserialize(byte[] in) {
        T result = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream is = null;
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(in);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return (T) objectInputStream.readObject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Non-deserializable object", e);
        }
    }
}
