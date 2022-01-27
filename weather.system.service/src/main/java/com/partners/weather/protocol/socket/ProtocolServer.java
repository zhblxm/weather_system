package com.partners.weather.protocol.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.weather.common.ResultCode;
import com.partners.weather.download.TerminalDownload;
import com.partners.weather.event.ComponentStartEvent;
import com.partners.weather.event.args.ComponentStartEventArgs;

public class ProtocolServer {
	private static final Logger logger = LoggerFactory.getLogger(ProtocolServer.class);
	private int port;
	private ServerSocket serverSocket;
	private boolean runServer = false;

	public ProtocolServer(int port) {
		this.port = port;
	}

	public boolean initSocketServer(int componentId) {
		boolean initResult = false;
		try {
			if (port > 0) {
				serverSocket = new ServerSocket(port);
				this.dispatchEvent(componentId, ResultCode.OK);
				this.startServer(componentId);
				initResult = true;
			} else {
				dispatchEvent(componentId, ResultCode.ComponentCannotStarted);
			}
		} catch (Exception e) {
			logger.error("Init socket server exception.\r\n" + "@" + e.getMessage());
		}
		return initResult;
	}

	private void dispatchEvent(int componentId, ResultCode result) {
		ComponentStartEventArgs args = new ComponentStartEventArgs(componentId, result);
		ComponentStartEvent event = new ComponentStartEvent(args);
		event.dispatchEvent();
	}

	private void startServer(int componentId) throws Exception {
		runServer = true;
		int coreSize=Runtime.getRuntime().availableProcessors();
		ExecutorService downloadThreadPool =  Executors.newFixedThreadPool(coreSize);
		while (runServer) {
			Socket socket = null;
			try {
				if (serverSocket != null && !serverSocket.isClosed()) {
					try {
						socket = serverSocket.accept();
						if (socket.isBound()) {
							//SessionState sessionState = new TerminalDownload(socket).doTransferSession();
							downloadThreadPool.execute(new TerminalDownload(socket));
						}
					} 
					catch (SocketException e) {
						logger.error("Socket is close.\r\n" + "@" + e.getMessage());
					}
					catch (Exception e) {
						e.printStackTrace();
						logger.error("Start download exception.\r\n" + "@" + e.getMessage());
					}
				} else {
					throw new Exception("Socket Server Not Started.");
				}
			} catch (Exception e) {
				logger.error("Download Server Runtime exception.\r\n" + "@" + e.getMessage());
				serverSocket = null;
				break;
			}
		}
	}

	public void stopServer() throws Exception {
		while (serverSocket != null && !serverSocket.isClosed()) {
			serverSocket.close();
		}
		if (serverSocket != null) {
			serverSocket = null;
		}
		runServer = false;
	}

	public void pauseComponent() throws InterruptedException {
		serverSocket.wait();
	}
}
