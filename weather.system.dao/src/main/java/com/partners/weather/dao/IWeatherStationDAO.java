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

public interface IWeatherStationDAO {
	public List<Integer> getAllPorts();

	public int getWeatherStationTerminalCount();
	
	public List<WeatherStationTerminal> getWeatherStationTerminals();

	public WeatherStationTerminal getWeatherStationTerminalByModel(String terminalModel);

	public List<Weatherstationcategory> getWeatherStationCategorys(VWeatherstationcategory vWeatherstationcategory);

	public List<Weatherstationcategory> getAllWeatherCatsExcludeCategory(int categoryId);

	public Weatherstationcategory getWeatherStationCategory(int categoryId);

	public Weatherstationcategory getWeatherStationCategoryByName(String weatherStationCategoryName);

	public void delWeatherStationCategory(int categoryId);

	public int checkCategoryExists(int categoryId, String weatherStationCategoryName);

	public int checkWeatherStationExists(int categoryId);

	public int getWeatherCategoryMaxId();

	public void insertWeatherStationCategory(Weatherstationcategory weatherstationcategory);
	
	public List<Weatherstationcategory> getAllWeatherStationCategory();		
	
	public int getWeatherStationCategoryCount(VWeatherstationcategory vWeatherstationcategory);
	
	public void updateStationCategoryName(Weatherstationcategory weatherstationcategory);	
	
	public List<WeatherStationTerminal> getTerminalsByStationId(List<Integer> categories);

	/******************************** * Weather Station ***********************************************/
	
	public List<Weatherstation> getAllWeatherStation();
	
	public List<Weatherstation> getWeatherStations(VWeatherStation vWeatherStation);

	public Weatherstation getWeatherStation(int weatherStationId);

	public Weatherstation getWeatherStationByName(String weatherStationName);

	public void delWeatherStation(int weatherStationId);

	public void delWeatherStationTerminal(int weatherStationId);

	public int insertWeatherStation(Weatherstation weatherstation);

	public int batchInsertWeatherStationTerminal(List<WeatherStationTerminal> weatherStationTerminals);

	public ArrayList<WeatherStationTerminal> getWeatherStationTerminalById(int weatherStationId);
	
	public int checkWeatherStationByNameExists(int weatherStationId, String weatherStationName);
	
	public int getWeatherStationCount(VWeatherStation vWeatherStation);
	
	public void updateWeatherStation(Weatherstation weatherstation);
	
	public int getWeatherStationTerminalMaxId();	
	
	public List<Weatherstation> getMutliWeatherStation(List<Integer> weatherstations);
	
	public List<Weatherstation> getWeatherStationsByCategory(List<Integer> categories);
	
	int getWeatherStationCountByPort(int gRPSPort);
	/******************************** Auto Frequency History***********************************************/
	
	public int insertAutoFrequencyHistory(Autofrequencyhistory autofrequencyhistory);

	public List<Autofrequencyhistory> getAutoFrequencyHistories();
	
	public List<Autofrequencyhistory> getAutoFrequencyHistory(VStatistics statistics);
	
	public int batchInsertAutoFrequencyHistory(List<Autofrequencyhistory> autofrequencyhistories);
	
	public void batchUpdateAutoFrequencyHistory(List<AutoFrequencyTerminal> autoFrequencyTerminals);	
	
	public List<WeatherstationClient> getWeatherStationClients();
	
	public List<WeatherstationClient> getWSClientsByStationId(List<Integer> weatherStations);
}
