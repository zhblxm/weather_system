package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partners.entity.ClientInfo;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.dao.IClientInfoDAO;
import com.partners.weather.service.IClientInfoService;

@Service
@Transactional
public class ClientInfoServiceImp implements IClientInfoService {
	private static final Logger logger = LoggerFactory.getLogger(ClientInfoServiceImp.class);
	@Autowired
	private IClientInfoDAO clientInfoDAO;

	@Override
	public ClientInfo getClientInfo(String clientIP) {
		ClientInfo clientInfo = null;
		try {
			clientInfo = clientInfoDAO.getClientInfo(clientIP);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return clientInfo;
	}
	
	@Override
	public ClientInfo getClientInfoByWSNumber(String weatherStationNumber) {
		ClientInfo clientInfo = null;
		try {
			clientInfo = clientInfoDAO.getClientInfoByWSNumber(weatherStationNumber);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return clientInfo;
	}

	@Override
	public ResponseMsg insertClientInfo(ClientInfo clientInfo) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			ClientInfo extClientInfo=this.getClientInfo(clientInfo.getClientIP());
			if (extClientInfo == null) {
				clientInfoDAO.insertClientInfo(clientInfo);
			}else {
				responseMsg=this.updateClientInfo(clientInfo);
			}
		} catch (Exception e) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage(e.getMessage());
			logger.error("Error in {}", e);
		}
		return responseMsg;
	}

	@Override
	public ResponseMsg updateClientInfo(ClientInfo clientInfo) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			clientInfoDAO.updateClientInfo(clientInfo);
		} catch (Exception e) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage(e.getMessage());
			logger.error("Error in {}", e);
		}
		return responseMsg;
	}

	@Override
	public List<ClientInfo> getClientInfos() {
		List<ClientInfo>  clientInfos = null;
		try {
			clientInfos = clientInfoDAO.getClientInfos();
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return clientInfos==null?new ArrayList<ClientInfo>(0):clientInfos;
	}

}
