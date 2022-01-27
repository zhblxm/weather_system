package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partners.entity.AuditTrailLog;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VAuditTrailLog;
import com.partners.weather.dao.IUserAuditTrailLogDAO;
import com.partners.weather.service.IUserAuditTrailLogService;

@Service
@Transactional
public class UserAuditTrailLogServiceImp implements IUserAuditTrailLogService {
	private static final Logger logger = LoggerFactory.getLogger(UserAuditTrailLogServiceImp.class);
	@Autowired
	private IUserAuditTrailLogDAO userAuditTrailLogDAO;

	@Override
	public ResponseMsg insertAuditTratilLog(AuditTrailLog auditTrailLog) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			userAuditTrailLogDAO.insertAuditTratilLog(auditTrailLog);
		} catch (Exception exception) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("保存用户日志失败！");
			logger.error("Error in {}", exception);
		}
		return responseMsg;
	}

	@Override
	public List<AuditTrailLog> getAuditTratilLogs(VAuditTrailLog vAuditTrailLog) {
		List<AuditTrailLog> auditTrailLogs = null;
		try {
			auditTrailLogs = userAuditTrailLogDAO.getAuditTratilLogs(vAuditTrailLog);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return auditTrailLogs == null ? new ArrayList<AuditTrailLog>(0) : auditTrailLogs;
	}

	@Override
	public ResponseMsg delAuditTrailLog() {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			userAuditTrailLogDAO.delAuditTrailLog();
		} catch (Exception exception) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("删除用户历史数据失败！");
			logger.error("Error in {}", exception);
		}
		return responseMsg;
	}

	@Override
	public int getAuditTratilLogCount(VAuditTrailLog vAuditTrailLog) {
		int count=0;
		try {
			count = userAuditTrailLogDAO.getAuditTratilLogCount(vAuditTrailLog);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return count;
	}

	@Override
	public AuditTrailLog getAuditTratilLog(int userAuditTrailLogId) {
		AuditTrailLog auditTrailLog = null;
		try {
			auditTrailLog = userAuditTrailLogDAO.getAuditTratilLog(userAuditTrailLogId);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return auditTrailLog;
	}

}
