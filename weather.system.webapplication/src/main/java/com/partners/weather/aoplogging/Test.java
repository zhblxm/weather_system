package com.partners.weather.aoplogging;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.partners.entity.AutoFrequencyTerminal;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}

	public static void printDifference(Date startDate, Date endDate) {

		Interval interval = new Interval(startDate.getTime(), endDate.getTime());
		Period period = interval.toPeriod();

		DateTime start = new DateTime(startDate);

		DateTime end = new DateTime(endDate);

		
		 DateTime dabct = new DateTime(new Timestamp(System.currentTimeMillis()));



		Period p = new Period(start,end,PeriodType.hours());
		int day = p.getHours()/6+(p.getHours()%6==0?0:1);
		System.out.printf("%d\r\n",day);
		System.out.printf("%d years, %d months, %d days, %d hours, %d minutes, %d seconds%n", period.getYears(), period.getMonths(), period.getDays(), period.getHours(), period.getMinutes(), period.getSeconds());

	}

	public static void main(String[] args) throws ParseException {

		  List persons = Lists.newArrayList(
	                new AutoFrequencyTerminal(1, 1,1,"a"),
	                new AutoFrequencyTerminal(2, 2,2,"b"),
	                new AutoFrequencyTerminal(3, 3,3,"c")
	        );
 
			Map<String,AutoFrequencyTerminal> map = Maps.uniqueIndex(persons, new Function<AutoFrequencyTerminal,String>() {

				@Override
				public String apply(AutoFrequencyTerminal input) {
					// TODO Auto-generated method stub
					return input.getTerminalModel();
				}
					 

		        });
		 
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/M/yyyy hh:mm:ss:SSS");
		SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyyMMddhhmmssSSS");
		System.out.println(Long.parseLong(simpleDateFormat1.format(new Date()))*100);
		
		SecureRandom random = new SecureRandom();
		System.out.println(Math.abs(random.nextInt(100)));
	 
	
		try {
			//20170905103510707
			//9223372036854775807
			//6472175285787712970
			Date date1 = simpleDateFormat.parse("10/10/2013 11:30:10:123");
			Date date2 = simpleDateFormat.parse("13/11/2014 20:35:55:345");

			printDifference(date1, date2);

		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

}
