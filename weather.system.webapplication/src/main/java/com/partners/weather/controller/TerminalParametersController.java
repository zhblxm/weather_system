package com.partners.weather.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.Adminuser;
import com.partners.entity.JsonResult;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.Terminalparameters;
import com.partners.entity.Terminalparameterscategory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VTerminalParam;
import com.partners.weather.common.CommonResources;
import com.partners.weather.common.MappingType;
import com.partners.weather.common.RedisKey;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.exception.FileFormartException;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.search.SearchIndexUtil;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.ITerminalParamCategoryService;
import com.partners.weather.xml.ParseXML;

import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
@RequestMapping("/terminal")
@UserPermission(value = UserPermissionEnum.PARAMETERGROUP)
public class TerminalParametersController {
	private static final Logger logger = LoggerFactory.getLogger(TerminalParametersController.class);
	@Resource
	ITerminalParamCategoryService terminalParamCategoryService;
	@Autowired
	JedisPool jedisPool;

	@RequestMapping("/manage")
	@UserPermission(value = UserPermissionEnum.PARAMETERGROUPSELECT)
	@UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "查询要素分类")
	public String manage(HttpServletRequest request) {
		Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
		request.setAttribute("Update", 0);
		request.setAttribute("Delete", 0);
		request.setAttribute("View", 0);
		if (adminuser.getPermissions().contains(UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE.getId())) {
			request.setAttribute("Update", 1);
		}
		if (adminuser.getPermissions().contains(UserPermissionEnum.PARAMETERGROUPDELETE.getId())) {
			request.setAttribute("Delete", 1);
		}
		if (adminuser.getPermissions().contains(UserPermissionEnum.TERMINALPARAMETERSELECT.getId())) {
			request.setAttribute("View", 1);
		}
		return "terminalparamcategorylist";
	}

	@RequestMapping("/categories")
	@UserPermission(value = UserPermissionEnum.PARAMETERGROUPSELECT)
	@UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "查询要素分类")
	@ResponseBody
	public JsonResult categories(HttpServletRequest request) {
		VTerminalParam vTerminalParam = new VTerminalParam(RequestHelper.prepareRequest(request));
		ArrayList<Terminalparameterscategory> terminalparameterscategories = terminalParamCategoryService.getTerminalparamCategorys(vTerminalParam);
		for (Terminalparameterscategory terminalparameterscategory : terminalparameterscategories) {
			terminalparameterscategory.setIsSystem(0);
			terminalparameterscategory.setMappingName(null);
		}
		int draw = StringUtils.isBlank(request.getParameter("draw")) ? 1 : Integer.valueOf(request.getParameter("draw"));
		int count = terminalParamCategoryService.getTerminalparamCategoryCount(vTerminalParam);
		JsonResult jsonResult = new JsonResult();
		jsonResult.setDraw(draw);
		jsonResult.setRecordsTotal(count);
		jsonResult.setRecordsFiltered(count);
		jsonResult.setData(terminalparameterscategories.toArray());
		return jsonResult;
	}

	@RequestMapping("/addcategory")
	@UserPermission(value = UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "添加和更新要素分类")
	public String addcategory(HttpServletRequest request) {
		return "terminalparamcategory";
	}

	@RequestMapping("/detail/{categoryUniqueId}")
	@UserPermission(value = UserPermissionEnum.TERMINALPARAMETERSELECT)
	@UserAction(Action = UserPermissionEnum.TERMINALPARAMETER, Description = "要素查询")
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
		return "terminal";
	}

	@RequestMapping("/uploadterminalcategory")
	@UserPermission(value = UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE)
	@UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "添加和更新要素")
	@ResponseBody
	public ResponseMsg uploadterminalcategory(@RequestParam(value = "fileTerminalCategor", required = false) MultipartFile multipartFile, HttpServletRequest request) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		Jedis client = null;
		if (multipartFile == null) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("需要上传的文件不能为空。");
			return responseMsg;
		}
		String saveFilePathDir = request.getSession().getServletContext().getRealPath("/resources/terminalconfig");
		File saveTerminalCategoryFile = new File(saveFilePathDir);
		if (!saveTerminalCategoryFile.exists() && !saveTerminalCategoryFile.isDirectory()) {
			saveTerminalCategoryFile.mkdirs();
		}
		String suffix = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
		if (!suffix.toLowerCase().endsWith("xml")) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("必须为xml格式，其他格式无效！");
			return responseMsg;
		}
		saveFilePathDir += File.separator;
		File checkFile = new File(saveFilePathDir + multipartFile.getOriginalFilename());
		if (checkFile.exists()) {
			saveTerminalCategoryFile = new File(saveFilePathDir + UUID.randomUUID().toString() + suffix);
		} else {
			saveTerminalCategoryFile = new File(saveFilePathDir + multipartFile.getOriginalFilename());
		}
		try {
			multipartFile.transferTo(saveTerminalCategoryFile);
		} catch (IllegalStateException | IOException e) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("上传文件失败，请稍后再试！");
			logger.error("Error in {}", e);
		}
		if (responseMsg.getStatusCode() == 0) {
			try {
				Terminalparameters terminalparameters = ParseXML.parseXMLFile(saveTerminalCategoryFile);
				Terminalparameterscategory terminalparameterscategory = terminalParamCategoryService.getTerminalparamCategoryByName(terminalparameters.getName());
				if (terminalparameterscategory != null) {
					responseMsg.setStatusCode(1);
					responseMsg.setMessage(String.format("名字为%s的配置已经存在，请更换一个其他名字！", terminalparameters.getName()));
					saveTerminalCategoryFile.delete();
					return responseMsg;
				}
				terminalparameterscategory = new Terminalparameterscategory();
				terminalparameterscategory.setIsSystem(1);
				terminalparameterscategory.setTerminalParamCategoryName(terminalparameters.getName());
				terminalparameterscategory.setMappingName(saveTerminalCategoryFile.getName().replaceAll("\\s*", "").replaceAll("[.][^.]+$", ""));
				responseMsg = terminalParamCategoryService.insertTerminalParamCategory(terminalparameterscategory);
				if (responseMsg.getStatusCode() != 0) {
					responseMsg.setStatusCode(1);
					responseMsg.setMessage("保存文件失败，请稍后再试！");
					saveTerminalCategoryFile.delete();
					return responseMsg;
				}
				int categoryId = (int) responseMsg.getMessageObject();
				responseMsg = terminalParamCategoryService.batchInsertTerminalParamSettings(categoryId, terminalparameters);
				if (responseMsg.getStatusCode() != 0) {
					responseMsg.setStatusCode(1);
					responseMsg.setMessage("保存文件详细信息配置失败，请稍后再试！");
					saveTerminalCategoryFile.delete();
					return responseMsg;
				}
				ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
				RedisPoolManager.Init(jedisPool);
				client = RedisPoolManager.getJedis();
				client.set(String.valueOf(categoryId).getBytes(), objectSerializeTransfer.serialize(terminalparameters));
				Map<String, String> terminalCatetoryMap = new HashMap<>();
				terminalCatetoryMap.put(String.valueOf(categoryId), terminalparameterscategory.getMappingName());
				client.hmset(RedisKey.ALLCATETORY, terminalCatetoryMap);
				if (SearchIndexUtil.createIndex(MappingType.INDEX)) {
					SearchIndexUtil.createMapping(categoryId, terminalparameters, MappingType.INDEX);
				}
			} catch (FileFormartException | ParserConfigurationException | IOException | BadHanyuPinyinOutputFormatCombination e) {
				responseMsg.setStatusCode(1);
				responseMsg.setMessage("解析文件失败，请稍后再试！");
				logger.error("Error in {}", e);
			} finally {
				RedisPoolManager.close(client);
			}
		}
		return responseMsg;
	}

	@RequestMapping("/delete/{categoryUniqueId}")
	@UserPermission(value = UserPermissionEnum.PARAMETERGROUPDELETE)
	@UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "删除要素分类")
	@ResponseBody
	public ResponseMsg delete(HttpServletRequest request, @PathVariable("categoryUniqueId") String categoryUniqueId) {
		ResponseMsg responseMsg = new ResponseMsg();
		int categoryId = HexUtil.HexToInt(categoryUniqueId);
		Terminalparameterscategory category = this.terminalParamCategoryService.getTerminalparamCategory(categoryId);
		this.terminalParamCategoryService.delTerminalparamCategory(categoryId);
		if (category != null) {
			String parameFilePath = request.getSession().getServletContext().getRealPath("/resources/terminalconfig/" + category.getMappingName() + ".xml");
			File terminalCategoryFile = new File(parameFilePath);
			if (terminalCategoryFile.exists()) {
				terminalCategoryFile.delete();
			}
		}
		responseMsg.setStatusCode(0);
		responseMsg.setMessage("删除成功");
		return responseMsg;
	}
    @RequestMapping("/download/{categoryUniqueId}")    
	@UserPermission(value = UserPermissionEnum.PARAMETERGROUPSELECT)
	@UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "查询要素分类")
    public ResponseEntity<byte[]> download(HttpServletRequest request, @PathVariable("categoryUniqueId") String categoryUniqueId) throws IOException {    
        HttpHeaders headers = new HttpHeaders();    
        int categoryId = HexUtil.HexToInt(categoryUniqueId);
		Terminalparameterscategory category = this.terminalParamCategoryService.getTerminalparamCategory(categoryId);
		byte[] fileBytes=null;
		if(category!=null && !StringUtils.isBlank(category.getMappingName()))
		{
		    String path = request.getSession().getServletContext().getRealPath("/resources/terminalconfig/"+category.getMappingName()+".xml");
		    File categoryFile = new File(path);
		    if(categoryFile.exists())
		    {
		    	String fileName=new String((category.getMappingName()+".xml").getBytes("UTF-8"),"iso-8859-1");
		    	headers.setContentDispositionFormData("attachment", fileName); 
		    	fileBytes=FileUtils.readFileToByteArray(categoryFile);
		    }
		}
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);   
        fileBytes=fileBytes==null?new byte[0]:fileBytes;
        return new ResponseEntity<byte[]>(fileBytes, headers, HttpStatus.CREATED);    
    } 
}
