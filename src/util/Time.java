package util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Time
{
	public static long lastTimeMillis = 0;

	public static void showTime()
	{
		//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		
		
		
		//System.out.println(System.currentTimeMillis()-lastTimeMillis);
		
		lastTimeMillis = System.currentTimeMillis();
	}
}	
