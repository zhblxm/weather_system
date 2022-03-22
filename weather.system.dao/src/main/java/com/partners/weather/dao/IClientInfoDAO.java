package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.ClientInfo;

public interface IClientInfoDAO {

	List<ClientInfo> getClientInfos();

	ClientInfo getClientInfo(String clientIP);
	
	ClientInfo getClientInfoByWSNumber(String weatherStationNumber);

	int insertClientInfo(ClientInfo clientInfo);

	void updateClientInfo(ClientInfo clientInfo);

}
