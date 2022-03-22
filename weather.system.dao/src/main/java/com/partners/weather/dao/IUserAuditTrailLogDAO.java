package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.AuditTrailLog;
import com.partners.view.entity.VAuditTrailLog;

public interface IUserAuditTrailLogDAO {

	int insertAuditTratilLog(AuditTrailLog auditTrailLog);

	List<AuditTrailLog> getAuditTratilLogs(VAuditTrailLog vAuditTrailLog);

	void delAuditTrailLog();
	
	int getAuditTratilLogCount(VAuditTrailLog vAuditTrailLog);
	
	AuditTrailLog getAuditTratilLog(int userAuditTrailLogId);
	

}
