package jobs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import play.Logger;
import play.Play;
import play.jobs.On;


@On("0 0/5 * * * ?")
public class UserCityJob extends BaseJob {
	public void doJob() {
		if(!"1".equals(IS_JOB))return;

		try {
			long start = System.currentTimeMillis();
			Class.forName("com.mysql.jdbc.Driver");
	 
			String url = Play.configuration.getProperty("db");
			
			int j = url.indexOf(':', url.indexOf(':') + 1);//第二个冒号
			
			String url_db = url.split("@")[1];//数据库地址
			String url_user = url.split("@")[0];//数据库用户信息
			
			int strStartIndex = url_user.indexOf("/") + 2;  //解析 '//'的位置
		     
		    String userName = url_user.substring(strStartIndex, j);  
		    String password = url_user.substring(j+1, url_user.length());
		    //使用批处理语句
		    Connection connection = DriverManager.getConnection("jdbc:mysql://" + url_db , userName, password);
		    
			connection.setAutoCommit(false);
			//只插入100条
			String sql = "INSERT INTO t_user_city (user_id, province_id, province, city_id, city) SELECT t_users.id, CONCAT(LEFT (t_users.id_number, 2), '0000' ),(SELECT t_new_province.province FROM t_new_province WHERE t_new_province.province_id = CONCAT(LEFT (t_users.id_number, 2),'0000')),CONCAT(LEFT (t_users.id_number, 4),'00'),(SELECT t_new_city.city FROM t_new_city WHERE t_new_city.city_id = CONCAT(LEFT (t_users.id_number, 4),'00')) FROM t_users where not EXISTS (SELECT t_user_city.user_id from t_user_city where t_user_city.user_id = t_users.id) and t_users.id_number is not null limit 100";
			 
			PreparedStatement cmd = connection.prepareStatement(sql);
			//for(int i = 0; i < 100; i++) {  
			cmd.addBatch();  
           // } 
			cmd.executeBatch();
			connection.commit();

			cmd.close();
			connection.close();

			long end = System.currentTimeMillis();
			Logger.info("批处理 用户地区表耗时: " + (end - start) + " ms");
			
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.info("数据库异常！");
			
		} catch (ClassNotFoundException e1) {
			Logger.info("加载类异常！");
		}
		
		
	}
}
