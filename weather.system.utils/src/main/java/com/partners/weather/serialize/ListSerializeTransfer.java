package com.partners.weather.serialize;

import com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ListSerializeTransfer<T extends Serializable> extends BaseSerializeTranser {

    @Override
    public byte[] serialize(Object value) {
        if (Objects.isNull(value)) {
            throw new NullPointerException("Can't serialize null");
        }
        List<T> values = (List<T>) value;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(bos)) {
            for (T obj : values) {
                os.writeObject(obj);
            }
            os.writeObject(null);
         return bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Non-serializable object", e);
        }
    }

    @Override
    public Object deserialize(byte[] in) {
        List<T> list = Lists.newArrayList();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(in);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            while (true) {
                Object obj = objectInputStream.readObject();
                if (Objects.isNull(obj)) {
                    break;
                }
                list.add((T) obj);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return list;
    }

}
