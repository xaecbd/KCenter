package org.nesc.ec.bigdata.common.util;

import java.math.BigDecimal;
import java.util.Calendar;

public class TimeUtil {

	private static String msTransToHours(String mills) {
		double mill = Double.parseDouble(mills);
		double hours = mill/(60*60*1000);
		BigDecimal bg = new BigDecimal(hours);
		return String.valueOf(bg.setScale(4, BigDecimal.ROUND_HALF_UP));
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
}
