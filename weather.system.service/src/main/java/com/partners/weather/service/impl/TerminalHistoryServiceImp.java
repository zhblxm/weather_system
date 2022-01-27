package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partners.entity.Terminalhistory;
import com.partners.entity.WeatherstationClient;
import com.partners.view.entity.VTerminalhistory;
import com.partners.weather.dao.ITerminalHistoryDAO;
import com.partners.weather.service.ITerminalHistoryService;

@Service
@Transactional
public class TerminalHistoryServiceImp implements ITerminalHistoryService {
	private static final Logger logger = LoggerFactory.getLogger(TerminalHistoryServiceImp.class);
	private static final Pattern TABLEFORMATE = Pattern.compile("^[0-9a-zA-Z_]+$");
	@Autowired
	private ITerminalHistoryDAO terminalHistorDAO;

	@Override
	public void createNewTable(String tableName) throws IllegalArgumentException {
		Matcher matcher = TABLEFORMATE.matcher(tableName);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("参数不合法，只允许字母，数字和下划线！");
		}
		try {
			terminalHistorDAO.createNewTable(tableName);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
	}

	@Override
	public int inserTerminalhistory(Terminalhistory terminalhistory) {
		int terminalHistoryId = 0;
		if (terminalhistory == null) {
			throw new NullPointerException("设备发来的消息不能为空！");
		}
		if (StringUtils.isBlank(terminalhistory.getTableName())) {
			throw new NullPointerException("动态表名不能为空！");
		}
		try {
			createNewTable(terminalhistory.getTableName());
			terminalHistorDAO.inserTerminalhistory(terminalhistory);
		} catch (IllegalArgumentException exception) {
			logger.error("Error in {}", exception);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return terminalHistoryId;
	}

	@Override
	public void updateTerminalParamCategory(Terminalhistory terminalhistory) {
		if (terminalhistory == null) {
			throw new NullPointerException("设备发来的消息不能为空！");
		}
		if (StringUtils.isBlank(terminalhistory.getTableName())) {
			throw new NullPointerException("动态表名不能为空！");
		}
		try {
			terminalHistorDAO.inserTerminalhistory(terminalhistory);
		} catch (IllegalArgumentException exception) {
			logger.error("Error in {}", exception);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
	}
	@Override
	public List<Terminalhistory> getTerminalhistorys(VTerminalhistory vTerminalhistory){
		List<Terminalhistory> Terminalhistorys = null;
		try {
			Terminalhistorys = terminalHistorDAO.getTerminalhistorys(vTerminalhistory);
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return Terminalhistorys == null ? new ArrayList<Terminalhistory>(0) : Terminalhistorys;
	}
}
