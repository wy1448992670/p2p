package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.shove.JSONUtils;

import net.sf.json.JSONArray;

/**
 * Echarts图表数据工具类
 * 页面输出数据的方法
 * 		legend:${chartBean.legend.raw()}
 *      axis:${chartBean.xAxis.raw()}
 *      series:data: ${chartBean.data.get('ios')}
 * 注意：数组形式字符串中包含特殊字符（如：双引号），需使用raw()关闭字符转义 
 */
public class ECharts {
	
	/**
	 * 图例名列表，通用
	 */
	public String[] legend; 
	
	/**
	 *  类目名列表(X轴或Y轴)，适用于直角系图表
	 */
	public String[] axis;
	
	/**
	 * 统计数据，适用于直角系图表
	 */
	public Map<String,String[]> mapData;
	
	public ECharts(){
		
	}
	
	/**
	 * 封装图表Bean,适用于直角系图表，如：line，bar，scatter，k
	 * 
	 * @param axis  X轴或Y轴类目名列表
	 * @param legend 图例名列表
	 * @param legendData  统计数据，kay：图例名，value：图例的数据统计
	 */
	public ECharts(String[] axis, String[] legend, Map<String,String[]> legendData){
		
		//获取类目列表，
		this.axis = axis;
		
		//获取图例列表
		this.legend = legend;
		
		//获取图例数据
		Map<String,String[]> map = new HashMap<String,String[]>();
		for(int i = 0; i < legend.length; i++){
			String[] _data = legendData.get(legend[i]);
			map.put(legend[i],_data);
		}

		this.mapData = map;
	}
}
