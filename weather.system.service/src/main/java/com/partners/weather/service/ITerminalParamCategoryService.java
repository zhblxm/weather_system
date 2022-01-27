package com.partners.weather.service;

import java.util.ArrayList;
import java.util.List;

import com.partners.entity.TerminalParamSettings;
import com.partners.entity.Terminalparameters;
import com.partners.entity.Terminalparameterscategory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VTerminalParam;

public interface ITerminalParamCategoryService {
	public ArrayList<Terminalparameterscategory> getAllCategories();

	public ArrayList<Terminalparameterscategory> getTerminalparamCategorys(VTerminalParam vTerminalParam);

	public Terminalparameterscategory getTerminalparamCategory(int categoryId);

	public void delTerminalparamCategory(int categoryId);

	public ResponseMsg insertTerminalParamCategory(Terminalparameterscategory terminalparameterscategory);

	public Terminalparameterscategory getTerminalparamCategoryByName(String terminalParamCategoryName);

	public ArrayList<TerminalParamSettings> getTerminalParamSettings(int categoryId);

	public ResponseMsg batchInsertTerminalParamSettings(List<TerminalParamSettings> terminalParamSettings);

	public ResponseMsg batchInsertTerminalParamSettings(int categoryId, Terminalparameters terminalparameter);

	public int getTerminalparamCategoryCount(VTerminalParam vTerminalParam);

	public List<TerminalParamSettings> getAllTerminalParamSettings();

	public void setTerminalParameterValue(int categoryId,Terminalparameters terminalparameter, List<TerminalParamSettings> terminalParamSettings);
	
	public void setTerminalParameterValue(int categoryId,List<Terminalparameters> terminalparameters, List<TerminalParamSettings> terminalParamSettings);
}
