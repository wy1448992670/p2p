package utils.ymsms.httpclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA. User: WHL Date: 2008-11-5 Time: 14:03:26 To change this template use File | Settings | File Templates.
 */
public class GetProperties {
	private static Properties p = null;
	private static Logger logger = Logger.getLogger(GetProperties.class);

	public static Properties getProperties(String filename) {
		p = new Properties();
		File f = new File(filename);
		FileInputStream fis = null;
		InputStreamReader isr = null;
		try {
			fis = new FileInputStream(f);
			isr = new InputStreamReader(fis, "GBK");
			p.load(isr);
		} catch (FileNotFoundException e) {
			logger.info("���ļ�������!");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (isr != null) {
				try {
					isr.close();
				} catch (IOException e) {
					e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
				}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
				}
			}
		}
		return p;
	}

	public static String getFile(String fpath) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date time = new Date(System.currentTimeMillis());
		String datestr = sdf.format(time);
		Calendar c = new GregorianCalendar();
		int yearnow = c.get(Calendar.YEAR);
		int monthnow = c.get(Calendar.MONTH) + 1;
		int daynow = c.get(Calendar.DAY_OF_MONTH);
		int hournow = c.get(Calendar.HOUR_OF_DAY);
		if (hournow == 0) {
			hournow = 23;
			if (monthnow > 1 && monthnow <= 12) {
				monthnow = monthnow - 1;
				if (getyear(yearnow, monthnow) == 31 && daynow > 1 && daynow <= 31) {
					daynow = daynow - 1;
				} else if (getyear(yearnow, monthnow) == 30 && daynow > 1 && daynow <= 30) {
					daynow = daynow - 1;
				} else if (getyear(yearnow, monthnow) == 29 && daynow > 1 && daynow <= 29) {
					daynow = daynow - 1;
				} else if (getyear(yearnow, monthnow) == 28 && daynow > 1 && daynow <= 28) {
					daynow = daynow - 1;
				} else if (getyear(yearnow, monthnow) == 31 && daynow == 1) {
					daynow = 31;
				} else if (getyear(yearnow, monthnow) == 30 && daynow == 1) {
					daynow = 30;
				} else if (getyear(yearnow, monthnow) == 29 && daynow == 1) {
					daynow = 29;
				} else if (getyear(yearnow, monthnow) == 28 && daynow == 1) {
					daynow = 28;
				}
			} else if (monthnow == 1) {
				daynow = 31;
				monthnow = 12;
				yearnow = yearnow - 1;
			}
			fpath = fpath + yearnow + monthnow + daynow + "/" + hournow + ".log";
		} else {
			hournow = hournow - 1;
			fpath = fpath + datestr + "/" + hournow + ".log";
		}
		return fpath;
	}

	public static int getyear(int year, int month) {
		int days = 0;
		int biaoji = 0; // ������
		if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
			biaoji = 1;
		} else if ((year % 4 > 0 && year % 100 == 0) || year % 400 > 0) {
			biaoji = 0;
		}
		if (month >= 1 && month <= 12) {
			if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
				days = 31;
			} else if (month == 4 || month == 6 || month == 9 || month == 11) {
				days = 30;
			} else if (biaoji == 1) {
				days = 29;
			} else {
				days = 28;
			}
		} else {
			logger.info("������·ݲ���ȷ");
		}
		return days;
	}

	public static void main(String[] args) {
		Properties p = getProperties("conf/wapsp.properties");
		System.out.println(p.getProperty("mtpath"));

	}
}
