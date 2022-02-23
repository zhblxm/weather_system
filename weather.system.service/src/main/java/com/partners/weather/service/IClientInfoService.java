package com.partners.weather.service;

import java.util.List;

import com.partners.entity.ClientInfo;
import com.partners.view.entity.ResponseMsg;

public interface IClientInfoService {
    List<ClientInfo> getClientInfos();

    ClientInfo getClientInfo(String clientIP);

    ClientInfo getClientInfoByWSNumber(String weatherStationNumber);

    ResponseMsg insertClientInfo(ClientInfo clientInfo);

    ResponseMsg updateClientInfo(ClientInfo clientInfo);
}
