package com.partners.weather.service;

import java.util.List;

import com.partners.entity.MessageNotice;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VMessageNotice;

public interface IMessageNoticeService {
	public List<MessageNotice> getMessageNotices(VMessageNotice messageNotice);
	
	public MessageNotice getMessageNotice(int messageNoticeId);

	public ResponseMsg insertMessageNotice(MessageNotice messageNotice);

	public ResponseMsg updateMessageNotice(MessageNotice messageNotice);
	
	public boolean delMessageNotice(int messageNoticeId);
	
	public int getMessageNoticeCount(VMessageNotice messageNotice);
}
