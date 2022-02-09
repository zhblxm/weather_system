package com.partners.weather.job;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ScheduleFactory {
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
			log.error("Error in {}", e);
		}
	}

	public static void cancelSchedule(String scheduleName) {
		try {
			if (futureMap.containsKey(scheduleName)) {
				ScheduledFuture<?> future = futureMap.get(scheduleName);
				future.cancel(true);
				futureMap.remove(scheduleName);
			}
		} catch (Exception e) {
			log.error("Error in {}", e);
		}
	}
	public static boolean checkExistsScheduler(String scheduleName) {
		try {
			return futureMap.containsKey(scheduleName);
		} catch (Exception e) {
			log.error("Error in {}", e);
		}
		return false;
	}
	public static void stopPoolTaskScheduler() {
		try {
			threadPoolTaskScheduler.shutdown();
		} catch (Exception e) {
			log.error("Error in {}", e);
		}
	}
}
