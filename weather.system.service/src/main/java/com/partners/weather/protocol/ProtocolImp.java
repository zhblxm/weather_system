package com.partners.weather.protocol;

import com.partners.weather.common.ResultCode;
import com.partners.weather.protocol.socket.ProtocolServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtocolImp extends GeneralComponent implements IProtocol {
    private boolean hasStart = false;
    private ProtocolServer server = null;

    public ProtocolImp(int port) {
        super(port);

    }

    private boolean initComponent() {
        server = new ProtocolServer(this.getPort());
        return server.initSocketServer(1);
    }

    public ResultCode pause() {
        try {
            if (hasStart) {
                server.pauseComponent();
            } else {
                return ResultCode.ComponentNotStarted;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultCode.ComponentCannotPaused;
        }
        return ResultCode.OK;
    }

    public ResultCode start() {
        try {
            if (hasStart) {
                return ResultCode.ComponentAlreadyStarted;
            }
            boolean bol = initComponent();
            if (bol) {
                hasStart = true;
                return ResultCode.OK;
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        hasStart = false;
        return ResultCode.ComponentCannotStarted;
    }

    public ResultCode stop() {
        try {
            if (!hasStart) {
                return ResultCode.ComponentNotStarted;
            }
            server.stopServer();
            hasStart = false;
            server = null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResultCode.ComponentCannotStopped;
        }
        return ResultCode.OK;
    }
}
