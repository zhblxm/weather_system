package com.partners.weather.dao;

import java.util.ArrayList;
import java.util.List;

import com.partners.entity.AutoFrequencyTerminal;
import com.partners.entity.Autofrequencyhistory;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.entity.WeatherstationClient;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.VStatistics;
import com.partners.view.entity.VWeatherStation;
import com.partners.view.entity.VWeatherstationcategory;

interface IWeatherStationDAO {
	List<Integer> getAllPorts();

	int getWeatherStationTerminalCount();
	
	List<WeatherStationTerminal> getWeatherStationTerminals();

	WeatherStationTerminal getWeatherStationTerminalByModel(String terminalModel);

	List<Weatherstationcategory> getWeatherStationCategorys(VWeatherstationcategory vWeatherstationcategory);

	List<Weatherstationcategory> getAllWeatherCatsExcludeCategory(int categoryId);

	Weatherstationcategory getWeatherStationCategory(int categoryId);

	Weatherstationcategory getWeatherStationCategoryByName(String weatherStationCategoryName);

	void delWeatherStationCategory(int categoryId);

	int checkCategoryExists(int categoryId, String weatherStationCategoryName);

	int checkWeatherStationExists(int categoryId);

	int getWeatherCategoryMaxId();

	void insertWeatherStationCategory(Weatherstationcategory weatherstationcategory);
	
	List<Weatherstationcategory> getAllWeatherStationCategory();		
	
	int getWeatherStationCategoryCount(VWeatherstationcategory vWeatherstationcategory);
	
	void updateStationCategoryName(Weatherstationcategory weatherstationcategory);	
	
	List<WeatherStationTerminal> getTerminalsByStationId(List<Integer> categories);

	/******************************** * Weather Station ***********************************************/
	
	List<Weatherstation> getAllWeatherStation();
	
	List<Weatherstation> getWeatherStations(VWeatherStation vWeatherStation);

	Weatherstation getWeatherStation(int weatherStationId);

	Weatherstation getWeatherStationByName(String weatherStationName);

	void delWeatherStation(int weatherStationId);

	void delWeatherStationTerminal(int weatherStationId);

	int insertWeatherStation(Weatherstation weatherstation);

	int batchInsertWeatherStationTerminal(List<WeatherStationTerminal> weatherStationTerminals);

	ArrayList<WeatherStationTerminal> getWeatherStationTerminalById(int weatherStationId);
	
	int checkWeatherStationByNameExists(int weatherStationId, String weatherStationName);
	
	int getWeatherStationCount(VWeatherStation vWeatherStation);
	
	void updateWeatherStation(Weatherstation weatherstation);
	
	int getWeatherStationTerminalMaxId();	
	
	List<Weatherstation> getMutliWeatherStation(List<Integer> weatherstations);
	
	List<Weatherstation> getWeatherStationsByCategory(List<Integer> categories);
	
	int getWeatherStationCountByPort(int gRPSPort);
	/******************************** Auto Frequency History***********************************************/
	
	int insertAutoFrequencyHistory(Autofrequencyhistory autofrequencyhistory);

	List<Autofrequencyhistory> getAutoFrequencyHistories();
	
	List<Autofrequencyhistory> getAutoFrequencyHistory(VStatistics statistics);
	
	int batchInsertAutoFrequencyHistory(List<Autofrequencyhistory> autofrequencyhistories);
	
	void batchUpdateAutoFrequencyHistory(List<AutoFrequencyTerminal> autoFrequencyTerminals);	
	
	List<WeatherstationClient> getWeatherStationClients();
	
	List<WeatherstationClient> getWSClientsByStationId(List<Integer> weatherStations);
}
