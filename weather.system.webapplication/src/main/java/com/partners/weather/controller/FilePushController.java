package com.partners.weather.controller;

import com.jcraft.jsch.JSchException;
import com.partners.annotation.UserAction;
import com.partners.annotation.UserPermission;
import com.partners.annotation.UserPermissionEnum;
import com.partners.entity.SystemOption;
import com.partners.view.entity.ResponseMsg;
import com.partners.weather.common.CommonResources;
import com.partners.weather.ftp.FtpUtil;
import com.partners.weather.ftp.SftpUtil;
import com.partners.weather.service.ISystemOptionService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/filepush")
@UserPermission(value = UserPermissionEnum.FILEPUSH)
@Slf4j
public class FilePushController {
    private static final Map<String, String> KEYMAP = new HashMap<String, String>() {
        {
            put(CommonResources.FILEPUSHFTPURL, "ftpurl");
            put(CommonResources.FILEPUSHFTPPORT, "ftpport");
            put(CommonResources.FILEPUSHFTPUSER, "ftpuser");
            put(CommonResources.FILEPUSHFTPTYPE, "ftptype");
            put(CommonResources.FILEPUSHONOFF, "ftponoff");
            put(CommonResources.FILEPUSHFTPPWD, "ftppwd");
        }
    };
    @Resource
    ISystemOptionService systemOptionService;

    @RequestMapping
    @UserPermission(value = UserPermissionEnum.FILEPUSH)
    @UserAction(Action = UserPermissionEnum.FILEPUSH, Description = "系统设置")
    public String Manage(HttpServletRequest request) {
        List<SystemOption> systemOptions = systemOptionService.getSystemOptions();
        KEYMAP.forEach((key, value) -> {
            processSystemOption(request, systemOptions, Tuple.of(key, value));
        });
        return "filepush";
    }

    private void processSystemOption(HttpServletRequest request, List<SystemOption> systemOptions, Tuple2<String, String> keyTuple) {
        SystemOption systemOption = systemOptionService.getSystemOption(systemOptions, keyTuple._1);
        request.setAttribute(keyTuple._2, Objects.isNull(systemOption) ? StringUtils.EMPTY : systemOption.getOptionValue());
    }

    @RequestMapping("/save")
    @UserPermission(value = UserPermissionEnum.FILEPUSH)
    @UserAction(Action = UserPermissionEnum.FILEPUSH, Description = "更新文件设置")
    @ResponseBody
    public ResponseMsg saveFilePush(HttpServletRequest request) {
        ResponseMsg responseMsg = new ResponseMsg();
        checkFtpOrSFTPConnect(responseMsg, request);
        if (responseMsg.getStatusCode() == 1) {
            return responseMsg;
        }
        try {
            KEYMAP.forEach((key, value) -> {
                systemOptionService.addSystemOption(SystemOption.builder()
                        .optionId(key)
                        .optionValue(request.getParameter(value))
                        .build()
                );
            });
        } catch (Exception e) {
            responseMsg.setStatusCode(1);
            responseMsg.setMessage("文件推送设置保存失败，请联系管理员！");
            log.error(e.getMessage(), e);
        }
        return responseMsg;
    }

    private void checkFtpOrSFTPConnect(ResponseMsg responseMsg, HttpServletRequest request) {
        String ftpurl = request.getParameter("ftpurl");
        String ftpport = request.getParameter("ftpport");
        String ftpuser = request.getParameter("ftpuser");
        String ftppwd = request.getParameter("ftppwd");
        String ftptype = request.getParameter("ftptype");
        responseMsg.setStatusCode(0);
        try {
            if (StringUtils.equalsAnyIgnoreCase(ftptype, "ftp")) {
                FtpUtil ftpUtil = new FtpUtil(2, 2, 2);
                ftpUtil.connect(ftpurl, Integer.parseInt(ftpport), ftpuser, ftppwd, true);
                if (!ftpUtil.isConnected()) {
                    throw new JSchException("Can't find FTP server '" + ftpurl + "'");
                } else {
                    ftpUtil.disconnect();
                }
            } else {
                SftpUtil sftpUtil = new SftpUtil(ftpurl, ftpuser, Integer.parseInt(ftpport), 2);
                sftpUtil.connect(ftppwd);
                sftpUtil.disconnect();
            }
        } catch (Exception ex) {
            responseMsg.setStatusCode(1);
            responseMsg.setMessage("ftp连接失败！");
        }
    }

    @RequestMapping("/upload")
    @UserPermission(value = UserPermissionEnum.FILEPUSH)
    @UserAction(Action = UserPermissionEnum.FILEPUSH, Description = "更新文件模板")
    @ResponseBody
    public ResponseMsg uploadFile(HttpServletRequest request) {
        ResponseMsg responseMsg = ResponseMsg.builder().statusCode(0).build();
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
        String basePath = request.getSession().getServletContext().getRealPath("/resources/download"),
                filePath = basePath + "/filetempletup.txt";
        MultipartFile file = multiRequest.getFile("fileTemplet");
        try {
            if (Objects.isNull(file) || StringUtils.isBlank(filePath)) {
                return ResponseMsg.builder().statusCode(0).message("文件模板保存失败，请联系管理员！").build();
            }
            File saveUploadFile = new File(filePath);
            FileUtils.deleteQuietly(saveUploadFile);
            file.transferTo(saveUploadFile);
        } catch (Exception e) {
            responseMsg.setStatusCode(1);
            responseMsg.setMessage("文件模板保存失败，请联系管理员！");
            log.error(e.getMessage(), e);
        }
        return responseMsg;
    }
}
