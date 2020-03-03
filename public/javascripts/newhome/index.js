/*$(function(){    导航箭头
	
	$("#nav1 ul li").bind("click",function(){
		var index=$(this).index();
		$(this).children("img").show();
		$(this).siblings('li').children("img").hide();
	})
	
})*/
/*导航边框*/
$(function(){
	$("#nav1 ul li").bind("click",function(){
		var index=$(this).index();
		$(this).css("border","1px solid #ee4035")
		$(this).children("a").css("color","#ee4035")
		$(this).siblings('li').css("border","1px solid rgba(255,255,255,0)");
		$(this).siblings('li').children("a").css("color","#777");
	})
	
})




/*轮播宽度兼容*/
$(document).ready(function(){
		
		
		if(window.screen.width>=1440){
			$("#pics .pall").css("width","1423px")
		
		}
		if(window.screen.width>=1366 && window.screen.width<1440 ){
			$("#pics .pall").css("width","1349px")
		
		}
		if(window.screen.width>=1360 && window.screen.width<1366 ){
			$("#pics .pall").css("width","1343px")
			
		}
		if(window.screen.width>=1280 && window.screen.width<1360 ){
			$("#pics .pall").css("width","1263px")
			
		}

})


/*选项卡*/
$(function(){
	$("#news_title li").bind("click",function(){

		var index=$(this).index();//获取当前点击的li的索引值
		$(this).children().addClass("active").parent().siblings().children().removeClass("active");//给当前的li加上类active，它的同胞元素去掉类
		$('#news_more').attr('href',$(this).children().attr('url'));
		$("#content div").eq(index).show().siblings("#content div").hide();//让对应当前li的content里div的元素显示
		var sh=$("#content div").eq(index).show()
		if(sh){
			$(this).css("color","#676767");
		}
	});
});

/*上边二维码*/
$(function(){
	$(".gzh").bind("mouseover",function(){
		$(".na").css("display","block")
		
	})
	$(".gzh").bind("mouseout",function(){
		$(".na").css("display","none")
		
	})
})
//下边二维码         （完全新增）
$(function(){
	$(".foot_wxbo").bind("mouseover",function(){
		$(".code_bot").css("display","block")
	})
	$(".foot_wxbo").bind("mouseout",function(){
		$(".code_bot").css("display","none")
	})
})

/*兼容超出部分以省略号显示*/
$(function(){
	for(var i=0;i<$(".vioce_sp").length;i++){
		var vioce_sp1=$(".vioce_sp").eq(i).html().length;
		var vioce_sph=$(".vioce_sp").eq(i).html();
		if(vioce_sp1>40){
			var wa=vioce_sph.substring(0,40);
		    var was=wa+("...");
			$(".vioce_sp").eq(i).html(was);
			
		}
	}
	
})







/*$(function(){
	var vioce_spl=$(".vioce_sp").html().length;
	var vioce_sph=$(".vioce_sp").html();
	var vioce_arr=[".vioce_sp",".vioce_sp2",".vioce_sp3",".vioce_sp4",".vioce_sp5",".vioce_sp6"];
	for(i=0;i<vioce_arr.length;i++){
		console( $(".vioce_arr[i]"));
		if($("vioce_arr[i]").html()>10){
			var wa=$("vioce_arr[i]").substring(0,40);
			alert($("vioce_arr[i]"));
			var was=wa+("...");
	        $("vioce_arr[i]").html(was);
			
		}
		//return true;
	}
	
})*/


/*投资状态颜色变化*/
$(function(data){
	var arr=[0,1,1,0,1,1,0,1,1,0,1,1]  //0为热售中 1为售罄 
	var arrPro = [1,2,3,4,5,6,7,8,9,10,11,12];
	//var arra = [".on_once",".on_twice",".on_times",".con2_once",".con2_twice",".con2_times",".con3_once",".con3_twice",".con3_times",".con4_once",".con4_twice",".con4_times"]
	var arra = [[".on_once",".on_twice",".on_times"],[".con2_once",".con2_twice",".con2_times"],[".con3_once",".con3_twice",".con3_times"],[".con4_once",".con4_twice",".con4_times"]]
	var arrColorHot = ["#ee4035","#f9b552","#89abd9","#89c997"]
	var arrColorLine = ["2px solid #ee4035","2px solid #f9b552","2px solid #89abd9","2px solid #89c997"]
	for (i = 0;i < arr.length;i++){
		var group = parseInt(i / 3);
		var par = parseInt(i % 3);
		if(arr[i] == 0){
			$(arra[group][par]).parent().parent().css("border-top",arrColorLine[group]);
			$(arra[group][par]).css("background",arrColorHot[group]);
			$(arra[group][par]).val("立即出借");
		}else if(arr[i] == 1){
			$(arra[group][par]).parent().parent().css("border-top","2px solid #949494");
			$(arra[group][par]).css("background","#949494");
			$(arra[group][par]).val("售罄");
		}
	}
})

