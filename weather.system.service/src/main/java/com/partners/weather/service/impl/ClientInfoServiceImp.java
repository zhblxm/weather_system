package com.partners.weather.service.impl;

import com.google.common.collect.Lists;

import com.partners.entity.ClientInfo;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.dao.IClientInfoDAO;
import com.partners.weather.service.IClientInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class ClientInfoServiceImp implements IClientInfoService {

    @Autowired
    private IClientInfoDAO clientInfoDAO;

    @Override
    public ClientInfo getClientInfo(String clientIP) {
        try {
            return clientInfoDAO.getClientInfo(clientIP);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ClientInfo getClientInfoByWSNumber(String weatherStationNumber) {
        try {
            return clientInfoDAO.getClientInfoByWSNumber(weatherStationNumber);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ResponseMsg insertClientInfo(ClientInfo clientInfo) {
        ResponseMsg responseMsg = ResponseMsg.builder().statusCode(0).build();
        try {
            ClientInfo extClientInfo = this.getClientInfo(clientInfo.getClientIP());
            if (Objects.isNull(extClientInfo)) {
                clientInfoDAO.insertClientInfo(clientInfo);
            } else {
                responseMsg = this.updateClientInfo(clientInfo);
            }
        } catch (Exception e) {
            responseMsg = ResponseMsg.builder().statusCode(1).message(e.getMessage()).build();
            log.error(e.getMessage(), e);
        }
        return responseMsg;
    }

    @Override
    public ResponseMsg updateClientInfo(ClientInfo clientInfo) {
        try {
            clientInfoDAO.updateClientInfo(clientInfo);
            return ResponseMsg.builder().statusCode(0).build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ResponseMsg.builder().statusCode(1).message("Update client failed.").build();
    }

    @Override
    public List<ClientInfo> getClientInfos() {
        try {
            return clientInfoDAO.getClientInfos();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Lists.newArrayListWithCapacity(0);
    }

}
