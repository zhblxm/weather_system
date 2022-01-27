package com.partners.weather.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partners.entity.TerminalParamSettings;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.Terminalparameters;
import com.partners.entity.Terminalparameterscategory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VTerminalParam;
import com.partners.weather.dao.ITeminalParamCategoryDAO;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.service.ITerminalParamCategoryService;

@Service
@Transactional
public class TerminalParamCategoryServiceImp implements ITerminalParamCategoryService {
	private static final Logger logger = LoggerFactory.getLogger(TerminalParamCategoryServiceImp.class);
	@Autowired
	private ITeminalParamCategoryDAO teminalParamCategoryDAO;

	@Override
	public ArrayList<Terminalparameterscategory> getTerminalparamCategorys(VTerminalParam vTerminalParam) {
		ArrayList<Terminalparameterscategory> terminalparameterscategories = null;
		try {
			terminalparameterscategories = teminalParamCategoryDAO.getTerminalparamCategorys(vTerminalParam);
			for (Terminalparameterscategory category : terminalparameterscategories) {
				category.setCategoryUniqueId(HexUtil.IntToHex(category.getTerminalParamCategoryId()));
				category.setTerminalParamCategoryId(0);

			}
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return terminalparameterscategories == null ? new ArrayList<Terminalparameterscategory>(0) : terminalparameterscategories;
	}

	@Override
	public Terminalparameterscategory getTerminalparamCategory(int categoryId) {
		Terminalparameterscategory terminalparameterscategory = null;
		try {
			terminalparameterscategory = teminalParamCategoryDAO.getTerminalparamCategory(categoryId);
			if (terminalparameterscategory != null) {
				terminalparameterscategory.setCategoryUniqueId(HexUtil.IntToHex(terminalparameterscategory.getTerminalParamCategoryId()));
				terminalparameterscategory.setTerminalParamCategoryId(0);
			}
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return terminalparameterscategory;
	}

	@Override
	public void delTerminalparamCategory(int categoryId) {
		try {
			teminalParamCategoryDAO.delTerminalparamCategory(categoryId);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
	}

	@Override
	public ResponseMsg insertTerminalParamCategory(Terminalparameterscategory terminalparameterscategory) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		Terminalparameterscategory existsTerminalparameterscategory = null;
		try {
			int categoryId = terminalparameterscategory.getTerminalParamCategoryId();
			int categpruCount = teminalParamCategoryDAO.checkCategoryExists(categoryId, terminalparameterscategory.getTerminalParamCategoryName());
			if (categpruCount > 0) {
				responseMsg.setStatusCode(1);
				responseMsg.setMessage(terminalparameterscategory.getTerminalParamCategoryName() + "已经存在，请更换其它名字。");
				return responseMsg;
			}
			if (categoryId > 0) {
				existsTerminalparameterscategory = teminalParamCategoryDAO.getTerminalparamCategory(categoryId);
			}
			if (existsTerminalparameterscategory == null) {
				teminalParamCategoryDAO.insertTerminalParamCategory(terminalparameterscategory);

			} else {
				teminalParamCategoryDAO.updateTerminalParamCategory(terminalparameterscategory);
			}
			responseMsg.setMessageObject(terminalparameterscategory.getTerminalParamCategoryId());
		} catch (Exception exception) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("新增要素失败！");
			logger.error("Error in {}", exception);
		}
		return responseMsg;
	}

	@Override
	public ArrayList<Terminalparameterscategory> getAllCategories() {
		ArrayList<Terminalparameterscategory> terminalparameterscategories = null;
		try {
			terminalparameterscategories = teminalParamCategoryDAO.getAllCategories();
			if (terminalparameterscategories != null && terminalparameterscategories.size() > 0) {
				for (Terminalparameterscategory category : terminalparameterscategories) {
					category.setCategoryUniqueId(HexUtil.IntToHex(category.getTerminalParamCategoryId()));
					category.setTerminalParamCategoryId(0);
				}
			}
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return terminalparameterscategories == null ? new ArrayList<Terminalparameterscategory>(0) : terminalparameterscategories;
	}

	@Override
	public Terminalparameterscategory getTerminalparamCategoryByName(String terminalParamCategoryName) {
		Terminalparameterscategory terminalparameterscategory = null;
		try {
			terminalparameterscategory = teminalParamCategoryDAO.getTerminalparamCategoryByName(terminalParamCategoryName);
			if (terminalparameterscategory != null) {
				terminalparameterscategory.setCategoryUniqueId(HexUtil.IntToHex(terminalparameterscategory.getTerminalParamCategoryId()));
				terminalparameterscategory.setTerminalParamCategoryId(0);
			}
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return terminalparameterscategory;
	}

	@Override
	public ArrayList<TerminalParamSettings> getTerminalParamSettings(int categoryId) {

		ArrayList<TerminalParamSettings> terminalParamSettings = null;
		try {
			terminalParamSettings = teminalParamCategoryDAO.getTerminalParamSettings(categoryId);
			for (TerminalParamSettings terminalParam : terminalParamSettings) {
				terminalParam.setUniqueId(HexUtil.IntToHex(terminalParam.getTerminalParamCategoryId()));
				terminalParam.setTerminalParamCategoryId(0);
				terminalParam.setTerminalParamId(0);
			}
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return terminalParamSettings == null ? new ArrayList<TerminalParamSettings>(0) : terminalParamSettings;
	}

	@Override
	public ResponseMsg batchInsertTerminalParamSettings(List<TerminalParamSettings> terminalParamSettings) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			if (terminalParamSettings != null && terminalParamSettings.size() > 0) {
				teminalParamCategoryDAO.delTerminalparamSettings(terminalParamSettings.get(0).getTerminalParamCategoryId());
				teminalParamCategoryDAO.batchInsertTerminalParamSettings(terminalParamSettings);
				responseMsg.setMessageObject(terminalParamSettings.get(0).getTerminalParamCategoryId());
			}
		} catch (Exception exception) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("新增要素设置失败！");
			logger.error("Error in {}", exception);
		}
		return responseMsg;
	}

	@Override
	public ResponseMsg batchInsertTerminalParamSettings(int categoryId, Terminalparameters terminalparameter) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			if (terminalparameter != null && terminalparameter.getTerminalParametersAttrs() != null && terminalparameter.getTerminalParametersAttrs().size() > 0) {
				List<TerminalParamSettings> terminalParamSettings = new ArrayList<>(terminalparameter.getTerminalParametersAttrs().size());
				TerminalParamSettings terminalParamSetting;
				for (TerminalParametersAttrs terminalParametersAttr : terminalparameter.getTerminalParametersAttrs()) {
					terminalParamSetting = new TerminalParamSettings();
					terminalParamSetting.setTerminalParamCategoryId(categoryId);
					terminalParamSetting.setTerminalParamName(terminalParametersAttr.getName());
					terminalParamSettings.add(terminalParamSetting);
				}
				responseMsg = this.batchInsertTerminalParamSettings(terminalParamSettings);
				if(responseMsg.getStatusCode()==0)
				{
					List<Terminalparameters> parameters=new ArrayList<>(1);
					parameters.add(terminalparameter);
					this.setTerminalParameterValue(categoryId,parameters, terminalParamSettings);
				}
			}
		} catch (Exception exception) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("新增要素设置失败！");
			logger.error("Error in {}", exception);
		}
		return responseMsg;
	}

	@Override
	public int getTerminalparamCategoryCount(VTerminalParam vTerminalParam) {
		int paramCount = 0;
		try {
			paramCount = teminalParamCategoryDAO.getTerminalparamCategoryCount(vTerminalParam);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return paramCount;
	}

	@Override
	public List<TerminalParamSettings> getAllTerminalParamSettings() {
		List<TerminalParamSettings> terminalParamSettings = null;
		try {
			terminalParamSettings = teminalParamCategoryDAO.getAllTerminalParamSettings();
//			for (TerminalParamSettings terminalParam : terminalParamSettings) {
//				terminalParam.setUniqueId(HexUtil.IntToHex(terminalParam.getTerminalParamCategoryId()));
//				terminalParam.setUniqueParamId(HexUtil.IntToHex(terminalParam.getTerminalParamId()));
//				terminalParam.setTerminalParamCategoryId(0);
//				terminalParam.setTerminalParamId(0);
//			}
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return terminalParamSettings == null ? new ArrayList<TerminalParamSettings>(0) : terminalParamSettings;
	}

	@Override
	public void setTerminalParameterValue(int categoryId,List<Terminalparameters> terminalparameters, List<TerminalParamSettings> terminalParamSettings) {
		try {			
			Map<String, Integer> paramSettingMap=new HashMap<>();
			for (TerminalParamSettings parameterSetting : terminalParamSettings) {
				paramSettingMap.put(parameterSetting.getTerminalParamCategoryId()+"_"+parameterSetting.getTerminalParamName(), parameterSetting.getTerminalParamId());
			}
			for (Terminalparameters terminal : terminalparameters) {
				for (TerminalParametersAttrs parameter : terminal.getTerminalParametersAttrs()) {
					if(paramSettingMap.containsKey(categoryId+"_"+parameter.getName()))
					{
						parameter.setId(HexUtil.IntToHex(paramSettingMap.get(parameter.getName())));
					}
				}
			}
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
	}

	@Override
	public void setTerminalParameterValue(int categoryId, Terminalparameters terminalparameter, List<TerminalParamSettings> terminalParamSettings) {
		try {			
			Map<String, Integer> paramSettingMap=new HashMap<>();
			for (TerminalParamSettings parameterSetting : terminalParamSettings) {
				paramSettingMap.put(parameterSetting.getTerminalParamCategoryId()+"_"+parameterSetting.getTerminalParamName(), parameterSetting.getTerminalParamId());
			}	
			for (TerminalParametersAttrs parameter : terminalparameter.getTerminalParametersAttrs()) {
				if(paramSettingMap.containsKey(categoryId+"_"+parameter.getName()))
				{
					parameter.setId(HexUtil.IntToHex(paramSettingMap.get(categoryId+"_"+parameter.getName())));
				}				
			}
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		
	}

}
