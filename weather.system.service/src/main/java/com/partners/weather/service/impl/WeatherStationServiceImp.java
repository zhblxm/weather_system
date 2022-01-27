package com.partners.weather.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.partners.entity.AutoFrequencyTerminal;
import com.partners.entity.Autofrequencyhistory;
import com.partners.entity.Heartbeat;
import com.partners.entity.TerminalParametersAttrs;
import com.partners.entity.Terminalparameters;
import com.partners.entity.WeatherStationTerminal;
import com.partners.entity.Weatherstation;
import com.partners.entity.WeatherstationClient;
import com.partners.entity.Weatherstationcategory;
import com.partners.view.entity.ResponseMsg;
import com.partners.view.entity.VWeatherStation;
import com.partners.view.entity.VWeatherstationcategory;
import com.partners.weather.common.CommonResources;
import com.partners.weather.common.RedisKey;
import com.partners.weather.dao.IWeatherStationDAO;
import com.partners.weather.encrypt.HexUtil;
import com.partners.weather.redis.RedisPoolManager;
import com.partners.weather.serialize.ListSerializeTransfer;
import com.partners.weather.serialize.ObjectSerializeTransfer;
import com.partners.weather.service.IAutoFrequencyHistoryService;
import com.partners.weather.service.IWeatherStationService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
@Transactional
public class WeatherStationServiceImp implements IWeatherStationService {
	private static final Logger logger = LoggerFactory.getLogger(WeatherStationServiceImp.class);
	private static Lock lock = new ReentrantLock();
	@Autowired
	private IWeatherStationDAO weatherStationDAO;
	@Autowired
	private IAutoFrequencyHistoryService autoFrequencyHistoryService;
	@Autowired
	JedisPool jedisPool;
	@Autowired
	RedisAtomicInteger redisAtomicInteger;

