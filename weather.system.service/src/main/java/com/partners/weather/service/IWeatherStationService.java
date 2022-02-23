package com.partners.weather.service;

import com.partners.entity.Heartbeat;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.entity.WeatherstationClient;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VWeatherStation;
import com.partners.view.entity.VWeatherstationcategory;

import java.util.ArrayList;
import java.util.List;

public interface IWeatherStationService {
    List<Integer> getAllPorts();

    List<WeatherStationTerminal> getWeatherStationTerminals();

    List<Weatherstationcategory> getWeatherStationCategorys(VWeatherstationcategory vWeatherstationcategory);

    List<Weatherstationcategory> getAllWeatherCatsExcludeCategory(int categoryId);

    Weatherstationcategory getWeatherStationCategory(int categoryId);

    Weatherstationcategory getWeatherStationCategoryByName(String weatherStationCategoryName);

    ResponseMsg delWeatherStationCategory(int categoryId);

    int checkCategoryExists(int categoryId, String weatherStationCategoryName);

    int checkWeatherStationExists(int categoryId);

    int getWeatherCategoryMaxId();

    ResponseMsg insertWeatherStationCategory(Weatherstationcategory weatherstationcategory);

    List<Weatherstation> getWeatherStations(VWeatherStation vWeatherStation);

    Weatherstation getWeatherStation(int weatherStationId);

    Weatherstation getWeatherStationByName(String weatherStationName);

    ResponseMsg delWeatherStation(int weatherStationId);

    ResponseMsg delWeatherStationTerminal(int weatherStationId);

    ResponseMsg insertWeatherStation(Weatherstation weatherstation);

    ResponseMsg batchInsertWeatherStationTerminal(int weatherStationId, List<WeatherStationTerminal> weatherStationTerminals);

    ResponseMsg batchInsertWeatherStationTerminal(int weatherStationId, List<WeatherStationTerminal> weatherStationTerminals, List<WeatherStationTerminal> orginalTerminals);

    int checkWeatherStationExists(int weatherStationId, String weatherStationName);

    List<Weatherstation> getAllWeatherStation();

    List<Weatherstationcategory> getAllWeatherStationCategory();

    List<Heartbeat> getHeartbeats();

    int getWeatherStationCount(VWeatherStation vWeatherStation);

    int getWeatherStationCategoryCount(VWeatherstationcategory vWeatherstationcategory);

    ResponseMsg updateWeatherStation(Weatherstation weatherstation);

    ArrayList<WeatherStationTerminal> getWeatherStationTerminalById(int weatherStationId);

    int getWeatherStationTerminalMaxId();

    List<Weatherstationcategory> filterWeatherStationCategorys(List<Weatherstationcategory> weatherstationcategories, String userCategory, boolean isHexId);

    List<Weatherstation> filterWeatherStation(List<Weatherstation> weatherstations, String userStation, boolean isHexId);

    List<Weatherstation> getWeatherStationsByCategory(List<Integer> categories);

    List<WeatherStationTerminal> getTerminalsByStationId(List<Integer> categories);

    int getWeatherStationTerminalCount();

    List<WeatherstationClient> getWeatherStationClients();

    List<WeatherstationClient> getWSClientsByStationId(List<Integer> weatherStations);

    int getWeatherStationCountByPort(int gRPSPort);
}
