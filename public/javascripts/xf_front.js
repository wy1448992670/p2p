$(function(){	
	

	$("p").limit();	//截取字数 
	/****************导航栏效果切换*********************/
	$(".xf_h_idx_1_navul li").click(function(){
		$(this).addClass("xf_h_idx_1_navul_hover").siblings().removeClass("xf_h_idx_1_navul_hover");
	});	
	/****************导航栏效果切换*********************/
	
	
	/***banner 滑动 start ***/			
	var 
	$scroll=$(".scroll-box"), 
	winW=$(window).width(), 
	width=$scroll.width(); 
	$scroll.css("margin-left",(winW-width)/2+"px"); 
	$(window).resize(function(){ 
	var 
	winW=$(window).width(), 
	width=$scroll.width(); 
	$scroll.css("margin-left",(winW-width)/2+"px"); 
	$(".btn").css("left",($(window).width()-$(".btn").outerWidth())/2+"px"); 
	}); 


	var sWidth = $("#xf_b_idx_2_banner ul li").width(); 
	var len = $("#xf_b_idx_2_banner ul li").length; 
	var index = 0; 
	var picTimer; 

	var btn = "<div class='btnBg'></div><div class='btn'>"; 
	for(var i=0; i < len; i++) { 
	btn += "<span></span>"; 
	} 
	btn += "</div>"; 
	$("#xf_b_idx_2_banner").append(btn); 
	$("#xf_b_idx_2_banner .btnBg").css("opacity",1); 
	$(".btn").css("left",($(window).width()-$(".btn").outerWidth())/2+"px"); 
	//为小按钮添加鼠标滑入事件，以显示相应的内容 
	$("#xf_b_idx_2_banner .btn span").css("opacity",1).mouseover(function() { 
	index = $("#xf_b_idx_2_banner .btn span").index(this); 
	showPics(index); 
	}).eq(0).trigger("mouseover"); 
	$("#rightButton").bind("click",function(){
		index++; 
		if(index == len) {index = 0;} 
		showPics(index); 
	});
	
	$("#leftButton").bind("click",function(){
		index--; 
		if(index == -1) {index = len - 1;} 
		showPics(index); 
	});
	
	$("#rightButton").hover(function() { 
		clearInterval(picTimer); 
		$("#rightButton").show();
		$("#leftButton").show();
		},function() { 
		picTimer = setInterval(function() { 
		index++; 
		if(index == len) {index = 0;} 
		showPics(index); 
		},5000); 
		});
	
	$("#leftButton").hover(function() { 
		clearInterval(picTimer); 
		$("#rightButton").show();
		$("#leftButton").show();
		},function() { 
		picTimer = setInterval(function() { 
		index++; 
		if(index == len) {index = 0;} 
		showPics(index); 
		},5000); 
		}); 
	
	//鼠标滑上焦点图时停止自动播放，滑出时开始自动播放 
	$scroll.hover(function() { 
	clearInterval(picTimer);
	$("#rightButton").show();
	$("#leftButton").show();
	},function() { 
	picTimer = setInterval(function() { 
	index++; 
	if(index == len) {index = 0;} 
	showPics(index); 
	},5000); 
	$("#rightButton").hide();
	$("#leftButton").hide();
	}).trigger("mouseleave"); 

	//显示图片函数，根据接收的index值显示相应的内容 
	function showPics(index) { 
	var nowLeft = -index*1920; 
	$("#xf_b_idx_2_banner ul").stop(true,false).animate({"left":nowLeft+"px"},300); 
	$("#xf_b_idx_2_banner .btn span").removeClass("active").eq(index).addClass("active"); 
	$("#xf_b_idx_2_banner .btn span").stop(true,false).animate({"opacity":"1"},300).eq(index).stop(true,false).animate({"opacity":"1"},300); 
	} 
	
	/******banner 滑动 ends ******/	

	$(".xf_c_idx_10_sliderUl").css("width",$(".xf_c_idx_10_sliderUl li").outerWidth() * $(".xf_c_idx_10_sliderUl li").length + "");
	
	//我要借款详情页选项卡
	$(".xf_con_wyjk_tentultbody:eq(0)").show();
	$("#xf_wyjkview_xxk").children("ul").find("li").click(function(){
		var xxkli = $(this).index();
		$(this).addClass("xf_con_wyjk_r_liishot").siblings().removeClass("xf_con_wyjk_r_liishot");
		$(".xf_con_wyjk_tentultbody").eq(xxkli).show().siblings(".xf_con_wyjk_tentultbody").hide();

	});

	//我的账户左侧导航效果
	$("#xf_mem_nav_left .xf_mem_l_title_zhz").hide();
	$("#xf_mem_nav_left .xf_mem_r_more").click(function(){
		$("#xf_mem_nav_left .xf_mem_l_title_zhz").hide();
		$("#xf_mem_nav_left .xf_mem_l_title_zhz").parent().removeClass("xf_con_wyjk_leftliisshow");
		$("#xf_mem_nav_left .xf_mem_l_title_zhz,.xf_mem_r_more").removeClass("xf_mem_r_jian");
		
		var ernav = $(this).siblings(".xf_mem_l_title_zhz").is(":hidden"),
			ernav2 = $(this).siblings(".xf_mem_l_title_zhz").is(":visible")
		if(ernav){
			$(this).siblings(".xf_mem_l_title_zhz").show();
			$(this).parent().addClass("xf_con_wyjk_leftliisshow");
			$(this).addClass("xf_mem_r_jian");
		}
		if(ernav2){
			$(this).siblings(".xf_mem_l_title_zhz").hide();
			$(this).parent().removeClass("xf_con_wyjk_leftliisshow");
			$(this).removeClass("xf_mem_r_jian");
		}
	});

});

//20150504  选项卡效果
$(function(){
	$('.tab_menu ul li').click(function(){
		var  index = $(".tab_menu ul li").index(this);
		$(this).addClass("on").siblings().removeClass("on");
		$('.tab_content>div').eq(index).show().siblings().hide();
	});
});

$(function(){
	$(".l-text").bind({ 
         focus:function(){ 
		    if (this.value == this.defaultValue){
				this.value="";
				}
			},
		blur:function(){
			if (this.value == ""){ 
                this.value = this.defaultValue; 
            }
        } 
    }); 
})	