package com.partners.weather.service;

import java.util.List;

import com.partners.entity.AuditTrailLog;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VAuditTrailLog;

public interface IUserAuditTrailLogService {
	public ResponseMsg insertAuditTratilLog(AuditTrailLog auditTrailLog);

	public List<AuditTrailLog> getAuditTratilLogs(VAuditTrailLog vAuditTrailLog);

	public ResponseMsg delAuditTrailLog();
	
	public int getAuditTratilLogCount(VAuditTrailLog vAuditTrailLog);
	
	public AuditTrailLog getAuditTratilLog(int userAuditTrailLogId);
}
