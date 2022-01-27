package com.partners.weather.service;

import java.util.List;

import com.partners.entity.Invalid;
import com.partners.view.entity.VInvalid;

public interface IStationInvalidService {
	 List<Invalid> getInvalidRecords(VInvalid invalid);
}
