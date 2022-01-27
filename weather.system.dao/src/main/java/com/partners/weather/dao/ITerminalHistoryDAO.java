package com.partners.weather.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.partners.entity.Terminalhistory;
import com.partners.view.entity.VTerminalhistory;

public interface ITerminalHistoryDAO {
	
	public void createNewTable(@Param("tableName") String tableName);
	
	public int inserTerminalhistory(Terminalhistory terminalhistory);
	
	public void updateTerminalParamCategory(Terminalhistory terminalhistory);
	
	public List<Terminalhistory> getTerminalhistorys(VTerminalhistory vTerminalhistory);
	
}