/*用户心声名称星号省略*/
$(function(){


	
	for(var i=0;i<$(".voice_name").length;i++){
		var voice_name=$(".voice_name").eq(i).html().length;
		var voice_nameh=$(".voice_name").eq(i).html();
		/*alert(voice_name);*/
		if(voice_name>4){
			var bef_all=voice_nameh.substring(0,voice_name-3);
			var end_third=voice_nameh.substring(voice_name-3,voice_name);
			var bef_all=("***");
			$(".voice_name").eq(i).html(bef_all+end_third);
		}else{
			$(".voice_name").eq(i).html('***' + voice_nameh.substring(voice_name-1,voice_name));
		}
	}
	for(var i=0;i<$(".vioce_sp").length;i++){
		var vioce_sp1=$(".vioce_sp").eq(i).html().length;
		var vioce_sph=$(".vioce_sp").eq(i).html();
//			alert($(".vioce_sp").eq(i).html());
			if(vioce_sp1>40){
			var wa=vioce_sph.substring(0,40);
			var was=wa+("...");
			$(".vioce_sp").eq(i).html(was);
			
		}
	}
	
	
})


/*风云榜省略号显示*/
$(function(){
	for(var i=0;i<$(".fy").length;i++){
		var fy=$(".fy").eq(i).html().length;
		var fyh=$(".fy").eq(i).html();
		/*alert(fy);*/
		if(fy>4){
			var bef_all=fyh.substring(0,fy-3);
			var end_third=fyh.substring(fy-3,fy);
			var bef_all=("***");
			$(".fy").eq(i).html(bef_all+end_third);
			
			
		}
	}
})


/*累计交易     千分位*/

$(function(){
   for(var i=0;i<$(".thousands").length;i++){
	var tho_now=$(".thousands").eq(i);
	var tho_nowh=$(".thousands").eq(i).html();
    if(tho_nowh.length>=3 ){
   	    var me = tho_now, txt = me.html().replace(/,/g,'');
        txt = txt.replace (/\d{1,3}(?=(\d{3})+(\.\d*)?$)/g, '$&,');
                me.html(txt);	
       	   	}
   	   	
   	   }
   	    
   });

//标的总额千分位

$(function(){
	for(var i=0;i<$(".total_sum").length;i++){
		var tho_now=$(".total_sum").eq(i);
		var tho_nowh=$(".total_sum").eq(i).html();
		
//			var to=tho_nowh.substring(0,11);
		var to=tho_nowh.replace(/,/g,'');
		var to=to.replace (/\d{1,3}(?=(\d{3})+(\.\d*)?$)/g, '$&,');
		var to_le=to.length;
		var to=to.substring(0,to_le-3)
		    tho_now.html(to);
		if(to.length>13){
			var to=to.substring(0,13);
			var was=to+("...");
			tho_now.html(was);

			 };
		
		    }
		
	   });


$(function(){
	for(var i=0;i<$(".bd_name").length;i++){
		var tho_now=$(".bd_name").eq(i);
		var tho_nowh=$(".bd_name").eq(i).html();
		if(tho_nowh.length>10){
			var to=tho_nowh.substring(0,10);
			tho_now.html(to);
		}
    }
});

//上边app二维码
$(function(){
	$(".top_yi").bind("mouseover",function(){
		$(".pic_yi").css("display","block")
	})
	$(".top_yi").bind("mouseout",function(){
		$(".pic_yi").css("display","none")
	})
})

$(function(){
	$(".top_yi").bind("mouseover",function(){
		$(".pic_yi").css("display","block")
	})
	$(".top_yi").bind("mouseout",function(){
		$(".pic_yi").css("display","none")
	})
})
$(function() {
	$("#back").hide();
	$(".code_big").hide();
	$(window).scroll(function() {
		var sT = $(window).scrollTop();
		if (sT > 150) {
			$("#back").show();
		} else {
			$("#back").hide();
		}
	});
	$("#back").bind("click", function() {
		$("html,body").animate({
			"scrollTop" : 0
		}, 1000)
	});
	$(".code").show();
	$(document).ready(function() {
		$("#tel").hover(function() {
			$(".code_big").show();
		}, function() {
			$(".code_big").hide();
		});
		$(".code_big").hover(function() {
			$(this).show();
		}, function() {
			$(this).hide();
		})

	})

})