package com.partners.weather.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListSerializeTransfer<T extends Serializable> extends BaseSerializeTranser {

	@Override
	public byte[] serialize(Object value) {
		if (value == null) {
			throw new NullPointerException("Can't serialize null");
		}
		List<T> values = (List<T>) value;
		byte[] results = null;
		ByteArrayOutputStream bos = null;
		ObjectOutputStream os = null;

		try {
			bos = new ByteArrayOutputStream();
			os = new ObjectOutputStream(bos);
			for (T obj : values) {
				os.writeObject(obj);
			}
			os.writeObject(null);
			os.close();
			bos.close();
			results = bos.toByteArray();
		} catch (IOException e) {
			logger.error("Error in {}",e);
			throw new IllegalArgumentException("Non-serializable object", e);
		} finally {
			close(os);
			close(bos);
		}
		return results;
	}

	@Override
	public Object deserialize(byte[] in) {
		List<T> list = new ArrayList<T>();
		ByteArrayInputStream bis = null;
		ObjectInputStream is = null;
		try {
			if (in != null) {
				bis = new ByteArrayInputStream(in);
				is = new ObjectInputStream(bis);
				while (true) {
					Object obj = is.readObject();
					if (obj == null) {
						break;
					}
					list.add((T)obj);
				}
				is.close();
				bis.close();
			}
		} catch (IOException e) {
			logger.error("Error in {}",e);
		} catch (ClassNotFoundException e) {
			logger.error("Error in {}",e);
		} finally {
			close(is);
			close(bis);
		}

		return list;
	}

}
