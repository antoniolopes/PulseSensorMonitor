package data;

import java.util.Calendar;

public class CurrentDate {
	private int day = 0;
	private int month = 0;
	private int year = 0;
	private int hours = 0;
	private int minutes = 0;
	private int seconds = 0;

	public CurrentDate() {
		refresh();
	}

	public CurrentDate(long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		set(cal);
	}

	public void refresh() {
		set(Calendar.getInstance());
	}

	private void set(Calendar cal) {
		day = cal.get(Calendar.DATE);
		month = cal.get(Calendar.MONTH) + 1;
		year = cal.get(Calendar.YEAR);
		hours = cal.get(Calendar.HOUR_OF_DAY);
		minutes = cal.get(Calendar.MINUTE);
		seconds = cal.get(Calendar.SECOND);
	}

	public String toString() {
		return defaultFormat();
	}

	private String defaultFormat() {
		return day + "/" + month + "/" + year + " " + ((hours < 10) ? "0" + hours : hours) + ":" + ((minutes < 10) ? "0" + minutes : minutes) + ":" + ((seconds < 10) ? "0" + seconds : seconds);
	}

	public String shortFormat() {
		return year + "" + ((month < 10) ? "0" + month : month) + "" + ((day < 10) ? "0" + day : day) + "_" + hours + "" + minutes + "" + seconds;
	}
}
