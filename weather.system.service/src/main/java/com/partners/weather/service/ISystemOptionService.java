package com.partners.weather.service;

import java.util.List;

import com.partners.entity.SystemOption;
import com.partners.view.entity.ResponseMsg;

public interface ISystemOptionService {
    List<SystemOption> getSystemOptions();

    SystemOption getSystemOption(String optionId);

    ResponseMsg addSystemOption(SystemOption systemOption);

    SystemOption getSystemOption(List<SystemOption> systemOptions, String optionId);

    ResponseMsg delSystemOption(String optionId);
}
