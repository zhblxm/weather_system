package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.MessageNotice;
import com.partners.view.entity.VMessageNotice;


public interface IMessageNoticeDAO {
	List<MessageNotice> getMessageNotices(VMessageNotice messageNotice);
	
	MessageNotice getMessageNotice(int messageNoticeId);

	int insertMessageNotice(MessageNotice messageNotice);

	void updateMessageNotice(MessageNotice messageNotice);
	
	int getMessageNoticeCount(VMessageNotice messageNotice);

	void delMessageNotice(int messageNoticeId);
}
