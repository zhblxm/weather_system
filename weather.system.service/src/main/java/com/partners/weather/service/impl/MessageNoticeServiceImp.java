package com.partners.weather.service.impl;

import com.google.common.collect.Lists;

import com.partners.entity.MessageNotice;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VMessageNotice;
import com.partners.weather.dao.IMessageNoticeDAO;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.service.IMessageNoticeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageNoticeServiceImp implements IMessageNoticeService {
    private IMessageNoticeDAO messageNoticeDAO;

    @Autowired
    public MessageNoticeServiceImp(IMessageNoticeDAO messageNoticeDAO) {
        this.messageNoticeDAO = messageNoticeDAO;
    }

    @Override
    public List<MessageNotice> getMessageNotices(VMessageNotice messageNotice) {
        try {
            return messageNoticeDAO.getMessageNotices(messageNotice);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Lists.newArrayListWithCapacity(0);
    }

    @Override
    public MessageNotice getMessageNotice(int messageNoticeId) {
        try {
            return messageNoticeDAO.getMessageNotice(messageNoticeId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ResponseMsg insertMessageNotice(MessageNotice messageNotice) {
        ResponseMsg responseMsg = ResponseMsg.builder().statusCode(0).build();
        int messageNoticeId = messageNotice.getMessageNoticeId();
        if (messageNoticeId > 0) {
            messageNoticeDAO.updateMessageNotice(messageNotice);
        } else {
            messageNoticeDAO.insertMessageNotice(messageNotice);
        }
        responseMsg.setMessageObject(HexUtil.IntToHex(messageNotice.getMessageNoticeId()));
        return responseMsg;
    }

    @Override
    public ResponseMsg updateMessageNotice(MessageNotice messageNotice) {
        try {
            messageNoticeDAO.updateMessageNotice(messageNotice);
            return  ResponseMsg.builder().statusCode(0).build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return  ResponseMsg.builder().statusCode(1).message("Update message notice failed").build();
    }

    @Override
    public boolean delMessageNotice(int messageNoticeId) {
        try {
            messageNoticeDAO.delMessageNotice(messageNoticeId);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Boolean.FALSE;

    }

    @Override
    public int getMessageNoticeCount(VMessageNotice messageNotice) {
        try {
           return messageNoticeDAO.getMessageNoticeCount(messageNotice);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

}
