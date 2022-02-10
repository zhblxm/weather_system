package com.partners.weather.protocol.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

import com.partners.weather.common.ResultCode;
import com.partners.weather.download.TerminalDownload;
import com.partners.weather.event.ComponentStartEvent;
import com.partners.weather.event.args.ComponentStartEventArgs;

@Slf4j
public class ProtocolServer {
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
                this.startServer();
                initResult = true;
            } else {
                dispatchEvent(componentId, ResultCode.ComponentCannotStarted);
            }
        } catch (Exception e) {
            log.error("Init socket server exception." + "@" + e.getMessage(),e);
        }
        return initResult;
    }

    private void dispatchEvent(int componentId, ResultCode result) {
        ComponentStartEventArgs args = new ComponentStartEventArgs(componentId, result);
        ComponentStartEvent event = new ComponentStartEvent(args);
        event.dispatchEvent();
    }

    private void startServer() {
        runServer = true;
        int coreSize = Runtime.getRuntime().availableProcessors();
        ExecutorService downloadThreadPool = Executors.newFixedThreadPool(coreSize);
        while (runServer) {
            try {
                Socket socket = serverSocket.accept();
                if (socket.isBound()) {
                    downloadThreadPool.execute(new TerminalDownload(socket));
                }
            } catch (Exception e) {
                log.error("Download Server Runtime exception." + "@" + e.getMessage(), e);
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
