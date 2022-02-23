package com.partners.weather.service;

import com.partners.entity.AuditTrailLog;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VAuditTrailLog;

import java.util.List;

public interface IUserAuditTrailLogService {
    ResponseMsg insertAuditTratilLog(AuditTrailLog auditTrailLog);

    List<AuditTrailLog> getAuditTratilLogs(VAuditTrailLog vAuditTrailLog);

    ResponseMsg delAuditTrailLog();

    int getAuditTratilLogCount(VAuditTrailLog vAuditTrailLog);

    AuditTrailLog getAuditTratilLog(int userAuditTrailLogId);
}
