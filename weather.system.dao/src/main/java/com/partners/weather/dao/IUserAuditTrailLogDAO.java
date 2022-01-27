package com.partners.weather.dao;

import java.util.List;

import com.partners.entity.AuditTrailLog;
import com.partners.view.entity.VAuditTrailLog;

public interface IUserAuditTrailLogDAO {

	public int insertAuditTratilLog(AuditTrailLog auditTrailLog);

	public List<AuditTrailLog> getAuditTratilLogs(VAuditTrailLog vAuditTrailLog);

	public void delAuditTrailLog();
	
	public int getAuditTratilLogCount(VAuditTrailLog vAuditTrailLog);
	
	public AuditTrailLog getAuditTratilLog(int userAuditTrailLogId);
	

}
