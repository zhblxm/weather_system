package com.partners.weather.service.impl;

import com.google.common.collect.Lists;

import com.partners.entity.FilePushFlag;
import com.partners.weather.dao.IFilePushFlagDAO;
import com.partners.weather.service.IFilePushFlagService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FilePushFlagServiceImp implements IFilePushFlagService {
    private IFilePushFlagDAO filePushFlagDAO;

    @Autowired
    public FilePushFlagServiceImp(IFilePushFlagDAO filePushFlagDAO) {
        this.filePushFlagDAO = filePushFlagDAO;
    }

    @Override
    public List<FilePushFlag> getAllFilePushFlags() {
        try {
            return filePushFlagDAO.getAllFilePushFlags();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Lists.newArrayListWithCapacity(0);
    }

    @Override
    public FilePushFlag getFilePushFlag(List<FilePushFlag> filePushFlags, String terminalParam) {
        FilePushFlag filePushFlag = null;
        if (filePushFlags == null) {
            return filePushFlag;
        }
        try {
            filePushFlag = filePushFlags.stream().filter(e -> e.getTerminalparam().equalsIgnoreCase(terminalParam)).findFirst().orElseGet(null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return filePushFlag;
    }
}
