package com.partners.weather.controller;

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
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.request.RequestHelper;
import com.partners.weather.search.SearchIndexUtil;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.ITerminalParamCategoryService;
import com.partners.weather.xml.ParseXML;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Controller
@RequestMapping("/terminal")
@UserPermission(value = UserPermissionEnum.PARAMETERGROUP)
@Slf4j
public class TerminalParametersController {

    @Resource
    private ITerminalParamCategoryService terminalParamCategoryService;
    @Autowired
    private JedisPool jedisPool;

    @RequestMapping("/manage")
    @UserPermission(value = UserPermissionEnum.PARAMETERGROUPSELECT)
    @UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "??????????????????")
    public String manage(HttpServletRequest request) {
        Adminuser adminuser = (Adminuser) request.getSession().getAttribute(CommonResources.ADMINUSERKEY);
        request.setAttribute("Update", adminuser.getPermissions().contains(UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE.getId()) ? 1 : 0);
        request.setAttribute("Delete", adminuser.getPermissions().contains(UserPermissionEnum.PARAMETERGROUPDELETE.getId()) ? 1 : 0);
        request.setAttribute("View", adminuser.getPermissions().contains(UserPermissionEnum.TERMINALPARAMETERSELECT.getId()) ? 1 : 0);
        return "terminalparamcategorylist";
    }

    @RequestMapping("/categories")
    @UserPermission(value = UserPermissionEnum.PARAMETERGROUPSELECT)
    @UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "??????????????????")
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
        return JsonResult.builder().draw(draw).recordsTotal(count).recordsFiltered(count).data(terminalparameterscategories.toArray()).build();
    }

    @RequestMapping("/addcategory")
    @UserPermission(value = UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE)
    @UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "???????????????????????????")
    public String addcategory(HttpServletRequest request) {
        return "terminalparamcategory";
    }

    @RequestMapping("/detail/{categoryUniqueId}")
    @UserPermission(value = UserPermissionEnum.TERMINALPARAMETERSELECT)
    @UserAction(Action = UserPermissionEnum.TERMINALPARAMETER, Description = "????????????")
    public String detail(HttpServletRequest request, @PathVariable("categoryUniqueId") String categoryUniqueId) {
        ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
        RedisPoolManager.Init(jedisPool);
        Jedis client = RedisPoolManager.getJedis();
        try {
            int categoryId = HexUtil.HexToInt(categoryUniqueId);
            byte[] parameters = null;
            List<TerminalParametersAttrs> parametersAttrs = null;
            if (client.exists(String.valueOf(categoryId).getBytes())) {
                parameters = client.get(String.valueOf(categoryId).getBytes());
            }
            if (ArrayUtils.isNotEmpty(parameters)) {
                parametersAttrs = ((Terminalparameters) objectSerializeTransfer.deserialize(parameters)).getTerminalParametersAttrs()
                        .stream().filter(terminalParametersAttr -> !"N".equalsIgnoreCase(terminalParametersAttr.getShowInPage()))
                        .collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(parametersAttrs)) {
                request.setAttribute("Parameters", parametersAttrs);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            RedisPoolManager.close(client);
        }
        return "terminal";
    }

    @SneakyThrows
    @RequestMapping("/uploadterminalcategory")
    @UserPermission(value = UserPermissionEnum.PARAMETERGROUPINSERTANDUPDATE)
    @UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "?????????????????????")
    @ResponseBody
    public ResponseMsg uploadterminalcategory(@RequestParam(value = "fileTerminalCategor", required = false) MultipartFile multipartFile, HttpServletRequest request) {
        ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
        RedisPoolManager.Init(jedisPool);
        Jedis client = RedisPoolManager.getJedis();
        if (Objects.isNull(multipartFile)) {
            return ResponseMsg.builder().statusCode(1).message("????????????????????????????????????").build();
        }
        String saveFilePathDir = request.getSession().getServletContext().getRealPath("/resources/terminalconfig");
        File saveTerminalCategoryFile = new File(saveFilePathDir);
        if (!saveTerminalCategoryFile.exists() && !saveTerminalCategoryFile.isDirectory()) {
            saveTerminalCategoryFile.mkdirs();
        }
        String suffix = multipartFile.getOriginalFilename().substring(multipartFile.getOriginalFilename().lastIndexOf("."));
        if (!suffix.toLowerCase().endsWith("xml")) {
            return ResponseMsg.builder().statusCode(1).message("?????????xml??????????????????????????????").build();
        }
        saveFilePathDir += File.separator;
        File checkFile = new File(saveFilePathDir, multipartFile.getOriginalFilename());
        if (checkFile.exists()) {
            saveTerminalCategoryFile = new File(saveFilePathDir + UUID.randomUUID().toString() + suffix);
        }
        multipartFile.transferTo(saveTerminalCategoryFile);
        try {
            Terminalparameters terminalparameters = ParseXML.parseXMLFile(saveTerminalCategoryFile);
            Terminalparameterscategory terminalparameterscategory = terminalParamCategoryService.getTerminalparamCategoryByName(terminalparameters.getName());
            if (Objects.isNull(terminalparameterscategory)) {
                FileUtils.deleteQuietly(saveTerminalCategoryFile);
                return ResponseMsg.builder().statusCode(1).message(String.format("?????????%s??????????????????????????????????????????????????????", terminalparameters.getName())).build();
            }
            terminalparameterscategory = new Terminalparameterscategory();
            terminalparameterscategory.setIsSystem(1);
            terminalparameterscategory.setTerminalParamCategoryName(terminalparameters.getName());
            terminalparameterscategory.setMappingName(saveTerminalCategoryFile.getName().replaceAll("\\s*", "").replaceAll("[.][^.]+$", ""));
            ResponseMsg responseMsg = terminalParamCategoryService.insertTerminalParamCategory(terminalparameterscategory);
            if (responseMsg.getStatusCode() != 0) {
                responseMsg.setMessage("???????????????????????????????????????");
                FileUtils.deleteQuietly(saveTerminalCategoryFile);
                return responseMsg;
            }
            int categoryId = (int) responseMsg.getMessageObject();
            responseMsg = terminalParamCategoryService.batchInsertTerminalParamSettings(categoryId, terminalparameters);
            if (responseMsg.getStatusCode() != 0) {
                responseMsg.setMessage("?????????????????????????????????????????????????????????");
                FileUtils.deleteQuietly(saveTerminalCategoryFile);
                return responseMsg;
            }
            client.set(String.valueOf(categoryId).getBytes(), objectSerializeTransfer.serialize(terminalparameters));
            Map<String, String> terminalCatetoryMap = new HashMap<>();
            terminalCatetoryMap.put(String.valueOf(categoryId), terminalparameterscategory.getMappingName());
            client.hmset(RedisKey.ALLCATETORY, terminalCatetoryMap);
            if (SearchIndexUtil.createIndex(MappingType.INDEX)) {
                SearchIndexUtil.createMapping(categoryId, terminalparameters, MappingType.INDEX);
            }
            return responseMsg;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            RedisPoolManager.close(client);
        }
        return ResponseMsg.builder().statusCode(1).message("???????????????????????????????????????").build();
    }

    @RequestMapping("/delete/{categoryUniqueId}")
    @UserPermission(value = UserPermissionEnum.PARAMETERGROUPDELETE)
    @UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "??????????????????")
    @ResponseBody
    public ResponseMsg delete(HttpServletRequest request, @PathVariable("categoryUniqueId") String categoryUniqueId) {
        int categoryId = HexUtil.HexToInt(categoryUniqueId);
        Terminalparameterscategory category = this.terminalParamCategoryService.getTerminalparamCategory(categoryId);
        this.terminalParamCategoryService.delTerminalparamCategory(categoryId);
        if (category != null) {
            String parameFilePath = request.getSession().getServletContext().getRealPath("/resources/terminalconfig/" + category.getMappingName() + ".xml");
            FileUtils.deleteQuietly(new File(parameFilePath));
        }
        return ResponseMsg.builder().statusCode(0).message("????????????").build();
    }

    @RequestMapping("/download/{categoryUniqueId}")
    @UserPermission(value = UserPermissionEnum.PARAMETERGROUPSELECT)
    @UserAction(Action = UserPermissionEnum.PARAMETERGROUP, Description = "??????????????????")
    public ResponseEntity<byte[]> download(HttpServletRequest request, @PathVariable("categoryUniqueId") String categoryUniqueId) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        int categoryId = HexUtil.HexToInt(categoryUniqueId);
        Terminalparameterscategory category = this.terminalParamCategoryService.getTerminalparamCategory(categoryId);
        byte[] fileBytes = null;
        if (category != null && !StringUtils.isBlank(category.getMappingName())) {
            String path = request.getSession().getServletContext().getRealPath("/resources/terminalconfig/" + category.getMappingName() + ".xml");
            File categoryFile = new File(path);
            if (categoryFile.exists()) {
                String fileName = new String((category.getMappingName() + ".xml").getBytes("UTF-8"), "iso-8859-1");
                headers.setContentDispositionFormData("attachment", fileName);
                fileBytes = FileUtils.readFileToByteArray(categoryFile);
            }
        }
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        fileBytes = fileBytes == null ? new byte[0] : fileBytes;
        return new ResponseEntity<byte[]>(fileBytes, headers, HttpStatus.CREATED);
    }
}
