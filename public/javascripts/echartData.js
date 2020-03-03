$(function(){
	
	var myChart = echarts.init(document.getElementById('menudiv_img'));

	// 图表使用-------------------
	var option = {
	    legend: {
	        padding: 5,
	        itemGap: 10,
	        data: ${chartBean.legend.raw()}
	    },
	    tooltip: {
	        trigger: 'item',
	    },
	    xAxis: [
	        {
	            type: 'category',
	            data: ${chartBean.axis.raw()}
	        }
	    ],
	    yAxis: [
	        {
	            type: 'value',
	            boundaryGap: [0, 0.1],
	            splitNumber: 4
	        }
	    ],
	    series: [
	        {
	            name: '新增理财会员',
	            type: 'line',
	            data: ${chartBean.mapData.get('aa')}
	        },
	        {
	            name: '新增注册会员',
	            type: 'line',
	            data: ${chartBean.mapData.get('bb')}
	        }
	    ]
	};
	
	myChart.setOption(option);
	
});