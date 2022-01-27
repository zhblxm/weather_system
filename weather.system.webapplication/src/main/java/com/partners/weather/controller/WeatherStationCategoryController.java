package com.partners.weather.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.JsonResult;
import com.partners.entity.Location;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VWeatherstationcategory;
import com.partners.weather.Json.JsonHelper;
import com.partners.weather.common.CommonResources;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.service.IWeatherStationService;

@Controller
@RequestMapping("/weatherStationcategory")
@UserPermission(value = UserPermissionEnum.STATIONCATEGORY)
public class WeatherStationCategoryController {
	private static final Logger logger = LoggerFactory.getLogger(WeatherStationCategoryController.class);
	@Autowired
	IWeatherStationService weatherStationService;

	@RequestMapping
	@UserPermission(value = UserPermissionEnum.STATIONCATEGORYSELECT)
	@UserAction(Action = UserPermissionEnum.STATIONCATEGORY, Description = "查询站点分类")
	public String manage(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		request.setAttribute("Update", 0);
		request.setAttribute("Delete", 0);
		if (adminuser.getPermissions().contains(UserPermissionEnum.STATIONCATEGORYINSERTANDUPDATE.getId())) {
			request.setAttribute("Update", 1);
		}
		if (adminuser.getPermissions().contains(UserPermissionEnum.STATIONCATEGORYDELETE.getId())) {
			request.setAttribute("Delete", 1);
		}
		return "weacherstationcategorylist";
	}
	@RequestMapping("/categories")
	@UserPermission(value = UserPermissionEnum.STATIONCATEGORYSELECT)
	@UserAction(Action = UserPermissionEnum.STATIONCATEGORY, Description = "查询站点分类")
	@ResponseBody
	public JsonResult categories(HttpServletRequest request) {	
		VWeatherstationcategory vWeatherstationcategory = new VWeatherstationcategory(RequestHelper.prepareRequest(request));
		if(logger.isInfoEnabled()){
			logger.info(String.format("Request name is %s:%s", vWeatherstationcategory.getName(), request.getParameter("search[value]")));
		}
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		if(!StringUtils.isBlank(adminuser.getWeatherStationGroup()) && !"all".equalsIgnoreCase(adminuser.getWeatherStationGroup())){
			List<Integer> userCategories = Lists.transform(Arrays.asList(adminuser.getWeatherStationGroup().split(",")), new Function<String, Integer>() {
				public Integer apply(String e) {
					return HexUtil.HexToInt(e);
				}
			});
			vWeatherstationcategory.setCategories(userCategories);
		}		
		List<Weatherstationcategory> weatherstationcategories = weatherStationService.getWeatherStationCategorys(vWeatherstationcategory);
		weatherstationcategories=weatherstationcategories == null ? new ArrayList<Weatherstationcategory>(0) : weatherstationcategories;
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		int count = weatherStationService.getWeatherStationCategoryCount(vWeatherstationcategory);
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(count);
		jsonResult.setRecordsFiltered(count);
		jsonResult.setData(weatherstationcategories.toArray());
		return jsonResult;		
	}
	
	@RequestMapping("/add")
	@UserPermission(value = UserPermissionEnum.STATIONCATEGORYINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.STATIONCATEGORY, Description = "添加和更新站点分类")
	public String add(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		List<Weatherstationcategory> weatherstationcategories=weatherStationService.getAllWeatherCatsExcludeCategory(0);
		weatherstationcategories=weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(),true);
		request.setAttribute("Categories", weatherstationcategories);	
		return "weatherstationcategory";
	}

	@RequestMapping("/update/{categoryUniqueId}")
	@UserPermission(value = UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.STATIONCATEGORY, Description = "添加和更新站点分类")
	public String update(HttpServletRequest request, @PathVariable("categoryUniqueId") String categoryUniqueId) throws Exception {
		int categoryId = HexUtil.HexToInt(categoryUniqueId);
		Weatherstationcategory weatherstationcategory = weatherStationService.getWeatherStationCategory(categoryId);
		if (weatherstationcategory == null) {
			throw new NullPointerException("站点分类不存在");
		}
		request.setAttribute("Catetory", weatherstationcategory);
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		List<Weatherstationcategory> weatherstationcategories=weatherStationService.getAllWeatherCatsExcludeCategory(categoryId);
		weatherstationcategories=weatherStationService.filterWeatherStationCategorys(weatherstationcategories, adminuser.getWeatherStationGroup(),true);
		request.setAttribute("Categories", weatherstationcategories);	
		return "weatherstationcategory";
	}
	@RequestMapping("/map")
	@UserPermission(value = UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.STATIONCATEGORY, Description = "添加和更新站点分类")
	public String map(HttpServletRequest request) {
		request.setAttribute("MapImgHost", CommonResources.MapImageHost);
		return "map";		
	}
	@RequestMapping("/mapsearch")
	@UserPermission(value = UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.STATIONCATEGORY, Description = "添加和更新站点分类")
	@ResponseBody
	public List<Location> mapsearch(HttpServletRequest request) {
		String word=request.getParameter("word");	
		List<Location> locations=JsonHelper.findCity(word);
		return locations;	
	}
	@RequestMapping("/asynsavecategory")
	@UserPermission(value = UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.STATIONCATEGORY, Description = "添加和更新站点分类")
	@ResponseBody
	public ResponseMsg save(@RequestBody Weatherstationcategory weatherstationcategory) {
		ResponseMsg responseMsg = new ResponseMsg();
		if (StringUtils.isBlank(weatherstationcategory.getWeatherStationCategoryName())) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("站点分类名称不能为空。");
			return responseMsg;
		}
		int categoryId = 0,parentCategoryId=0;
		if (!StringUtils.isBlank(weatherstationcategory.getUniqueCategoryId())) {
			categoryId = HexUtil.HexToInt(weatherstationcategory.getUniqueCategoryId());
		}
		if (!StringUtils.isBlank(weatherstationcategory.getUniqueParentId())) {
			parentCategoryId = HexUtil.HexToInt(weatherstationcategory.getUniqueParentId());
		}
		weatherstationcategory.setWeatherStationCategoryId(categoryId);
		weatherstationcategory.setParentCategoryId(parentCategoryId);
		responseMsg = weatherStationService.insertWeatherStationCategory(weatherstationcategory);
		return responseMsg;
	}

	@RequestMapping("/delete/{categoryUniqueId}")
	@UserPermission(value = UserPermissionEnum.STATIONCATEGORYDELETE)
	@UserAction(Action = UserPermissionEnum.STATIONCATEGORY, Description = "删除站点分类")
	@ResponseBody
	public ResponseMsg delete(HttpServletRequest request, @PathVariable("categoryUniqueId") String categoryUniqueId) {
		ResponseMsg responseMsg = new ResponseMsg();
		int categoryId = HexUtil.HexToInt(categoryUniqueId);
		responseMsg=this.weatherStationService.delWeatherStationCategory(categoryId);
		return responseMsg;
	}
}
