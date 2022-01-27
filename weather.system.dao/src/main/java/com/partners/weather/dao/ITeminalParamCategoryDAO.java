package com.partners.weather.dao;

import java.util.ArrayList;
import java.util.List;

import com.partners.entity.TerminalParamSettings;
import com.partners.entity.Terminalparameterscategory;
import com.partners.view.entity.VTerminalParam;

public interface ITeminalParamCategoryDAO {
	public ArrayList<Terminalparameterscategory> getAllCategories();

	public ArrayList<Terminalparameterscategory> getTerminalparamCategorys(VTerminalParam vTerminalParam);

	public Terminalparameterscategory getTerminalparamCategory(int categoryId);

	public void delTerminalparamCategory(int categoryId);

	public int checkCategoryExists(int categoryId, String categoryName);

	public int insertTerminalParamCategory(Terminalparameterscategory terminalparameterscategory);

	public void updateTerminalParamCategory(Terminalparameterscategory terminalparameterscategory);
	
	public Terminalparameterscategory getTerminalparamCategoryByName(String terminalParamCategoryName);	
	
	public void delTerminalparamSettings(int categoryId);
	
	public ArrayList<TerminalParamSettings> getTerminalParamSettings(int categoryId);
	
	public int batchInsertTerminalParamSettings(List<TerminalParamSettings> terminalParamSettings);
 
	public int getTerminalparamCategoryCount(VTerminalParam vTerminalParam);
	
	public List<TerminalParamSettings> getAllTerminalParamSettings();	
}
