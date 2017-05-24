package com.bonc.ftputil.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	public static void main(String[] args) {
		// System.out.println(Arrays.toString(getDistanceHours("161026/10",
		// "yyMMdd/HH")));
		// System.out.println(Arrays.toString(getNowDate("yyMMdd/HH")));
		// System.out.println(Arrays.toString(getOptimes("yyyyMMdd")));
	}

	/**
	 * 获取指定时间到当前时间前两小时的时间列表
	 */
	public static String[] getDistanceHours(String hour, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);// "yyMMdd/HH"
		Date one;
		Date two;
		int hours = 0;
		String[] optime = null;
		try {
			one = new Date();
			two = sdf.parse(hour);
			long time1 = one.getTime();
			long time2 = two.getTime();
			long diff = time1 - time2;
			hours = (int) (diff / (1000 * 60 * 60));
			if (hours > 1) {
				optime = new String[hours - 1];
			}
			Calendar cal = Calendar.getInstance();
			String time = null;
			for (int i = 0; i < hours - 1; i++) {
				if (i == 0) {
					cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 2);
				} else {
					cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 1);
				}
				time = sdf.format(cal.getTime());
				optime[i] = time;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return optime;
	}

	/**
	 * 获取当前时间及前一小时
	 */
	public static String[] getNowDate(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String[] optime = new String[2];
		Calendar cal = Calendar.getInstance();
		String time = sdf.format(cal.getTime());
		optime[0] = time;// 获取到完整的时间

		cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 1);
		time = sdf.format(cal.getTime());
		optime[1] = time;// 获取到完整的时间
		return optime;
	}

	/**
	 * 获取当前时间及前4个小时
	 */
	public static String[] getNowDateAndBeforeFour(String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String[] optime = new String[5];
		Calendar cal = Calendar.getInstance();
		String Now_time = sdf.format(cal.getTime());
		optime[0] = Now_time;// 获取到完整的时间

		for (int i = 1; i < 5; i++) {
			cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 1);
			optime[i] = sdf.format(cal.getTime());
		}
		return optime;
	}

	/**
	 * 获取前两天的的小时目录
	 */

	public static String[] getBeforeTwoDayHour(String format) {

		String time[] = new String[1];
		SimpleDateFormat sdf = new SimpleDateFormat(format);

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - 48);

		time[0] = sdf.format(cal.getTime());
		return time;

	}

	/***
	 * 以指定格式返回日期字符串
	 * 
	 * @param date
	 * @param format
	 * @return
	 */
	public static String formatDate(Date date, String format) {

		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);

			return sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/***
	 * 以指定格式返回日期字符串
	 * 
	 * @param ts
	 * @param format
	 * @return
	 */
	public static String formatDate(Timestamp ts, String format) {

		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);

			return sdf.format(ts);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 以指定格式解析字符串为日期
	 * 
	 * @param dateStr
	 * @param dateFormat
	 * @return
	 */
	public static Date parseDate(String dateStr, String dateFormat) {

		if (dateStr != null && dateFormat != null) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

				return sdf.parse(dateStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 获取当前帐期和上一账期
	 * 
	 * @param dateFormat
	 * @return
	 */
	public static String[] getOptimes(String format) {
		String opTimes[] = null;
		SimpleDateFormat sdf = new SimpleDateFormat(format);

		Calendar cal = Calendar.getInstance();

		int currentHour = cal.get(Calendar.HOUR_OF_DAY);

		if (currentHour == 0 || currentHour == 1) {// 扫描当前账期和上一账期
			opTimes = new String[2];

			opTimes[0] = sdf.format(cal.getTime());

			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 1);

			opTimes[1] = sdf.format(cal.getTime());

			return opTimes;
		} else {
			opTimes = new String[1];

			opTimes[0] = sdf.format(cal.getTime());

			return opTimes;
		}

	}
}
