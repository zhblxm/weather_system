package com.partners.annotation;

public enum UserPermissionEnum {
	ALLOWALL(0,"允许全部"),
	MONITOR(1,"实时监控"),
	SELECT(2,"数据查询"),	
	AGGS(3,"数据统计"),
	ANALYSIS(4,"图表分析"),
	EXCEPTIONDATA(5,"异常数据"),
	IMAGEMONITOR(6,"图像监控"),
	USERGUOUP(7,"用户组管理"),
	USERGUOUPSELECT(8,"用户组管理查询"),
	USERGUOUPINSERTANDUPDATE(9,"用户组管理添加和更新"),
	USERGUOUPDELETE(10,"用户组管理删除"),
	ADMINUSER(11,"管理员管理"),
	ADMINUSERSELECT(12,"管理员管理查询"),
	ADMINUSERINSERTANDUPDATE(13,"管理员管理添加和更新"),
	ADMINUSERDELETE(14,"管理员管理删除"),
	STATIONCATEGORY(15,"站点分类管理"),
	STATIONCATEGORYSELECT(16,"站点分类管理查询"),
	STATIONCATEGORYINSERTANDUPDATE(17,"站点分类管理添加和更新"),
	STATIONCATEGORYDELETE(18,"站点分类管理删除"),	
	STATION(19,"站点管理"),
	STATIONSELECT(20,"站点管理查询"),
	STATIONINSERTANDUPDATE(21,"站点管理添加和更新"),
	STATIONDELETE(22,"站点管理删除"),
	PARAMETERGROUP(23,"要素分类管理"),
	PARAMETERGROUPSELECT(24,"要素分类管理查询"),
	PARAMETERGROUPINSERTANDUPDATE(25,"要素分类管理添加和更新"),
	PARAMETERGROUPDELETE(26,"要素分类管理删除"),
	TERMINALPARAMETER(27,"要素管理"),
	TERMINALPARAMETERSELECT(28,"要素管理查询"),
	TERMINALPARAMETERINSERTANDUPDATE(29,"要素管理添加和更新"),
	TERMINALPARAMETERDELETE(30,"要素管理删除"),
	FILEPUSH(31,"文件推送设置"),
	MANUALSYNDATA(32,"手动同步数据设置"),
	EMAILANDSMS(33,"邮件服务器和短信设置"),
	RESETPASSWORD(34,"重置密码"),	
	USERAUDITTRATILLOG(35,"用户操作记录报表"),	
	SYSTEMSETUP(36,"系统设置"),
	STATISTICS(37,"及/逾时统计"),
	NOTIFICATION(38,"系统通知"),
	SCHEDULE(39,"任务执行报表"),
	SERIALPORT(40,"串口设置"),
	NOTIFICATIONSELECT(41,"系统通知查询"),
	NOTIFICATIONINSERTANDUPDATE(42,"系统通知添加和更新"),
	NOTIFICATIONDELETE(43,"系统通知删除");
	private final int id;
	private final String name;
	UserPermissionEnum(int id,String name) {
		this.id = id;
		this.name=name;
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
}
