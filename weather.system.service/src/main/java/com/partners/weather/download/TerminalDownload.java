package com.partners.weather.download;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.entity.RequestMessage;
import com.partners.weather.common.SessionState;
import com.partners.weather.protocol.ComponentManager;

@Slf4j
public class TerminalDownload implements Runnable {
    private static final int Timeout = 5;
    private Socket socket;
    private BufferedInputStream in = null;

    public TerminalDownload(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            this.doTransferSession();
        } catch (IOException | InterruptedException e) {
            log.error(String.format("Error in %s", e.getMessage()), e);
        }
    }

    public void doTransferSession() throws IOException, InterruptedException {
        log.debug("Client IP:{}, Port:{}, Date:{}", socket.getInetAddress(), socket.getPort(), DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
        in = new BufferedInputStream(socket.getInputStream());
        SessionState sessionState = this.receive();
        log.debug("Sessions state is: {}", sessionState);
        this.finishDownload();
    }

    private SessionState receive() {
        long startTime = System.currentTimeMillis();
        byte[] buffer;
        int received = 0;
        do {
            if (System.currentTimeMillis() > (startTime + (Timeout * 1000))) {
                log.info("Receive terminal request timeout.\r\n");
                return SessionState.ReqTimeout;
            }
            try {
                int availableLen = in.available();
                if (availableLen < 0) {
                    Thread.sleep(5);
                    continue;
                }
                buffer = new byte[availableLen];
                if ((availableLen = in.read(buffer, 0, availableLen)) > 0) {
                    received += availableLen;
                }
                String receiveWeatherInfo = new String(buffer, "UTF-8");
                if (StringUtils.isBlank(receiveWeatherInfo)) {
                    continue;
                }
                String[] receiveMessages = receiveWeatherInfo.split("\r\n");
                Stream.of(receiveMessages).forEach(receiveMsg -> {
                    if (StringUtils.isBlank(receiveMsg)) {
                        return;
                    }
                    ComponentManager.getInstance().getJedisQueue().pushFromHead(
                            RequestMessage.builder().ClientIP(socket.getInetAddress().getHostAddress())
                                    .Port(socket.getPort())
                                    .RequestMessage(receiveMsg).build());
                });

            } catch (Exception e) {
                log.error("Receive terminal request failed.{}",e.getMessage());
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
