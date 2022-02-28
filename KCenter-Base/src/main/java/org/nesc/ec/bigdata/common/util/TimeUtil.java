package org.nesc.ec.bigdata.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class TimeUtil {

	public static DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


	private static String msTransToHours(String mills) {
		double mill = Double.parseDouble(mills);
		double hours = mill/(60*60*1000);
		BigDecimal bg = new BigDecimal(hours);
		return String.valueOf(bg.setScale(4, RoundingMode.HALF_UP));
	}
	
	public static long hoursTransToMiss(int hours) {
		long hour = Long.valueOf(hours);
		return hour*60*60*1000;
		
	}
	

	public static int tranToMiss(String interval) {
		String units = interval.substring(interval.length()-1, interval.length());
		int value = Integer.parseInt(interval.substring(0,interval.length()-1));
		int result = 0;
		switch (units) {
		case "m":	
			result = value * 60;
			break;
		case "h":	
			result = value * 60 *60;
			break;
		case "d":			
			result = value * 24 *60 *60;
			break;
			default:;
		}
		return result;
	}

	public static Calendar nowCalendar(){
		long now = System.currentTimeMillis();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(now);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		return cal;
	}

	public static Set<String> getBetweenTime(String startTime, String endTime){
		Set<String> dates = new TreeSet<>(String::compareTo);
		LocalDate startDate = LocalDate.parse(startTime,date_formatter);
		LocalDate endDate = LocalDate.parse(endTime,date_formatter);
		long distance = ChronoUnit.DAYS.between(startDate,endDate);
		if(distance<1){
			dates.add(startTime);
			return dates;
		}
		Stream.iterate(startDate, d -> d.plusDays(1)).limit(distance + 1).forEach(f -> dates.add(f.toString()));
		return dates;
	}

}
