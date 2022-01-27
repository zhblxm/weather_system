package com.partners.weather.service;

import java.util.ArrayList;
import java.util.List;

import com.partners.entity.Heartbeat;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.entity.WeatherstationClient;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VWeatherStation;
import com.partners.view.entity.VWeatherstationcategory;

public interface IWeatherStationService {
	public List<Integer> getAllPorts();

	public List<WeatherStationTerminal> getWeatherStationTerminals();

	public List<Weatherstationcategory> getWeatherStationCategorys(VWeatherstationcategory vWeatherstationcategory);

	public List<Weatherstationcategory> getAllWeatherCatsExcludeCategory(int categoryId);

	public Weatherstationcategory getWeatherStationCategory(int categoryId);

	public Weatherstationcategory getWeatherStationCategoryByName(String weatherStationCategoryName);

	public ResponseMsg delWeatherStationCategory(int categoryId);

	public int checkCategoryExists(int categoryId, String weatherStationCategoryName);

	public int checkWeatherStationExists(int categoryId);

	public int getWeatherCategoryMaxId();

	public ResponseMsg insertWeatherStationCategory(Weatherstationcategory weatherstationcategory);

	public List<Weatherstation> getWeatherStations(VWeatherStation vWeatherStation);

	public Weatherstation getWeatherStation(int weatherStationId);

	public Weatherstation getWeatherStationByName(String weatherStationName);

	public ResponseMsg delWeatherStation(int weatherStationId);

	public ResponseMsg delWeatherStationTerminal(int weatherStationId);

	public ResponseMsg insertWeatherStation(Weatherstation weatherstation);

	public ResponseMsg batchInsertWeatherStationTerminal(int weatherStationId, List<WeatherStationTerminal> weatherStationTerminals);
	
	public ResponseMsg batchInsertWeatherStationTerminal(int weatherStationId, List<WeatherStationTerminal> weatherStationTerminals,List<WeatherStationTerminal> orginalTerminals);

	public int checkWeatherStationExists(int weatherStationId, String weatherStationName);
	
	public List<Weatherstation> getAllWeatherStation();
	
	public List<Weatherstationcategory> getAllWeatherStationCategory();	
	
	public List<Heartbeat> getHeartbeats();	
	
	public int getWeatherStationCount(VWeatherStation vWeatherStation);
	
	public int getWeatherStationCategoryCount(VWeatherstationcategory vWeatherstationcategory);
	
	public ResponseMsg updateWeatherStation(Weatherstation weatherstation);
	
	public ArrayList<WeatherStationTerminal> getWeatherStationTerminalById(int weatherStationId);
	
	public int getWeatherStationTerminalMaxId();
	
	public List<Weatherstationcategory> filterWeatherStationCategorys(List<Weatherstationcategory> weatherstationcategories,String userCategory,boolean isHexId);
	
	public List<Weatherstation> filterWeatherStation(List<Weatherstation> weatherstations,String userStation,boolean isHexId);
	
	public List<Weatherstation> getWeatherStationsByCategory(List<Integer> categories);
	
	public List<WeatherStationTerminal> getTerminalsByStationId(List<Integer> categories);
	
	public int getWeatherStationTerminalCount();
	
	public List<WeatherstationClient> getWeatherStationClients();
	
	public List<WeatherstationClient> getWSClientsByStationId(List<Integer> weatherStations);
	
	int getWeatherStationCountByPort(int gRPSPort);
}
