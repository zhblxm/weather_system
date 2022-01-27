package com.partners.weather.service;

import java.util.List;

import com.partners.entity.Terminalhistory;
import com.partners.view.entity.VTerminalhistory;

public interface ITerminalHistoryService {

	public void createNewTable(String tableName) throws IllegalArgumentException;

	public int inserTerminalhistory(Terminalhistory terminalhistory);

	public void updateTerminalParamCategory(Terminalhistory terminalhistory);

	public List<Terminalhistory> getTerminalhistorys(VTerminalhistory vTerminalhistory);
}