	@Override
	public List<Integer> getAllPorts() {
		List<Integer> ports = null;
		try {
			ports = weatherStationDAO.getAllPorts();

		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return ports == null ? new ArrayList<Integer>(0) : ports;
	}

	@Override
	public List<WeatherStationTerminal> getWeatherStationTerminals() {
		List<WeatherStationTerminal> weatherStationTerminals = null;
		try {
			weatherStationTerminals = weatherStationDAO.getWeatherStationTerminals();

		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherStationTerminals == null ? new ArrayList<WeatherStationTerminal>(0) : weatherStationTerminals;
	}

	@Override
	public List<Weatherstationcategory> getWeatherStationCategorys(VWeatherstationcategory vWeatherstationcategory) {
		List<Weatherstationcategory> weatherstationcategories = null;
		try {
			weatherstationcategories = weatherStationDAO.getWeatherStationCategorys(vWeatherstationcategory);
			if (weatherstationcategories != null && weatherstationcategories.size() > 0) {
				for (Weatherstationcategory weatherstationcategory : weatherstationcategories) {
					weatherstationcategory.setUniqueCategoryId(HexUtil.IntToHex(weatherstationcategory.getWeatherStationCategoryId()));
					weatherstationcategory.setWeatherStationCategoryId(0);
					if (weatherstationcategory.getParentCategoryId() > 0) {
						weatherstationcategory.setUniqueParentId(HexUtil.IntToHex(weatherstationcategory.getParentCategoryId()));
						weatherstationcategory.setParentCategoryId(0);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherstationcategories == null ? new ArrayList<Weatherstationcategory>(0) : weatherstationcategories;
	}

	@Override
	public Weatherstationcategory getWeatherStationCategory(int categoryId) {
		Weatherstationcategory weatherstationcategory = null;
		try {
			weatherstationcategory = weatherStationDAO.getWeatherStationCategory(categoryId);
			if (weatherstationcategory != null) {
				weatherstationcategory.setUniqueCategoryId(HexUtil.IntToHex(weatherstationcategory.getWeatherStationCategoryId()));
				weatherstationcategory.setWeatherStationCategoryId(0);
				if (weatherstationcategory.getParentCategoryId() > 0) {
					weatherstationcategory.setUniqueParentId(HexUtil.IntToHex(weatherstationcategory.getParentCategoryId()));
					weatherstationcategory.setParentCategoryId(0);
				}
			}

		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherstationcategory;
	}

	@Override
	public Weatherstationcategory getWeatherStationCategoryByName(String weatherStationCategoryName) {
		Weatherstationcategory weatherstationcategory = null;
		try {
			weatherstationcategory = weatherStationDAO.getWeatherStationCategoryByName(weatherStationCategoryName);
			if (weatherstationcategory != null) {
				weatherstationcategory.setUniqueCategoryId(HexUtil.IntToHex(weatherstationcategory.getWeatherStationCategoryId()));
				weatherstationcategory.setWeatherStationCategoryId(0);
				if (weatherstationcategory.getParentCategoryId() > 0) {
					weatherstationcategory.setUniqueParentId(HexUtil.IntToHex(weatherstationcategory.getParentCategoryId()));
					weatherstationcategory.setParentCategoryId(0);
				}
			}
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherstationcategory;
	}

	@Override
	public ResponseMsg delWeatherStationCategory(int categoryId) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		Jedis client = null;
		try {
			int useCount = this.checkWeatherStationExists(categoryId);
			if (useCount > 0) {
				responseMsg.setMessage("该分类已被使用，不能删除");
				responseMsg.setStatusCode(1);
				return responseMsg;
			}
			weatherStationDAO.delWeatherStationCategory(categoryId);
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			client.hdel(RedisKey.ALLCATETORY, String.valueOf(categoryId));
		} catch (Exception ex) {
			responseMsg.setMessage("删除站点分类失败！");
			responseMsg.setStatusCode(1);
			logger.error("Erron in {}", ex);
		} finally {
			RedisPoolManager.close(client);
		}
		return responseMsg;
	}

	@Override
	public int checkCategoryExists(int categoryId, String weatherStationCategoryName) {
		int categoryCount = 0;
		try {
			categoryCount = weatherStationDAO.checkCategoryExists(categoryId, weatherStationCategoryName);
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return categoryCount;
	}

	@Override
	public int checkWeatherStationExists(int categoryId) {
		int weatherStationrCategoryCount = 0;
		try {
			weatherStationrCategoryCount = weatherStationDAO.checkWeatherStationExists(categoryId);
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherStationrCategoryCount;
	}

	@Override
	public int getWeatherCategoryMaxId() {
		int weatherStationrCategoryMaxId = 0;
		try {
			weatherStationrCategoryMaxId = weatherStationDAO.getWeatherCategoryMaxId();
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherStationrCategoryMaxId;
	}

	@Override
	public ResponseMsg insertWeatherStationCategory(Weatherstationcategory weatherstationcategory) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			lock.lock();
			int weatherStationCategoryCount = this.checkCategoryExists(weatherstationcategory.getWeatherStationCategoryId(), weatherstationcategory.getWeatherStationCategoryName());
			if (weatherStationCategoryCount > 0) {
				responseMsg.setStatusCode(1);
				responseMsg.setMessage("站点分类名字已经存在！");
				return responseMsg;
			}
			if (weatherstationcategory.getWeatherStationCategoryId() == 0) {
				weatherstationcategory.setWeatherStationCategoryId(this.getWeatherCategoryMaxId());
			}
			weatherStationDAO.insertWeatherStationCategory(weatherstationcategory);
			weatherStationDAO.updateStationCategoryName(weatherstationcategory);
			responseMsg.setMessageObject(HexUtil.IntToHex(weatherstationcategory.getWeatherStationCategoryId()));
		} catch (Exception ex) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("新增站点分类失败！");
			logger.error("Erron in {}", ex);
		} finally {
			lock.unlock();
		}
		return responseMsg;
	}

	@Override
	public List<Weatherstationcategory> getAllWeatherCatsExcludeCategory(int categoryId) {
		List<Weatherstationcategory> weatherstationcategories = null;
		try {
			weatherstationcategories = weatherStationDAO.getAllWeatherCatsExcludeCategory(categoryId);
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherstationcategories == null ? new ArrayList<Weatherstationcategory>(0) : weatherstationcategories;
	}

	@Override
	public List<Weatherstation> getWeatherStations(VWeatherStation vWeatherStation) {
		List<Weatherstation> weatherstations = null;
		try {
			weatherstations = weatherStationDAO.getWeatherStations(vWeatherStation);
			if (weatherstations != null && weatherstations.size() > 0) {
				for (Weatherstation weatherstation : weatherstations) {
					weatherstation.setUniqueWeatherStationId(HexUtil.IntToHex(weatherstation.getWeatherStationId()));
					weatherstation.setWeatherStationId(0);
					weatherstation.setUniqueCategoryId(HexUtil.IntToHex(weatherstation.getWeatherStationCategoryId()));
					weatherstation.setWeatherStationCategoryId(0);
				}
			}
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherstations == null ? new ArrayList<Weatherstation>(0) : weatherstations;
	}

	@Override
	public Weatherstation getWeatherStation(int weatherStationId) {
		Weatherstation weatherstation = null;
		try {
			weatherstation = weatherStationDAO.getWeatherStation(weatherStationId);
			if (weatherstation != null) {
				weatherstation.setUniqueWeatherStationId(HexUtil.IntToHex(weatherstation.getWeatherStationId()));
				weatherstation.setWeatherStationId(0);
				weatherstation.setUniqueCategoryId(HexUtil.IntToHex(weatherstation.getWeatherStationCategoryId()));
				weatherstation.setWeatherStationCategoryId(0);
				if (weatherstation.getWeatherStationTerminals() == null) {
					weatherstation.setWeatherStationTerminals(this.getWeatherStationTerminalById(weatherStationId));
				}
				for (WeatherStationTerminal weatherStationTerminal : weatherstation.getWeatherStationTerminals()) {
					weatherStationTerminal.setUniqueWSTId(HexUtil.IntToHex(weatherStationTerminal.getWeatherStationTerminalId()));
					weatherStationTerminal.setUniqueStationId(HexUtil.IntToHex(weatherStationTerminal.getWeatherStationId()));
					weatherStationTerminal.setUniqueTPCId(HexUtil.IntToHex(weatherStationTerminal.getTeminalParameterCategoryId()));
					weatherStationTerminal.setWeatherStationTerminalId(0);
					weatherStationTerminal.setWeatherStationId(0);
					weatherStationTerminal.setTeminalParameterCategoryId(0);
				}
			}
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherstation;
	}

	@Override
	public Weatherstation getWeatherStationByName(String weatherStationName) {
		Weatherstation weatherstation = null;
		try {
			weatherstation = weatherStationDAO.getWeatherStationByName(weatherStationName);
			if (weatherstation != null) {
				weatherstation.setUniqueWeatherStationId(HexUtil.IntToHex(weatherstation.getWeatherStationId()));
				weatherstation.setWeatherStationId(0);
				weatherstation.setUniqueCategoryId(HexUtil.IntToHex(weatherstation.getWeatherStationCategoryId()));
				weatherstation.setWeatherStationCategoryId(0);
			}
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherstation;
	}

	@Override
	public ResponseMsg delWeatherStation(int weatherStationId) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			weatherStationDAO.delWeatherStation(weatherStationId);
		} catch (Exception ex) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("删除站点失败！");
			logger.error("Erron in {}", ex);
		}
		return responseMsg;
	}

	@Override
	public ResponseMsg delWeatherStationTerminal(int weatherStationId) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			weatherStationDAO.delWeatherStationTerminal(weatherStationId);
		} catch (Exception ex) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("删除站点设备失败！");
			logger.error("Erron in {}", ex);
		}
		return responseMsg;
	}

	@Override
	public ResponseMsg insertWeatherStation(Weatherstation weatherstation) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		Jedis client = null;
		try {
			int weatherStationCount = weatherStationDAO.checkWeatherStationByNameExists(weatherstation.getWeatherStationId(), weatherstation.getWeatherStationName());
			if (weatherStationCount > 0) {
				responseMsg.setStatusCode(1);
				responseMsg.setMessage("站点名字已经存在！");
				return responseMsg;
			}
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
			Map<byte[], byte[]> weatherStationTerminalMap = client.hgetAll(RedisKey.ALLWEATHERSYSTEMKEY.getBytes());
			final Map<String, Integer> terminalModelMap = new HashMap<>();
			Iterator<byte[]> terminalModelIterator = weatherStationTerminalMap.keySet().iterator();
			byte[] values;
			WeatherStationTerminal terminalTemp;
			while (terminalModelIterator.hasNext()) {
				values = weatherStationTerminalMap.get(terminalModelIterator.next());
				if (values != null && values.length > 0) {
					terminalTemp = (WeatherStationTerminal) objectSerializeTransfer.deserialize(values);
					terminalModelMap.put(terminalTemp.getTerminalModel().trim(), terminalTemp.getWeatherStationTerminalId());
				}
			}
			Collection<WeatherStationTerminal> validStationTerminals = Collections2.filter(weatherstation.getWeatherStationTerminals(), new Predicate<WeatherStationTerminal>() {
				@Override
				public boolean apply(WeatherStationTerminal terminal) {
					if (terminalModelMap.containsKey(terminal.getTerminalModel().trim())) {
						if (!StringUtils.isBlank(terminal.getUniqueWSTId()) && HexUtil.HexToInt(terminal.getUniqueWSTId()) == terminalModelMap.get(terminal.getTerminalModel().trim())) {
							return false;
						}
						return true;
					} else {
						return false;
					}
				}
			});
			if (validStationTerminals.size() > 0) {
				responseMsg.setStatusCode(1);
				responseMsg.setMessage("设备编号(" + ((WeatherStationTerminal) validStationTerminals.toArray()[0]).getTerminalModel() + ")必须填写并且全系统唯一！");
			}
			if (responseMsg.getStatusCode() != 0) {
				return responseMsg;
			}
			if (weatherstation.getWeatherStationId() > 0) {
				responseMsg = this.updateWeatherStation(weatherstation);
			} else {
				weatherStationDAO.insertWeatherStation(weatherstation);
			}
			if (responseMsg.getStatusCode() == 0) {
				List<WeatherStationTerminal> originalTerminals = this.getWeatherStationTerminalById(weatherstation.getWeatherStationId());
				this.delWeatherStationTerminal(weatherstation.getWeatherStationId());
				responseMsg = this.batchInsertWeatherStationTerminal(weatherstation.getWeatherStationId(), weatherstation.getWeatherStationTerminals(), originalTerminals);
			}
			weatherstation.setUniqueWeatherStationId(HexUtil.IntToHex(weatherstation.getWeatherStationId()));
			weatherstation.setUniqueCategoryId(HexUtil.IntToHex(weatherstation.getWeatherStationCategoryId()));
			weatherstation.setWeatherStationId(0);
			weatherstation.setWeatherStationCategoryId(0);
			Map<byte[], byte[]> weatherStationMap = new HashMap<>();
			if (client.exists(RedisKey.ALLSTATION.getBytes())) {
				weatherStationMap = client.hgetAll(RedisKey.ALLSTATION.getBytes());
			}
			for (WeatherStationTerminal terminal : weatherstation.getWeatherStationTerminals()) {
				weatherStationMap.put(terminal.getTerminalModel().getBytes(), weatherstation.getWeatherStationNumber().getBytes());
				terminal.setUniqueWSTId(HexUtil.IntToHex(terminal.getWeatherStationTerminalId()));
				terminal.setUniqueStationId(HexUtil.IntToHex(terminal.getWeatherStationId()));
				terminal.setUniqueTPCId(HexUtil.IntToHex(terminal.getTeminalParameterCategoryId()));
				terminal.setWeatherStationTerminalId(0);
				terminal.setWeatherStationId(0);
				terminal.setTeminalParameterCategoryId(0);
			}
			client.hmset(RedisKey.ALLSTATION.getBytes(), weatherStationMap);
			responseMsg.setMessageObject(weatherstation);
		} catch (Exception ex) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("添加站点失败！");
			logger.error("Erron in {}", ex);
		} finally {
			RedisPoolManager.close(client);
		}
		return responseMsg;
	}

	@Override
	public ResponseMsg batchInsertWeatherStationTerminal(int weatherStationId, List<WeatherStationTerminal> weatherStationTerminals) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		if (weatherStationTerminals == null) {
			throw new IllegalArgumentException("添加的设备不能为空！");
		}
		List<WeatherStationTerminal> originalTerminals = this.getWeatherStationTerminalById(weatherStationId);
		responseMsg = this.batchInsertWeatherStationTerminal(weatherStationId, weatherStationTerminals, originalTerminals);
		return responseMsg;
	}

	@Override
	public int checkWeatherStationExists(int weatherStationId, String weatherStationName) {
		int stationCount = 0;
		try {
			stationCount = weatherStationDAO.checkWeatherStationByNameExists(weatherStationId, weatherStationName);
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return stationCount;
	}

	@Override
	public List<Weatherstation> getAllWeatherStation() {
		List<Weatherstation> weatherstations = null;
		try {
			weatherstations = weatherStationDAO.getAllWeatherStation();

		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherstations == null ? new ArrayList<Weatherstation>(0) : weatherstations;
	}

	@Override
	public List<Weatherstationcategory> getAllWeatherStationCategory() {
		List<Weatherstationcategory> weatherstationcategories = null;
		try {
			weatherstationcategories = weatherStationDAO.getAllWeatherStationCategory();

		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherstationcategories == null ? new ArrayList<Weatherstationcategory>(0) : weatherstationcategories;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Heartbeat> getHeartbeats() {
		Jedis client = null;
		List<Heartbeat> heartbeats = null;
		try {
			RedisPoolManager.Init(jedisPool);
			client = RedisPoolManager.getJedis();
			byte[] key = CommonResources.HEARTBEAT.getBytes();
			ListSerializeTransfer<Heartbeat> lsSerializeTransfer = new ListSerializeTransfer<>();
			if (client.exists(key)) {
				heartbeats = (List<Heartbeat>) lsSerializeTransfer.deserialize(client.get(key));
			}
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		} finally {
			RedisPoolManager.close(client);
		}
		return heartbeats == null ? new ArrayList<Heartbeat>(0) : heartbeats;
	}

	@Override
	public int getWeatherStationCount(VWeatherStation vWeatherStation) {
		int count = 0;
		try {
			count = weatherStationDAO.getWeatherStationCount(vWeatherStation);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return count;
	}

	@Override
	public int getWeatherStationCategoryCount(VWeatherstationcategory vWeatherstationcategory) {
		int count = 0;
		try {
			count = weatherStationDAO.getWeatherStationCategoryCount(vWeatherstationcategory);
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return count;
	}

	@Override
	public ResponseMsg updateWeatherStation(Weatherstation weatherstation) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		try {
			weatherStationDAO.updateWeatherStation(weatherstation);
		} catch (Exception exception) {
			responseMsg.setMessage(exception.getMessage());
			responseMsg.setStatusCode(1);
			logger.error("Error in {}", exception);
		}
		return responseMsg;
	}

	@Override
	public ArrayList<WeatherStationTerminal> getWeatherStationTerminalById(int weatherStationId) {
		ArrayList<WeatherStationTerminal> weatherStationTerminals = null;
		try {
			weatherStationTerminals = weatherStationDAO.getWeatherStationTerminalById(weatherStationId);
		} catch (Exception exception) {

			logger.error("Error in {}", exception);
		}
		return weatherStationTerminals == null ? new ArrayList<WeatherStationTerminal>(0) : weatherStationTerminals;
	}

	@Override
	public int getWeatherStationTerminalMaxId() {
		int weatherStationTerminalMaxId = 0;
		try {
			weatherStationTerminalMaxId = weatherStationDAO.getWeatherStationTerminalMaxId();
		} catch (Exception exception) {

			logger.error("Error in {}", exception);
		}
		return weatherStationTerminalMaxId;
	}

	@Override
	public ResponseMsg batchInsertWeatherStationTerminal(int weatherStationId, List<WeatherStationTerminal> weatherStationTerminals, List<WeatherStationTerminal> orginalTerminals) {
		ResponseMsg responseMsg = new ResponseMsg();
		responseMsg.setStatusCode(0);
		Jedis client = null;
		if (weatherStationTerminals == null) {
			throw new IllegalArgumentException("添加的设备不能为空！");
		}
		try {
			if (weatherStationTerminals.size() > 0) {
				RedisPoolManager.Init(jedisPool);
				client = RedisPoolManager.getJedis();
				ObjectSerializeTransfer<Terminalparameters> objectSerializeTransfer = new ObjectSerializeTransfer<>();
				Terminalparameters terminalparameter;
				Map<byte[], byte[]> weatherStationTerminalMap = new HashMap<>();
				List<WeatherStationTerminal> terminalAutoFrequencies = new ArrayList<>(weatherStationTerminals.size());
				List<AutoFrequencyTerminal> autoFrequencyTerminals = new ArrayList<>(terminalAutoFrequencies.size());
				List<String> parametersAttrs;
				byte[] key;
				WeatherStationTerminal terminalBefore;
				Map<Integer, WeatherStationTerminal> originalTerminalMap = Maps.uniqueIndex(orginalTerminals, new Function<WeatherStationTerminal, Integer>() {
					@Override
					public Integer apply(WeatherStationTerminal input) {
						return input.getWeatherStationTerminalId();
					}
				});
				int weatherStationTerminalId = 0;
				for (WeatherStationTerminal terminal : weatherStationTerminals) {
					terminal.setWeatherStationTerminalId(redisAtomicInteger.getAndIncrement());
					terminal.setWeatherStationId(weatherStationId);
					terminal.setTeminalParameterCategoryId(HexUtil.HexToInt(terminal.getUniqueTPCId()));
					if (!StringUtils.isBlank(terminal.getUniqueWSTId())) {
						weatherStationTerminalId = HexUtil.HexToInt(terminal.getUniqueWSTId());
					}
					if (weatherStationTerminalId > 0 && null != originalTerminalMap.get(weatherStationTerminalId)) {
						terminalBefore = originalTerminalMap.get(weatherStationTerminalId);
						if (!(terminalBefore.getAcquisitionFrequency() + terminalBefore.getAcquisitionFrequencyUnit()).equalsIgnoreCase(terminal.getAcquisitionFrequency() + terminal.getAcquisitionFrequencyUnit())) {
							terminalAutoFrequencies.add(terminal);
						}
						autoFrequencyTerminals.add(new AutoFrequencyTerminal(weatherStationId, terminal.getWeatherStationTerminalId(), terminalBefore.getWeatherStationTerminalId(), terminalBefore.getTerminalModel()));
						client.hdel(RedisKey.ALLWEATHERSYSTEMKEY.getBytes(), terminalBefore.getTerminalModel().getBytes());
					} else {
						terminalAutoFrequencies.add(terminal);
					}
					if (StringUtils.isBlank(terminal.getTerminalParameters())) {
						key = String.valueOf(HexUtil.HexToInt(terminal.getUniqueTPCId())).getBytes();
						if (client.exists(key)) {
							terminalparameter = (Terminalparameters) objectSerializeTransfer.deserialize(client.get(key));
							if (terminalparameter != null && terminalparameter.getTerminalParametersAttrs().size() > 0) {
								parametersAttrs = new ArrayList<>(terminalparameter.getTerminalParametersAttrs().size());
								for (TerminalParametersAttrs terminalParametersAttr : terminalparameter.getTerminalParametersAttrs()) {
									if (!"N".equalsIgnoreCase(terminalParametersAttr.getShowInPage())) {
										parametersAttrs.add(terminalParametersAttr.getId());
									}
								}
								if (parametersAttrs.size() > 0) {
									terminal.setTerminalParameters(StringUtils.join(parametersAttrs, ","));
								}

							}
						}
					}
				}
				int lastWSTId = weatherStationDAO.batchInsertWeatherStationTerminal(weatherStationTerminals);
				if (lastWSTId < 1) {
					responseMsg.setStatusCode(1);
					responseMsg.setMessage("添加站点设备失败！");
				} else {
					Iterator<Integer> terminalIterator = originalTerminalMap.keySet().iterator();
					while (terminalIterator.hasNext()) {
						client.hdel(RedisKey.ALLWEATHERSYSTEMKEY.getBytes(), originalTerminalMap.get(terminalIterator.next()).getTerminalModel().getBytes());
					}
					for (WeatherStationTerminal terminal : weatherStationTerminals) {
						weatherStationTerminalMap.put(terminal.getTerminalModel().getBytes(), objectSerializeTransfer.serialize(terminal));
					}
					if (autoFrequencyTerminals.size() > 0) {
						autoFrequencyHistoryService.batchUpdateAutoFrequencyHistory(autoFrequencyTerminals);
					}
					client.hmset(RedisKey.ALLWEATHERSYSTEMKEY.getBytes(), weatherStationTerminalMap);
					Autofrequencyhistory autofrequencyhistory;
					List<Autofrequencyhistory> autofrequencyhistories = new ArrayList<>(weatherStationTerminals.size());
					Date now = new Date();
					Timestamp createDate = new Timestamp(now.getTime());
					for (WeatherStationTerminal terminal : terminalAutoFrequencies) {
						if (0 < terminal.getWeatherStationTerminalId()) {
							autofrequencyhistory = new Autofrequencyhistory();
							autofrequencyhistory.setWeatherStationId(weatherStationId);
							autofrequencyhistory.setWeatherStaionTerminalId(terminal.getWeatherStationTerminalId());
							autofrequencyhistory.setAutoFrequency(terminal.getAcquisitionFrequency());
							autofrequencyhistory.setAutoFrequencyUnit(terminal.getAcquisitionFrequencyUnit());
							autofrequencyhistory.setCreateDate(createDate);
							autofrequencyhistories.add(autofrequencyhistory);
						}
					}
					if (autofrequencyhistories.size() > 0) {
						autoFrequencyHistoryService.batchInsertAutoFrequencyHistory(autofrequencyhistories);
					}
				}
			}
		} catch (Exception ex) {
			responseMsg.setStatusCode(1);
			responseMsg.setMessage("添加站点设备失败！");
			logger.error("Erron in {}", ex);
		} finally {
			RedisPoolManager.close(client);
		}
		return responseMsg;
	}

	@Override
	public List<Weatherstationcategory> filterWeatherStationCategorys(List<Weatherstationcategory> weatherstationcategories, String userCategory, boolean isHexId) {
		List<Weatherstationcategory> filterCategories = new ArrayList<>(weatherstationcategories.size());
		try {
			if (StringUtils.isBlank(userCategory)) {
				return filterCategories;
			}
			List<String> categoryArray = Arrays.asList(userCategory.split(","));
			if ("all".equalsIgnoreCase(userCategory) || categoryArray.size() == 0) {
				filterCategories = weatherstationcategories;
			} else {
				List<Integer> userCategories = Lists.transform(categoryArray, new Function<String, Integer>() {
					public Integer apply(String e) {
						return HexUtil.HexToInt(e);
					}
				});
				for (Weatherstationcategory category : weatherstationcategories) {
					if (userCategories.contains(category.getWeatherStationCategoryId())) {
						filterCategories.add(category);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		filterCategories = filterCategories == null ? new ArrayList<Weatherstationcategory>(0) : filterCategories;
		if (isHexId) {
			for (Weatherstationcategory category : filterCategories) {
				category.setUniqueCategoryId(HexUtil.IntToHex(category.getWeatherStationCategoryId()));
				category.setWeatherStationCategoryId(0);
			}
		}
		return filterCategories;
	}

	@Override
	public List<Weatherstation> filterWeatherStation(List<Weatherstation> weatherstations, String userStation, boolean isHexId) {
		List<Weatherstation> filterStations = new ArrayList<>(weatherstations.size());
		try {
			if (StringUtils.isBlank(userStation)) {
				return filterStations;
			}
			List<String> stationArray = Arrays.asList(userStation.split(","));
			if ("all".equalsIgnoreCase(userStation) || stationArray.size() == 0) {
				filterStations = weatherstations;
			} else {
				List<Integer> userStations = Lists.transform(stationArray, new Function<String, Integer>() {
					public Integer apply(String e) {
						return HexUtil.HexToInt(e);
					}
				});
				for (Weatherstation station : weatherstations) {
					if (userStations.contains(station.getWeatherStationId())) {
						filterStations.add(station);
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		filterStations = filterStations == null ? new ArrayList<Weatherstation>(0) : filterStations;
		if (isHexId) {
			for (Weatherstation weatherstation : filterStations) {
				weatherstation.setUniqueWeatherStationId(HexUtil.IntToHex(weatherstation.getWeatherStationId()));
				weatherstation.setWeatherStationId(0);
				weatherstation.setUniqueCategoryId(HexUtil.IntToHex(weatherstation.getWeatherStationCategoryId()));
				weatherstation.setWeatherStationCategoryId(0);
			}
		}
		return filterStations;
	}

	@Override
	public List<Weatherstation> getWeatherStationsByCategory(List<Integer> categories) {
		List<Weatherstation> weatherStations = null;
		try {
			weatherStations = weatherStationDAO.getWeatherStationsByCategory(categories);
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherStations == null ? new ArrayList<Weatherstation>(0) : weatherStations;
	}

	@Override
	public List<WeatherStationTerminal> getTerminalsByStationId(List<Integer> categories) {
		List<WeatherStationTerminal> weatherStationTerminals = null;
		try {
			weatherStationTerminals = weatherStationDAO.getTerminalsByStationId(categories);
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return weatherStationTerminals == null ? new ArrayList<WeatherStationTerminal>(0) : weatherStationTerminals;
	}

	@Override
	public int getWeatherStationTerminalCount() {
		int count = 0;
		try {
			count = weatherStationDAO.getWeatherStationTerminalCount();
		} catch (Exception exception) {
			logger.error("Error in {}", exception);
		}
		return count;
	}

	@Override
	public List<WeatherstationClient> getWeatherStationClients() {
		List<WeatherstationClient> WeatherstationClients = null;
		try {
			WeatherstationClients = weatherStationDAO.getWeatherStationClients();
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return WeatherstationClients == null ? new ArrayList<WeatherstationClient>(0) : WeatherstationClients;
	}

	@Override
	public List<WeatherstationClient> getWSClientsByStationId(List<Integer> weatherStations) {
		List<WeatherstationClient> WeatherstationClients = null;
		try {
			if (weatherStations == null)
				return new ArrayList<WeatherstationClient>(0);
			if (weatherStations.size() < 1)
				return new ArrayList<WeatherstationClient>(0);

			WeatherstationClients = weatherStationDAO.getWSClientsByStationId(weatherStations);
		} catch (Exception ex) {
			logger.error("Erron in {}", ex);
		}
		return WeatherstationClients == null ? new ArrayList<WeatherstationClient>(0) : WeatherstationClients;
	}

	@Override
	public int getWeatherStationCountByPort(int gRPSPort) {
		return weatherStationDAO.getWeatherStationCountByPort(gRPSPort);
	}
}
