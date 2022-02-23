package com.partners.weather.service;

import java.util.List;

import com.partners.entity.Terminalhistory;
import com.partners.view.entity.VTerminalhistory;

public interface ITerminalHistoryService {

    void createNewTable(String tableName) throws IllegalArgumentException;

    int inserTerminalhistory(Terminalhistory terminalhistory);

    void updateTerminalParamCategory(Terminalhistory terminalhistory);

    List<Terminalhistory> getTerminalhistorys(VTerminalhistory vTerminalhistory);
}
