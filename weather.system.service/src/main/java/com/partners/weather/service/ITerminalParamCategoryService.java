package com.partners.weather.service;

import com.partners.entity.TerminalParamSettings;
import com.partners.entity.Terminalparameters;
import com.partners.entity.Terminalparameterscategory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VTerminalParam;

import java.util.ArrayList;
import java.util.List;

public interface ITerminalParamCategoryService {
    ArrayList<Terminalparameterscategory> getAllCategories();

    ArrayList<Terminalparameterscategory> getTerminalparamCategorys(VTerminalParam vTerminalParam);

    Terminalparameterscategory getTerminalparamCategory(int categoryId);

    void delTerminalparamCategory(int categoryId);

    ResponseMsg insertTerminalParamCategory(Terminalparameterscategory terminalparameterscategory);

    Terminalparameterscategory getTerminalparamCategoryByName(String terminalParamCategoryName);

    ArrayList<TerminalParamSettings> getTerminalParamSettings(int categoryId);

    ResponseMsg batchInsertTerminalParamSettings(List<TerminalParamSettings> terminalParamSettings);

    ResponseMsg batchInsertTerminalParamSettings(int categoryId, Terminalparameters terminalparameter);

    int getTerminalparamCategoryCount(VTerminalParam vTerminalParam);

    List<TerminalParamSettings> getAllTerminalParamSettings();

    void setTerminalParameterValue(int categoryId, Terminalparameters terminalparameter, List<TerminalParamSettings> terminalParamSettings);

    void setTerminalParameterValue(int categoryId, List<Terminalparameters> terminalparameters, List<TerminalParamSettings> terminalParamSettings);
}
