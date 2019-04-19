package org.smart.framework.util;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {
	public static ZoneId zoneId = ZoneId.of("Asia/Shanghai");
	public static void main(String[] args) {
		System.out.println(inMonth(LocalDateTime.of(2018, 2, 2, 1, 1).toEpochSecond(ZoneOffset.UTC)));
		System.out.println(inToday(LocalDateTime.of(2018, 7, 6, 7, 6).toEpochSecond(ZoneOffset.UTC)));
		System.out.println(inWeek(LocalDateTime.of(2018, 8, 28, 1, 1).toEpochSecond(ZoneOffset.UTC)));
		System.out.println(getNow());
		System.out.println(getNowSeconds());
		System.out.println(System.currentTimeMillis() / 1000);
	}
	
	
	/**
	 * 获取当前时间( UTC 1970  秒)
	 * @return
	 */
	public static long getNow() {
		Instant instant = Instant.now(Clock.system(zoneId));
		long timeStampMillis = instant.toEpochMilli();
		return timeStampMillis;
	}
	
	public static int getNowSeconds() {
		Long time = getNow() / 1000;
		return time.intValue();
	}
	
	/**
	 * 今日零点时间（秒）
	 * @return
	 */
	public static long todayZero() {
		return  LocalDate.now().atStartOfDay(zoneId).toEpochSecond();
	} 
	/**
	 * 明日零点时间（秒）
	 * @return
	 */
	public static long tomorrowZero() {
		return  LocalDate.now().plusDays(1).atStartOfDay(zoneId).toEpochSecond();
	} 
	
	public static long currentSeconds() {
		Long now = new Long(System.currentTimeMillis() / 1000);
		return  now.intValue();
	}
	/**
	 * 是否在当月时间范围内
	 * 这个月第一天的0:00:00至下个月第一天的0:00:00
	 * @param seconds  utc时间转换为秒
	 * @return
	 */
	public static boolean inMonth(long seconds) {
		LocalDateTime now = LocalDateTime.now();
		LocalDate nowMonth = LocalDate.of(now.getYear(), now.getMonth(), 1);
		LocalDateTime tmp = now.plusMonths(1);
		LocalDate nextMonth = LocalDate.of(tmp.getYear(), tmp.getMonth(), 1);
		long sSeconds = nowMonth.atStartOfDay(zoneId).toEpochSecond();
		long eSeconds = nextMonth.atStartOfDay(zoneId).toEpochSecond();
		return seconds >= sSeconds && seconds < eSeconds;
	}
	/**
	 * 是否在昨日时间范围内
	 * @param seconds utc时间转换为秒
	 * @return
	 */
	public static boolean inYesterday(long seconds) {
		long yesterdayZero = LocalDate.now().plusDays(-1).atStartOfDay(zoneId).toEpochSecond();
		return seconds >= yesterdayZero && seconds < todayZero();
	}
	
	public static boolean inToday(long seconds) {
		return todayZero() <= seconds && seconds < tomorrowZero();
		
	}
	public static boolean inWeek(long seconds) {
		return inWeek(seconds, DayOfWeek.SUNDAY);
	}
	public static boolean inWeek(long seconds,DayOfWeek start) {
		
		long sSeconds = weekStartZero(start);
		long eSeconds = weekEndZero(start);
		return sSeconds <= seconds && seconds < eSeconds;
		
	}
	
	public static long weekStartZero(DayOfWeek start) {
		LocalDate lastWeek = (LocalDate) TemporalAdjusters.previous(start).adjustInto(LocalDate.now());
		return lastWeek.atStartOfDay(zoneId).toEpochSecond();
	}
	public static long weekEndZero(DayOfWeek start) {
		LocalDate nowWeek = (LocalDate) TemporalAdjusters.next(start).adjustInto(LocalDate.now());
		return nowWeek.atStartOfDay(zoneId).toEpochSecond();
	}
	public static Date getDelayDate(int delay, TimeUnit timeUnit) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		switch (timeUnit) {
		case DAYS:
			c.add(Calendar.DAY_OF_YEAR, delay);
			break;
		case HOURS:
			c.add(Calendar.HOUR_OF_DAY, delay);
			break;
		case MILLISECONDS:
			c.add(Calendar.MILLISECOND, delay);
			break;
		case MINUTES:
			c.add(Calendar.MINUTE, delay);
			break;
		case SECONDS:
			c.add(Calendar.SECOND, delay);
			break;
		default:
			break;
		}
		return c.getTime();
	}
	
	/**
	 * 获取整点时间
	 * @param hour	设置整点(0-23)
	 * @return 返回utc时间毫秒
	 */
	public static long setFixTime(int hour) {
		Calendar currentTime = Calendar.getInstance();
		int curentHour = currentTime.get(Calendar.HOUR_OF_DAY);
		if (curentHour >= hour){
			hour += 24;
		}
		Calendar nowDay = Calendar.getInstance();
		nowDay.set(Calendar.HOUR_OF_DAY, hour);
		nowDay.set(Calendar.MINUTE, 0);
		nowDay.set(Calendar.SECOND, 0);
		nowDay.set(Calendar.MILLISECOND, 0);

		return nowDay.getTimeInMillis();
	}
	
	/**
	 * 获取timeUnit字段指定的整数时间 example : delay 指定延时周期, timeUnit为MINUTE 那么每分钟00秒
	 * 
	 * @param delay
	 *            延时周期
	 * @param timeUnit
	 *            时间字段
	 * @return
	 */
	public static Date getRoundDelayDate(TimeUnit timeUnit) {
		Date now = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.set(Calendar.MILLISECOND, 0);
		switch (timeUnit) {
		case HOURS:
			c.set(Calendar.HOUR_OF_DAY, 0);
			break;
		case MINUTES:
			c.add(Calendar.MINUTE, 1);
			c.set(Calendar.SECOND, 0);
			break;
		case SECONDS:
			c.set(Calendar.SECOND, 0);
			break;
		default:
			break;
		}
		return c.getTime();
	}
	
	/**
	 * 根据当前时间获取一个整点时间
	 * @return	返回utc时间毫秒
	 */
	public static long getNextHourTime() {
		Calendar nowDay = Calendar.getInstance();
		int minute = nowDay.get(Calendar.MINUTE);
		int second = nowDay.get(Calendar.SECOND);
		int millSecond = nowDay.get(Calendar.MILLISECOND);
		if(minute == 0 && second == 0 && millSecond == 0) {
			return nowDay.getTimeInMillis();
		}

		nowDay.add(Calendar.HOUR_OF_DAY, 1);
		nowDay.set(Calendar.MINUTE, 0);
		nowDay.set(Calendar.SECOND, 0);
		nowDay.set(Calendar.MILLISECOND, 0);

		return nowDay.getTimeInMillis();
	}
	
}
