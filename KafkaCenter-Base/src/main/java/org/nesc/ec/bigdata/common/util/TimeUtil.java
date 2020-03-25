package org.nesc.ec.bigdata.common.util;

import java.math.BigDecimal;

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
		}
		return result;
	}
}
