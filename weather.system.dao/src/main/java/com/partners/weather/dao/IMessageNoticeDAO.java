package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.MessageNotice;
import com.partners.view.entity.VMessageNotice;


public interface IMessageNoticeDAO {
	public List<MessageNotice> getMessageNotices(VMessageNotice messageNotice);
	
	public MessageNotice getMessageNotice(int messageNoticeId);

	public int insertMessageNotice(MessageNotice messageNotice);

	public void updateMessageNotice(MessageNotice messageNotice);
	
	public int getMessageNoticeCount(VMessageNotice messageNotice);

	public void delMessageNotice(int messageNoticeId);
}
