package com.partners.weather.dao;

import java.util.ArrayList;
import java.util.List;

import com.partners.entity.TerminalParamSettings;
import com.partners.entity.Terminalparameterscategory;
import com.partners.view.entity.VTerminalParam;

public interface ITeminalParamCategoryDAO {
	ArrayList<Terminalparameterscategory> getAllCategories();

	ArrayList<Terminalparameterscategory> getTerminalparamCategorys(VTerminalParam vTerminalParam);

	Terminalparameterscategory getTerminalparamCategory(int categoryId);

	void delTerminalparamCategory(int categoryId);

	int checkCategoryExists(int categoryId, String categoryName);

	int insertTerminalParamCategory(Terminalparameterscategory terminalparameterscategory);

	void updateTerminalParamCategory(Terminalparameterscategory terminalparameterscategory);
	
	Terminalparameterscategory getTerminalparamCategoryByName(String terminalParamCategoryName);	
	
	void delTerminalparamSettings(int categoryId);
	
	ArrayList<TerminalParamSettings> getTerminalParamSettings(int categoryId);
	
	int batchInsertTerminalParamSettings(List<TerminalParamSettings> terminalParamSettings);
 
	int getTerminalparamCategoryCount(VTerminalParam vTerminalParam);
	
	List<TerminalParamSettings> getAllTerminalParamSettings();	
}
