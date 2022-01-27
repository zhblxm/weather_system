package com.partners.weather.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.partners.weather.common.ResultCode;
import com.partners.weather.protocol.socket.ProtocolServer;

public class ProtocolImp extends GeneralComponent implements IProtocol {
	private static final Logger logger = LoggerFactory.getLogger(ProtocolImp.class);
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
			logger.error(e.getMessage());
			return ResultCode.ComponentCannotPaused;

		}

		return ResultCode.OK;
	}

	public ResultCode start() {
		try {
			if (!hasStart) {
				hasStart = true;
				boolean bol = initComponent();
				if (bol) {
					return ResultCode.OK;
				} else {
					hasStart = false;
					return ResultCode.ComponentCannotStarted;
				}
			} else {
				return ResultCode.ComponentAlreadyStarted;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			hasStart = false;
			return ResultCode.ComponentCannotStarted;
		}

	}

	public ResultCode stop() {
		try {
			if (hasStart) {
				server.stopServer();
				hasStart = false;
				server = null;
			} else {
				return ResultCode.ComponentNotStarted;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return ResultCode.ComponentCannotStopped;
		}
		return ResultCode.OK;
	}
}
