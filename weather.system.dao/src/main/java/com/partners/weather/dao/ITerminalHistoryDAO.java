package com.partners.weather.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.partners.entity.Terminalhistory;
import com.partners.view.entity.VTerminalhistory;

public interface ITerminalHistoryDAO {
	
	void createNewTable(@Param("tableName") String tableName);
	
	int inserTerminalhistory(Terminalhistory terminalhistory);
	
	List<Terminalhistory> getTerminalhistorys(VTerminalhistory vTerminalhistory);
	
}
