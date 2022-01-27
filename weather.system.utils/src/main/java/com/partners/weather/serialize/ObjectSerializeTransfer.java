package com.partners.weather.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectSerializeTransfer<T extends Serializable> extends BaseSerializeTranser {

	@Override
	public byte[] serialize(Object value) {
		if (value == null) {
			throw new NullPointerException("Can't serialize null");
		}
		byte[] result = null;
		ByteArrayOutputStream bos = null;
		ObjectOutputStream os = null;
		try {
			bos = new ByteArrayOutputStream();
			os = new ObjectOutputStream(bos);
			T obj = (T) value;
			os.writeObject(obj);
			os.close();
			bos.close();
			result = bos.toByteArray();
		} catch (IOException e) {
			logger.error("Error in {}",e);
			throw new IllegalArgumentException("Non-serializable object", e);
		} finally {
			close(os);
			close(bos);
		}
		return result;
	}

	@Override
	public Object deserialize(byte[] in) {
		T result = null;
		ByteArrayInputStream bis = null;
		ObjectInputStream is = null;
		try {
			if (in != null) {
				bis = new ByteArrayInputStream(in);
				is = new ObjectInputStream(bis);
				result = (T) is.readObject();
				is.close();
				bis.close();
			}
		} catch (IOException e) {
			logger.error("Error in {}", e);
		} catch (ClassNotFoundException e) {
			logger.error("Error in {}", e);
		} finally {
			close(is);
			close(bis);
		}
		return result;
	}

}
