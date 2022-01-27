package com.partners.weather.serialize;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseSerializeTranser {
	protected static Logger logger = LoggerFactory.getLogger(BaseSerializeTranser.class);

	public abstract byte[] serialize(Object value);

	public abstract Object deserialize(byte[] in);

	public void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				logger.error("Unable to close " + closeable, e);
			}
		}
	}
}
