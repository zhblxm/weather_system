package com.partners.weather.service;

import java.util.List;

import com.partners.entity.MessageNotice;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VMessageNotice;

public interface IMessageNoticeService {
    List<MessageNotice> getMessageNotices(VMessageNotice messageNotice);

    MessageNotice getMessageNotice(int messageNoticeId);

    ResponseMsg insertMessageNotice(MessageNotice messageNotice);

    ResponseMsg updateMessageNotice(MessageNotice messageNotice);

    boolean delMessageNotice(int messageNoticeId);

    int getMessageNoticeCount(VMessageNotice messageNotice);
}
