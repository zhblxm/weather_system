package com.partners.weather.Json;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.partners.entity.City;
import com.partners.entity.Country;
import com.partners.entity.Location;
import com.partners.entity.Piecearea;
import com.partners.entity.Province;
import com.partners.entity.Terminalparameters;
import com.partners.weather.common.CommonResources;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.serialize.ObjectSerializeTransfer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
public class JsonHelper {
    private static JsonHelper helper = new JsonHelper();

    @Autowired
    JedisPool jedisPool;

    private Country loadJsonFromFile() throws IOException, URISyntaxException {
        ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
        LineIterator it = null;
        Country country = null;
        StringBuilder countryBuilder = new StringBuilder();
        Jedis client = null;
        byte[] key = CommonResources.COUNTRY.getBytes();
        try {
            File countryFile = new File(JsonHelper.class.getClassLoader().getResource("").toURI().getPath() + "ChinaBoundryBinaryAll.json");
            RedisPoolManager.Init(jedisPool);
            client = RedisPoolManager.getJedis();
            if (client.exists(key)) {
                country = (Country) objectSerializeTransfer.deserialize(client.get(key));
            } else {
                it = FileUtils.lineIterator(countryFile, "UTF-8");
                while (it.hasNext()) {
                    countryBuilder.append(it.nextLine());
                }
                country = JSON.parseObject(countryBuilder.toString(), Country.class);
                client.set(key, objectSerializeTransfer.serialize(country));
                client.expire(key, 60 * 60 * 24);
            }
        } catch (IOException exception) {
            log.error("Exception in {}", exception);
        } finally {
            if (it != null) {
                LineIterator.closeQuietly(it);
            }
            RedisPoolManager.close(client);
        }
        return country;

    }

    private Location createLocation(String name, String loc) {
        Location location = new Location();
        location.setTitle(name);
        if (loc.split(" ").length != 2) {
            return null;
        }
        location.setLoc(new double[]{Double.parseDouble(loc.split(" ")[1]), Double.parseDouble(loc.split(" ")[0])});
        return location;
    }

    private List<Location> createLocation(String name, String[] locations) {
        List<Location> locList = null;
        if (locations == null || locations.length == 0) {
            return new ArrayList<>(0);
        }
        locList = new ArrayList<>(locations.length);
        Location location;
        for (String loc : locations) {
            if (loc.split(" ").length != 2) {
                continue;
            }
            location = new Location();
            location.setTitle(name);
            location.setLoc(new double[]{Double.parseDouble(loc.split(" ")[1]), Double.parseDouble(loc.split(" ")[0])});
            locList.add(location);
        }
        return locList;
    }

    private List<Location> searchKey(Object object, String key) {
        List<Location> locations = new ArrayList<>();
        String[] locArrays;
        Location location;
        List<City> cities = new ArrayList<>();
        City cityObj;
        if (object instanceof Province) {
            for (City city : ((Province) object).getCity()) {
                if (city.getName().contains(key)) {
                    cities.add(city);
                }
                locations.addAll(this.searchKey(city, key));
            }
        } else if (object instanceof City) {
            for (Piecearea piecearea : ((City) object).getPiecearea()) {
                if (piecearea.getName().contains(key)) {
                    cityObj = new City();
                    cityObj.setName(piecearea.getName());
                    cityObj.setRings(piecearea.getRings());
                    cityObj.setCode(piecearea.getCode());
                    cities.add(cityObj);
                }
            }
        }
        for (City city : cities) {
            locArrays = city.getRings().split(",");
            location = helper.createLocation(city.getName(), locArrays[0]);
            if (location != null) {
                locations.add(location);
            }
        }
        return locations;
    }

    public static List<Location> findCity(String key) {
        final List<Location> locations = Lists.newArrayList();
        try {
            helper.loadJsonFromFile().getProvince().stream().forEach(province -> {
                if (province.getName().contains(key)) {
                    String[] locArrays = province.getRings().split(",");
                    Location location = helper.createLocation(province.getName(), locArrays[locArrays.length / 2]);
                    if (Objects.nonNull(location)) {
                        locations.add(location);
                    }
                }
                locations.addAll(helper.searchKey(province, key));
            });
        } catch (IOException | URISyntaxException exception) {
            log.error("Exception in {}", exception);
        }
        final List<Location> filterList = new ArrayList<>(locations.size());
        locations.stream().forEach(
                l->{
                    if(!filterList.contains(l)) {
                        filterList.add(l);
                    }
                }
        );
        return filterList;
    }

    public static String find(String key) {
        List<Location> locations = new ArrayList<>();
        try {
            helper.loadJsonFromFile().getProvince().stream().forEach(province -> {
                if (province.getName().contains(key)) {
                    String[] locArrays = province.getRings().split(",");
                    Location location = helper.createLocation(province.getName(), locArrays[locArrays.length / 2]);
                    if (location != null) {
                        locations.add(location);
                    }
                }
                locations.addAll(helper.searchKey(province, key));
            });
        } catch (IOException | URISyntaxException exception) {
            log.error("Exception in {}", exception);
        }
        return JSON.toJSONString(locations);
    }
}
