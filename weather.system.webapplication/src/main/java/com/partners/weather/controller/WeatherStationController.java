package com.partners.weather.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.JsonResult;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.Terminalparameters;
import com.partners.entity.Terminalparameterscategory;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VWeatherStation;
import com.partners.weather.common.CommonResources;
import com.partners.weather.common.RedisKey;
import com.partners.weather.common.ResultCode;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.protocol.ComponentManager;
import com.partners.weather.protocol.ComponentStatus;
import com.partners.weather.protocol.GeneralComponent;
import com.partners.weather.protocol.ProtocolImp;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.ITerminalParamCategoryService;
import com.partners.weather.service.IWeatherStationService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
@RequestMapping("/weatherstation")
@UserPermission(value = UserPermissionEnum.STATION)
public class WeatherStationController {
	private static final Logger logger = LoggerFactory.getLogger(WeatherStationController.class);
	@Autowired
	IWeatherStationService weatherStationService;
	@Autowired
	ITerminalParamCategoryService terminalParamCategoryService;
	@Autowired
	JedisPool jedisPool;

	@RequestMapping
	@UserPermission(value = UserPermissionEnum.STATIONSELECT)
	@UserAction(Action = UserPermissionEnum.STATION, Description = "查询站点")
	public String manage(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		request.setAttribute("Update", 0);
		request.setAttribute("Delete", 0);
		if (adminuser.getPermissions().contains(UserPermissionEnum.STATIONINSERTANDUPDATE.getId())) {
			request.setAttribute("Update", 1);
		}
		if (adminuser.getPermissions().contains(UserPermissionEnum.STATIONDELETE.getId())) {
			request.setAttribute("Delete", 1);
		}
		return "weacherstationlist";
	}

	@RequestMapping("/stations")
	@UserPermission(value = UserPermissionEnum.STATIONSELECT)
	@UserAction(Action = UserPermissionEnum.STATION, Description = "查询站点")
	@ResponseBody
	public JsonResult stations(HttpServletRequest request) {
		VWeatherStation weatherStation = new VWeatherStation(RequestHelper.prepareRequest(request));
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		if (!StringUtils.isBlank(adminuser.getWeatherStation()) && !"all".equalsIgnoreCase(adminuser.getWeatherStation())) {
			List<Integer> userStations = Lists.transform(Arrays.asList(adminuser.getWeatherStation().split(",")), new Function<String, Integer>() {
				public Integer apply(String e) {
					return HexUtil.HexToInt(e);
				}
			});
			weatherStation.setStations(userStations);
		}
		List<Weatherstation> weatherStations = weatherStationService.getWeatherStations(weatherStation);
		weatherStations = weatherStations == null ? new ArrayList<Weatherstation>(0) : weatherStations;
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		int count = weatherStationService.getWeatherStationCount(weatherStation);
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(count);
		jsonResult.setRecordsFiltered(count);
		jsonResult.setData(weatherStations.toArray());
		return jsonResult;
	}

