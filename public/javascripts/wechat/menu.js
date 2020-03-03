//checkbox模拟
$(function(){
	$(".s_checkbox").click(function(event){
		if(!$(this).attr("checked")){
			$(this).attr("checked","checked");
		}else{
			$(this).removeAttr("checked");
		}
		event.stopPropagation();
	})
});
//radio模拟
$(function(){
	$(".s_radio").map(function(){
		$(this).click(function(){
			var name = $(this).attr("myname");
			var len = $(".s_radio").length;
			for(var i=0; i<len; i++){
				if($(".s_radio").eq(i).attr("myname") == name){
					$(".s_radio").eq(i).removeAttr("checked");
				}
			}
			if(!$(this).attr("checked")){
				$(this).attr("checked","checked");
			}else{
				$(this).removeAttr("checked");
			}
		});
	});
});
//模拟select
$(function(){
	$(".s_select").tap(function(event){
		var select=$(this),
			option=$(this).children("ul").children("li"),
			val=$(this).children("span");
		if($(this).hasClass("clicked")){
			$(this).removeClass("clicked").children("ul").hide();
		}else{
			$(this).addClass("clicked").children("ul").show();

			option.tap(function(){
				val.text($(this).text());
			});

			$("body").tap(function(){
				select.removeClass("clicked").children("ul").hide();
			});
		}
		event.stopPropagation();
	});
});
//banner
$(function(){
	var $banner=$(".s_banner"),
		$bannerUl=$banner.find("ul.s_slide"),
		$bannerLi=$bannerUl.find("li"),
		len=$bannerLi.length,
		width=$banner.width(),
		i=0,
		timeid,
		$control=$(".s_control"),
		$controlLi=$control.find("li");

	$bannerLi.width(width);
	$bannerUl.width(width*len);
	$(window).resize(function(){
		width=$banner.width();
		$bannerLi.width(width);
		$bannerUl.width(width*len);
		
	});
	function scrollLeft(){
		if(i<len-1){
			$bannerUl.animate({left:-width*(i+1)},300);
			i++;
		}else{
			$bannerUl.animate({left:0},150);
			i=0;
		}
		$controlLi.eq(i).addClass("tap").siblings().removeClass("tap");
	}
	function scrollRight(){
		if(i>0){
			$bannerUl.animate({left:-width*(i-1)},300);
			i--;
		}else{
			$bannerUl.animate({left:-width*(len-1)},150);
			i=len-1;
		}
		$controlLi.eq(i).addClass("tap").siblings().removeClass("tap");
	}
	$controlLi.tap(function(){
		var index=$(this).index();
		$bannerUl.animate({left:-width*index},300);
		$(this).addClass("tap").siblings().removeClass("tap");
		i=index;
	})
	function scrollStart(){
		timeid=setInterval(scrollLeft,3000);
	}
	function scrollStop(){
		clearInterval(timeid);
	}
	scrollStart();
	var startX,startY,moveX=0,moveY,scrollLeft;
	$banner.bind("touchstart",function(event){
		startX=event.touches[0].screenX;
		scrollStop();
	}).bind("touchend",function(event){
		scrollStart();
	}).swipeLeft(function(){
		scrollLeft();
	}).swipeRight(function(){
		scrollRight();
	});
});

//文本框focus效果
$(function(){
	$(".s_bindinput").map(function(){
		var input=$(this).children("input");
		input.focus(function(){
			$(this).addClass("focus").siblings().addClass("focus");
		}).blur(function(){
			$(this).removeClass("focus").siblings().removeClass("focus");
		});
	});

	$(".s_rembermsg a").tap(function(){
		$(this).children(".s_checkbox").trigger("tap");
	});
	
	$(".s_bindinput .arrow").tap(function(event){
		var arrow=$(this);
		if($(".s_usernames").is(":hidden")){
			$(this).addClass("open");
			$(".s_usernames").show();
			$(".s_usernames li").tap(function(event){
				$(".s_usernames").siblings("input").val($(this).text());
				$(".s_usernames").hide();
				arrow.removeClass("open");
				event.stopPropagation();
			});
		}else{
			$(this).removeClass("open");
			$(".s_usernames").hide();
		}
		event.stopPropagation();
	});
	$("body").tap(function(){
		$(".s_bindinput .arrow").removeClass("open");
		$(".s_usernames").hide();
	});
});

//排序
$(function(){
	$(".s_order li").tap(function(){
		if(!$(this).hasClass("clicked")){
			$(this).addClass("clicked").siblings().removeClass("clicked").removeClass("dbclicked");
		}else if($(this).hasClass("dbclicked")){
			$(this).removeClass("dbclicked");
		}else{
			$(this).addClass("dbclicked");
		}
	});
});

//详情页面展开关闭
$(function(){
	$(".s_biaodetail").map(function(){
		var title=$(this).children("h3"),
			cont=$(this).children(".s_biaodetaimain,#wrapper");
		title.click(function(){
			
			if(cont.is(":hidden")){
				title.addClass("clicked");
				cont.show();
				$(this).parent().siblings(".s_biaodetail").children("h3.clicked").removeClass("clicked").siblings(".s_biaodetaimain,#wrapper").hide();
			}else{
				title.removeClass("clicked");
				cont.hide();
			}
		});
	});
});


//上传认证资料
$(function(){

	$(".s_choose").tap(function(event){
		var $box=$(".s_choosebox");
		if($box.is(":hidden")){
			$(this).addClass("clicked");
			$box.show();
		}else{
			$(this).removeClass("clicked");
			$box.hide();
		}
		event.stopPropagation();
	});

	$(".s_choosebox").tap(function(event){
		event.stopPropagation();
	});

	$("body").tap(function(){
		$(".s_choose").removeClass("clicked");
		$(".s_choosebox").hide();
	});

	$(".s_chooseradio>li").tap(function(){
		$(this).find(".s_radio").attr("checked","checked");
		$(this).siblings().find(".s_radio").removeAttr("checked");
	});

	$(".s_borrownote li").tap(function(){
		$(this).addClass("clicked").siblings().removeClass("clicked");
	});
});
$(function(){
	$(".s_openbtn").tap(function(){
		$(".s_fixedbg").show();
		var height=$(".s_fixedbox").height();
		$(".s_fixedbox").css("margin-top",-height/2+"px");
	});
	$(".s_closebtn").tap(function(){
		$(this).parent().parent().hide();
	});
	$(".s_zrcard li").tap(function(){
		$(this).addClass("clicked").siblings().removeClass("clicked");
		$(".s_zrtab").hide().eq($(this).index()).show();
	});
	/*
	$(".s_set h3 .tabr").tap(function(){
		var next=$(this).parent().next("ul");
		if(next.is(":hidden")){
			next.show();
		}else{
			next.hide();
		}
	});
	*/
});


/*02-28*/
$(function(){
	$(".s_borrowstate h4").click(function(event){
		var ul=$(this).siblings("ul"),
			span=$(this).children("span");
		if(ul.is(":hidden")){
			ul.show();
			ul.children("li").click(function(){
				span.text($(this).text());
				$(this).parent().hide();
			});
		}else{
			ul.hide();
		}
		event.stopPropagation();
	});
	$("body").click(function(){
		$(".s_borrowstate ul").hide();
	});
});


