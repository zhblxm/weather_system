package com.partners.weather.download;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.entity.RequestMessage;
import com.partners.weather.common.SessionState;
import com.partners.weather.protocol.ComponentManager;

public class TerminalDownload implements Runnable {
	private static final int Timeout = 5;
	private static final Logger logger = LoggerFactory.getLogger(TerminalDownload.class);
	private Socket socket = null;
	private BufferedInputStream in = null;

	public TerminalDownload(Socket socket) {
		this.socket = socket;
	}
	@Override
	public void run() {
		try {
			this.doTransferSession();
		} catch (IOException | InterruptedException e) {
			logger.error(String.format("Error in %s",e.getMessage()),e);
		}
	}
	public void doTransferSession() throws IOException, InterruptedException {
		logger.debug(String.format("Client IP:%s, Port:%s, Date: %s",socket.getInetAddress(),socket.getPort(),DateTime.now().toString("yyyy-MM-dd HH:mm:ss")));
		in = new BufferedInputStream(socket.getInputStream());
		SessionState sessionState = this.receive();
		logger.debug(String.format("Sessions state is:%s",sessionState));
//		long startTime = System.currentTimeMillis();
//		SessionState sessionState = SessionState.SessionOK;
//		do {
//			sessionState = this.receive();
//		} while (isConnected());
//		System.currentTimeMillis() < (startTime + (5 * 60 * 1000))
		this.finishDownload();
	}

	private SessionState receive() {
		long startTime = System.currentTimeMillis();
		byte[] buffer;
		int received = 0;
		do {
			if (System.currentTimeMillis() > (startTime + (Timeout * 1000))) {
				logger.info("Receive termianl request timeout.\r\n");
				return SessionState.ReqTimeout;
			}
			try {
				int availableLen = in.available();
				if (availableLen > 0) {
					buffer = new byte[availableLen];
					if ((availableLen = in.read(buffer, 0, availableLen)) > 0) {
						received += availableLen;
					}
					String receiveWeatherInfo = new String(buffer, "UTF-8");
					if (!StringUtils.isBlank(receiveWeatherInfo)) {
						String[] receiveMessages = receiveWeatherInfo.split("\r\n");
						RequestMessage message = null;
						for (String receiveMsg : receiveMessages) {
							if (StringUtils.isBlank(receiveMsg)) {
								continue;
							}
							message = new RequestMessage();
							message.setClientIP(socket.getInetAddress().getHostAddress());
							message.setPort(socket.getPort());
							message.setRequestMessage(receiveMsg);
							ComponentManager.getInstance().getJedisQueue().pushFromHead(message);
						}
					}
				} else {
					Thread.sleep(5);
				}
			} catch (Exception e) {
				logger.error("Receive termianl request failed.\r\n" + e.getMessage());
				return SessionState.SessionFailed;
			}
		} while (received <= 0);

		return SessionState.SessionOK;
	}

	private boolean isConnected() {
		try {
			if (socket != null) {
				socket.sendUrgentData(0xFF);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void finishDownload() throws IOException {
		if (in != null) {
			in.close();
		}
		if (socket != null && !socket.isClosed()) {
			socket.shutdownInput();
			socket.shutdownOutput();
			socket.close();
		}

	}


}