	@RequestMapping("/add")
	@UserPermission(value = UserPermissionEnum.STATIONCATEGORYINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.STATION, Description = "添加和更新站点")
	public String add(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		List<Weatherstationcategory> weatherstationcategories = weatherStationService.getAllWeatherCatsExcludeCategory(0);
		weatherstationcategories = weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(), true);
		request.setAttribute("Categories", weatherstationcategories);
		return "weatherstation";
	}

	@RequestMapping("/getTerminalCategories")
	@UserPermission(value = UserPermissionEnum.STATIONCATEGORYINSERTANDUPDATE)
	@ResponseBody
	public ArrayList<Terminalparameterscategory> getTerminalCategories(HttpServletRequest request) {
		ArrayList<Terminalparameterscategory> categories = terminalParamCategoryService.getAllCategories();
		for (Terminalparameterscategory terminalparameterscategory : categories) {
			terminalparameterscategory.setIsSystem(0);
			terminalparameterscategory.setMappingName(null);
		}
		return categories;
	}

	@RequestMapping("/update/{uniqueWeatherStationId}")
	@UserPermission(value = UserPermissionEnum.STATIONCATEGORYINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.STATION, Description = "添加和更新站点")
	public String update(HttpServletRequest request, @PathVariable("uniqueWeatherStationId") String uniqueWeatherStationId) throws Exception {
		int weatherStationId = HexUtil.HexToInt(uniqueWeatherStationId);
		Weatherstation weatherstation = weatherStationService.getWeatherStation(weatherStationId);
		if (weatherstation == null) {
			throw new NullPointerException("站点不存在");
		}
		request.setAttribute("Station", weatherstation);
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		List<Weatherstationcategory> weatherstationcategories = weatherStationService.getAllWeatherCatsExcludeCategory(0);
		weatherstationcategories = weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(), true);
		request.setAttribute("Categories", weatherstationcategories);
		String coverFilePathDir = request.getSession().getServletContext().getRealPath("/resources/cover/" + weatherStationId + ".png");
		File coverFile = new File(coverFilePathDir);
		request.setAttribute("Cover", 0);
		if (coverFile.exists()) {
			request.setAttribute("Cover", weatherStationId);
		}
		return "weatherstation";
	}

	@RequestMapping("/asynsavestation")
	@UserPermission(value = UserPermissionEnum.STATIONCATEGORYINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.STATION, Description = "添加和更新站点")
	@ResponseBody
	public ResponseMsg saveStation(@RequestBody Weatherstation weatherstation) {
		ResponseMsg responseMsg = new ResponseMsg();
		Weatherstation origWeatherstation = null;
		if (StringUtils.isBlank(weatherstation.getWeatherStationName())) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("站点名称不能为空。");
			return responseMsg;
		}
		if (StringUtils.isBlank(weatherstation.getgRPSPort())) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("GRPS端口不能为空。");
			return responseMsg;
		}
		int weatherStationCategoryId = 0, weatherStationId = 0;
		if (!StringUtils.isBlank(weatherstation.getUniqueWeatherStationId())) {
			weatherStationId = HexUtil.HexToInt(weatherstation.getUniqueWeatherStationId());
		}
		if (!StringUtils.isBlank(weatherstation.getUniqueCategoryId())) {
			weatherStationCategoryId = HexUtil.HexToInt(weatherstation.getUniqueCategoryId());
		}
		if (weatherStationId > 0) {
			origWeatherstation = weatherStationService.getWeatherStation(weatherStationId);
		}
		int port = 0;
		try {
			port = Integer.valueOf(weatherstation.getgRPSPort().trim());
		} catch (NumberFormatException ex) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("GRPS端口不能大于65536");
			return responseMsg;
		}
		if (port <= 0 || port > 65536) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("GRPS端口必须大于0，小于65536");
			return responseMsg;
		}
		List<String> terminalModels = new ArrayList<>();
		for (WeatherStationTerminal wsTerminal : weatherstation.getWeatherStationTerminals()) {
			if (terminalModels.contains(wsTerminal.getTerminalModel().trim())) {
				responseMsg.setStatusCode(2);
				responseMsg.setMessage("设备编号(" + wsTerminal.getTerminalModel() + ")必须填写并且全系统唯一！");
				break;
			} else {
				terminalModels.add(wsTerminal.getTerminalModel().trim());
			}
		}
		terminalModels.clear();
		if (responseMsg.getStatusCode() > 0) {
			return responseMsg;
		}
		weatherstation.setgRPSPort(weatherstation.getgRPSPort().trim());
		weatherstation.setWeatherStationId(weatherStationId);
		weatherstation.setWeatherStationCategoryId(weatherStationCategoryId);
		GeneralComponent component=ComponentManager.getInstance().getComponent(port);
		int portUseCount=this.weatherStationService.getWeatherStationCountByPort(port);
		if (origWeatherstation != null && !origWeatherstation.getgRPSPort().equals(weatherstation.getgRPSPort()) && portUseCount==1) {
			ComponentManager.getInstance().stopComponent(Integer.valueOf(origWeatherstation.getgRPSPort()));
		} 
		boolean blnHastStar = false;
		if(component==null || component.getStatus()!=ComponentStatus.Started){
			component = new ProtocolImp(port);
			blnHastStar = true;
			ResultCode resultCode = ComponentManager.getInstance().startComponent(component);
			if (resultCode != resultCode.OK) {
				responseMsg.setStatusCode(3);
				responseMsg.setMessage("端口(" + port + ")无法启动,请更换端口！");
			}
		}
		if (responseMsg.getStatusCode() == 0) {
			responseMsg = weatherStationService.insertWeatherStation(weatherstation);
			if (responseMsg.getStatusCode() != 0 && blnHastStar) {
				ComponentManager.getInstance().stopComponent(Integer.valueOf(component.getPort()));
			}
		}
		if (responseMsg.getStatusCode() != 0 && StringUtils.isBlank(responseMsg.getMessage())) {
			responseMsg.setMessage("站点信息保存失败。");
		}
		return responseMsg;
	}

	@RequestMapping("/detail/{categoryUniqueId}")
	@UserPermission(value = UserPermissionEnum.STATIONSELECT)
	@UserAction(Action = UserPermissionEnum.STATION, Description = "查询站点")
	public String detail(HttpServletRequest request, @PathVariable("categoryUniqueId") String categoryUniqueId) {
		Jedis client = null;
		try {
			ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			int categoryId = HexUtil.HexToInt(categoryUniqueId);
			if (client.exists(String.valueOf(categoryId).getBytes())) {
				byte[] parameters = client.get(String.valueOf(categoryId).getBytes());
				if (parameters != null && parameters.length > 0) {
					Terminalparameters terminalparameter = (Terminalparameters) objectSerializeTransfer.deserialize(parameters);
					if (terminalparameter != null && terminalparameter.getTerminalParametersAttrs().size() > 0) {
						List<TerminalParametersAttrs> parametersAttrs = new ArrayList<>(terminalparameter.getTerminalParametersAttrs().size());
						for (TerminalParametersAttrs terminalParametersAttr : terminalparameter.getTerminalParametersAttrs()) {
							if (!"N".equalsIgnoreCase(terminalParametersAttr.getShowInPage())) {
								parametersAttrs.add(terminalParametersAttr);
							}
						}
						if (parametersAttrs.size() > 0) {
							request.setAttribute("Parameters", parametersAttrs);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		} finally {
			RedisPoolManager.close(client);
		}
		request.setAttribute("row", request.getParameter("row"));
		return "parametersetting";
	}

	@RequestMapping("/delete/{uniqueWeatherStationId}")
	@UserPermission(value = UserPermissionEnum.STATIONCATEGORYDELETE)
	@UserAction(Action = UserPermissionEnum.STATIONCATEGORY, Description = "删除站点分类")
	@ResponseBody
	public ResponseMsg delete(HttpServletRequest request, @PathVariable("uniqueWeatherStationId") String uniqueWeatherStationId) {
		ResponseMsg responseMsg = new ResponseMsg();
		Jedis client = null;
		int weatherStationId = HexUtil.HexToInt(uniqueWeatherStationId);
		Weatherstation origWeatherstation = null;
		if (weatherStationId > 0) {
			origWeatherstation = weatherStationService.getWeatherStation(weatherStationId);
			if (origWeatherstation != null && !StringUtils.isBlank(origWeatherstation.getgRPSPort())) {
				ComponentManager.getInstance().stopComponent(Integer.valueOf(origWeatherstation.getgRPSPort()));
			}
		}
		responseMsg = this.weatherStationService.delWeatherStation(weatherStationId);
		if (responseMsg.getStatusCode() == 0) {
			String coverFilePathDir = request.getSession().getServletContext().getRealPath("/resources/cover/" + weatherStationId + ".png");
			File coverFile = new File(coverFilePathDir);
			if (coverFile.exists()) {
				coverFile.delete();
			}
		}
		try {
			if (responseMsg.getStatusCode() == 0 && origWeatherstation != null) {
				RedisPoolManager.Init(jedisPool);
				client = RedisPoolManager.getJedis();
				if (client.exists(RedisKey.ALLWEATHERSYSTEMKEY.getBytes())) {
					client.hdel(RedisKey.ALLWEATHERSYSTEMKEY.getBytes(), origWeatherstation.getWeatherStationNumber().getBytes());
				}
				if (client.exists(RedisKey.ALLSTATION.getBytes())) {
					client.hdel(RedisKey.ALLSTATION.getBytes(), origWeatherstation.getWeatherStationNumber().getBytes());
				}
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		} finally {
			RedisPoolManager.close(client);
		}
		return responseMsg;
	}

	@RequestMapping("/upload/{uniqueWeatherStationId}")
	@UserPermission(value = UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "添加和更新要素")
	@ResponseBody
	public ResponseMsg upload(@RequestParam(value = "fileCover", required = false) MultipartFile multipartFile, HttpServletRequest request, @PathVariable("uniqueWeatherStationId") String uniqueWeatherStationId) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		if (multipartFile == null) {
			return responseMsg;
		}
		// Map<String, String> map=request.getParameterMap();
		// Iterator<String> ite=map.keySet().iterator();
		// while (ite.hasNext()) {
		// String key=ite.next();
		// String[] values=request.getParameterValues(key);
		// System.out.println(String.format("%s:%s", key,values[0]));
		// }
		int weatherStationId = HexUtil.HexToInt(uniqueWeatherStationId);
		String coverFilePathDir = request.getSession().getServletContext().getRealPath("/resources/cover/" + weatherStationId + ".png");
		File coverFile = new File(coverFilePathDir);
		if (coverFile.exists()) {
			coverFile.delete();
		}
		try {
			multipartFile.transferTo(coverFile);
		} catch (IllegalStateException | IOException e) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("上传文件失败，请稍后再试！");
			logger.error("Error in {}", e);
		}
		return responseMsg;
	}

	@RequestMapping("/validimage")
	@ResponseBody
	public ResponseMsg validimage(@RequestParam(value = "fileCover", required = false) MultipartFile multipartFile, HttpServletRequest request) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		if (multipartFile == null) {
			return responseMsg;
		}
		String extName = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
		if (!extName.equalsIgnoreCase("jpg") && !extName.equalsIgnoreCase("jpeg") && !extName.equalsIgnoreCase("png")) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("图片仅支持jpg,jpen,png格式。");
		} else if (multipartFile.getSize() > 500 * 1024) {
			responseMsg.setStatusCode(2);
			responseMsg.setMessage("图片大小必须为500kb以内。");
		}
		return responseMsg;
	}
}
