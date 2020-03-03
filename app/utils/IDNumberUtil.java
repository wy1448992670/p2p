package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

public class IDNumberUtil {

	public final static boolean check18(String id_number){
		boolean flag = false;
		String bri = StringUtils.substring(id_number, 6, 14);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date briDate = sdf.parse(bri);
			Date minDate = sdf.parse(sdf.format(DateUtil.dateAddYear(new Date(), -18)));
			
			if(briDate.before(minDate)){
				return true;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	
	public static void main(String[] args) {
		System.out.println(check18("621126200002053440"));
		System.out.println(check18("621126199909123440"));
		System.out.println(check18("621126199909113440"));
		System.out.println(check18("621126198909113440"));
	}
}
