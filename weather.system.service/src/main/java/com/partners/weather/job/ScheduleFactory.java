package com.partners.weather.job;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

@Service
public class ScheduleFactory {
	private static final Logger logger = LoggerFactory.getLogger(ScheduleFactory.class);
	private static volatile ThreadPoolTaskScheduler threadPoolTaskScheduler;
	private static volatile Map<String, ScheduledFuture<?>> futureMap = new HashMap<>();
	static {
		threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setThreadNamePrefix("Weather_System_Task");
		threadPoolTaskScheduler.initialize();
	}

	public static void addSchedule(String scheduleName, CronTrigger trigger,Runnable task) {
		/// new CronTrigger("*/2 * * * * *")
		try {
			ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(task, trigger);
			futureMap.put(scheduleName, future);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
	}

	public static void cancleSchedule(String scheduleName) {
		try {
			if (futureMap.containsKey(scheduleName)) {
				ScheduledFuture<?> future = futureMap.get(scheduleName);
				future.cancel(true);
				futureMap.remove(scheduleName);
			}
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
	}
	public static boolean checkExixtsScheduler(String scheduleName) {
		try {
			return futureMap.containsKey(scheduleName);
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
		return false;
	}
	public static void stopPoolTaskScheduler() {
		try {
			threadPoolTaskScheduler.shutdown();
		} catch (Exception e) {
			logger.error("Error in {}", e);
		}
	}
}
