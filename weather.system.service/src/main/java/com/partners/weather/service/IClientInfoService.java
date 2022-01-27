package com.partners.weather.service;

import java.util.List;

import com.partners.entity.ClientInfo;
import com.partners.view.entity.ResponseMsg;

public interface IClientInfoService {
	public List<ClientInfo> getClientInfos();

	public ClientInfo getClientInfo(String clientIP);
	
	public ClientInfo getClientInfoByWSNumber(String weatherStationNumber);

	public ResponseMsg insertClientInfo(ClientInfo clientInfo);

	public ResponseMsg updateClientInfo(ClientInfo clientInfo);
}
