package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.ClientInfo;

public interface IClientInfoDAO {

	public List<ClientInfo> getClientInfos();

	public ClientInfo getClientInfo(String clientIP);
	
	public ClientInfo getClientInfoByWSNumber(String weatherStationNumber);

	public int insertClientInfo(ClientInfo clientInfo);

	public void updateClientInfo(ClientInfo clientInfo);

}
