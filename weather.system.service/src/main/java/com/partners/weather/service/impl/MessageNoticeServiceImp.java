package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.partners.entity.MessageNotice;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VMessageNotice;
import com.partners.weather.dao.IMessageNoticeDAO;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.service.IMessageNoticeService;

@Service
public class MessageNoticeServiceImp implements IMessageNoticeService {
	private static final Logger logger = LoggerFactory.getLogger(MessageNoticeServiceImp.class);
 
	@Autowired
	private IMessageNoticeDAO messageNoticeDAO;

	@Override
	public List<MessageNotice> getMessageNotices(VMessageNotice messageNotice) {
		List<MessageNotice> messageNotices = null;
		try {
			messageNotices = messageNoticeDAO.getMessageNotices(messageNotice);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return messageNotices == null ? new ArrayList<MessageNotice>(0) : messageNotices;
	}
	
	@Override
	public MessageNotice getMessageNotice(int messageNoticeId) {
		MessageNotice messageNotice = null;
		try {
			messageNotice = messageNoticeDAO.getMessageNotice(messageNoticeId);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return messageNotice == null ? new MessageNotice() : messageNotice;
	}

	@Override
	public ResponseMsg insertMessageNotice(MessageNotice messageNotice) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
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
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			messageNoticeDAO.updateMessageNotice(messageNotice);
		} catch (Exception e) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage(e.getMessage());
			logger.error("Error in {}", e);
		}
		return responseMsg;
	}
	
	@Override
	public boolean delMessageNotice(int messageNoticeId) {
		boolean blnDelSuccessed = true;
		try {
			messageNoticeDAO.delMessageNotice(messageNoticeId);
		} catch (Exception e) {
			blnDelSuccessed = false;
			logger.error("Error in {}", e);
		}
		return blnDelSuccessed;

	}

	@Override
	public int getMessageNoticeCount(VMessageNotice messageNotice) {
		int count=0;
		try {
			count=messageNoticeDAO.getMessageNoticeCount(messageNotice);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return count;
	}
	 
}
