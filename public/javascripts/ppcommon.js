// JavaScript Document
$(document).ready(function(e) {
		setInterval(GetWindowWidth,10);
	});
function GetWindowWidth(){
	var w=$(window).width();
	$(".bd").css("width",w+"px");
	if (w < 1920){
   		$(".bd ul").css({"left":(w-1920)/2+"px"});
   	}else{
		$(".bd ul").css({"left":0+"px"});
	}
}

//通用选项卡 xxk
$(function(){
	function tab(li,tabox,name){
		li.eq(0).addClass(name);
		tabox.eq(0).show();
		li.click(function(){
			
			if(!$(this).hasClass("p_xq_xxjbli")){
				$(this).addClass(name).siblings().removeClass(name);
				tabox.hide().eq($(this).index()).show();
			}
		})
		
	}
	tab($(".p_zh_xxkul li"),$(".p_zh_tabox"),"on")
	tab($(".p_xq_xxkul li"),$(".p_xq_tabox"),"on")
})
// 文本框获取焦点
$(function(){
	$(".p_text").bind({ 
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
// 登录密码框
$(function(){
	$(".p_pass").bind({
        focus:function(){ 
			$(this).siblings("label").hide()
		},
		blur:function(){
			if (this.value == ""){ 
				$(this).siblings("label").show()        		
            } 
        } 
		
	})
})

$(function(){
	//首页头部边框处理
	$(".p_top_right a:first").css("border-left","0");
	$(".p_top_right a:last").css("border-right","0");
	//首页四大安全模块
	$(".p_sy_ul01 li:last").addClass("p_sy_sflast");
	//首页新闻最后li的边框处理
	$(".p_sy_ggul li:last").css("border","0");
	$(".p_sy_cgul li:last").css("border","0");
})

//返回顶部
$(function(){
	var fn;
	$(window).bind("scroll",fn=function(){
		if($(this).scrollTop()>=(0.3*$(this).height())){
			$(".p_go_top").show();
		}else{
			$(".p_go_top").hide();
		}
	});
	$(".p_go_top a").click(function(){
		var scrollTop=$(window).scrollTop();
		goTop(scrollTop);		
	});
	var time;
	function goTop(num){		
		time=setInterval(function(){
			num-=30;
			$(window).scrollTop(num);
			if(num <= 0){clearInterval(time);}
		},10);	
	}
	$(".p_go_top").click(function(){
		$(window).scrollTop(0);
	})
});

$(function(){
	$(".p_ft_icon a:last").css("margin-right","0");
	$(".p_jk_proul>:nth-child(3n)").css("margin-right","0");
	$('.p_zh_b1left>input').hover(function(){
		$(this).siblings('a').addClass('cozhuse');
	},function(){
		$(this).siblings('a').removeClass('cozhuse');
	})
});

// my account nav 我的帐户nav
$(function(){
	$(".p_zh_navul>li>h4>span").click(function(){
		var $nextUl=$(this).parent().next("ul");
		if($nextUl.is(":hidden")){
			if($nextUl.parent().attr("id") == 'li_2'){
				
				count();
			}
			$(".p_zh_navul>li>h4>span").removeClass('clicked');
			$(this).addClass("clicked");
			$('.p_zh_navul ul').slideUp();
			$nextUl.slideDown();
		}else{
			$(this).removeClass("clicked");
			$nextUl.slideUp();
		}
	});
	$(".p_zh_navul>li h4 a").click(function(){
		var $nextUl=$(this).parent().next("ul");
		if($nextUl.is(":hidden")){
			$(this).siblings("span").trigger("click");
		}
	})
});


// 进度条
$(function(){
	$(".probar50").map(function(){
	    var num=$(this).attr("probar"),
	        timeid;
	    var str="probar50_",
	        i=0;
	        var that=$(this);
	    var offsetTop=$(this).offset().top-550,
	    	fn;
	    that.attr("class","probar50 "+str+num);
	    $(window).bind("scroll",fn=function(){
			if($(window).scrollTop()>offsetTop){
				$(window).unbind("scroll",fn)
				    timeid=setInterval(function(){
				    if(i<=num){
				        that.attr("class","probar50 "+str+i);
				        that.text(i+"%");
				        i++;
				    }else{
				        clearInterval(timeid);
				    }
				},10);
			}
		});
	});
})














