	 












$(function() {
/* 注 pageIndex为全局变量 进页面为1 点击按钮会有相应的变化,供发请求传递当前页码数*/
				$(".page span").eq(0).click();
				if(pageIndex == 1){
					$(".page button").eq(0).attr("disabled","true");
					$(".page button").eq(1).attr("disabled","true");
				}else{
					$(".page button").eq(0).removeAttr("disabled");
					$(".page button").eq(1).removeAttr("disabled");
				}
			});
			
			var pageIndex=1;
			var pageSize=5;
			var PageMax = 10; //最大页数 这个可以根据后台返回的数据量计算出来最大页数
//			                                          假如你设定每页显示20条数据,返回数据data为305条数据,那么
//			                     PageMax = 305/20=15.25=16
			$(".page span").bind("click",function() { //绑定页码数的点击事件
				if($(".page span").hasClass("active")){
					
					
					
					
					
				
					
					$.ajax({
            url: "chart.json",
            type: "get",
            contentType: "application/json",
//          data: { "ProductJsonData": l_ProductID }, //发送给服务器的数据
            dataType: "json",
            cache: false,
            success: function (data) {
            	if (data != undefined && data != null) {
					var str ="";
                    for (var i = (pageIndex-1)*pageSize; i <(pageIndex)*pageSize; i++) {


                    
                        str+= '<div class="line_left">';

	                    str += '<img class="icon_left" src="'+data[i].icon+'" />';



                        str += '</div>';
                        str +='<div class="line_right">';
                        		str += '<p>' +data[i].content +'</p>';
                        		str +='<p><span>'+data[i].chart_name+'</span><span>'+data[i].chart_time+'</span>';
                        str +='</div>';

                    }

                    $(".con").html(str);

                }
            },
            error: function () {

                alert("出错了");
            }
        });
					
					
					
					
					
					
					
					
					
				};
				if(pageIndex == 1){
					$(".page button").eq(0).attr("disabled","true");
					$(".page button").eq(1).attr("disabled","true");
				}else{
					$(".page button").eq(0).removeAttr("disabled");
					$(".page button").eq(1).removeAttr("disabled");
				}
				if(pageIndex == PageMax){
					$(".page button").eq(3).attr("disabled","true");
					$(".page button").eq(2).attr("disabled","true");
				}else{
					$(".page button").eq(3).removeAttr("disabled");
					$(".page button").eq(2).removeAttr("disabled");
				}

		
	
		
		
		
		
		
		
		








			});
			$(".page span").hover(function() { //页码数的样式效果
				if($(this).attr("class") == "active"){
					
				}else{
					$(this).attr("class","activeE");
				}
			},function() {
				if($(this).attr("class") == "active"){
					
				}else{
					$(this).attr("class","activeF");
				}
			});
			$(".page button").bind("click",function() { //首页 上一页 尾页 下一页的点击事件
				
				
				
				$.ajax({
            url: "chart.json",
            type: "get",
            contentType: "application/json",
//          data: { "ProductJsonData": l_ProductID }, //发送给服务器的数据
            dataType: "json",
            cache: false,
            success: function (data) {
            	if (data != undefined && data != null) {
//                  var st = "";
//                  st = '<p style=" font-weight:bold">产品名称：' + data[0].ProductName + '</p>';
//                  $(".ibox-title").append(st);
                    for (var i = 0; i <data.length; i++) {
//                  	alert(data[i].icon+"======="+data[i].content+"====="+data[i].chart_name+"========"+data[i].chart_time);
//                  	
                    	var str ="";
                        str = '<div class="line_left">';
	//                      str += '<a  href="../Upload/ProductPicture/' + data[i].Picture + '" >';
	                        str += '<img class="icon_left" src="'+data[i].icon+'" />';
	//                      str += '</a>';
//	                        str += ' <div class="leftborder ProductSet" id="' + data[i].Picture + '>主图</div>';
//	                        str += ' <div class="rightborder ProductDel">删除</div>';
                        str += '</div>';
                        str +='<div class="line_right">';
                        		str += '<p>' +data[i].content +'</p>';
                        		str +='<p><span>'+data[i].chart_name+'</span><span>'+data[i].chart_time+'</span>';
                        str +='</div>';
                       $(".con").append(str);
                    }
//                  $(".ibox-content").append('<div class="clear"></div>');
                }
            },
            error: function () {
                alert("出错了");
            }
        });
				
				
				
				
				
				
				
				
				
//				$(".con").html(pageIndex);//显示框显示的内容，根据需求变化
				if(pageIndex == 1){
					$(".page button").eq(0).attr("disabled","true");
					$(".page button").eq(1).attr("disabled","true");
				}else{
					$(".page button").eq(0).removeAttr("disabled");
					$(".page button").eq(1).removeAttr("disabled");
				}
				if(pageIndex == PageMax){
					$(".page button").eq(3).attr("disabled","true");
					$(".page button").eq(2).attr("disabled","true");
				}else{
					$(".page button").eq(3).removeAttr("disabled");
					$(".page button").eq(2).removeAttr("disabled");
				}
			});
			//---开始--- 页面数的点击事件
			$(".page span").eq(0).click(function() {
				if(pageIndex > 2){
					$(".page span").eq(2).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+2);
					$(".page span").eq(3).html(parseInt(pageIndex)+1);
					$(".page span").eq(2).html(parseInt(pageIndex));
					$(".page span").eq(1).html(parseInt(pageIndex)-1);
					$(".page span").eq(0).html(parseInt(pageIndex)-2);
				}else if(pageIndex == 2){
					$(".page span").eq(0).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+3);
					$(".page span").eq(3).html(parseInt(pageIndex)+2);
					$(".page span").eq(2).html(parseInt(pageIndex)+1);
					$(".page span").eq(1).html(parseInt(pageIndex));
					$(".page span").eq(0).html(parseInt(pageIndex)-1);
				}else if(pageIndex == 1){
					$(".page span").eq(0).attr("class","active").siblings().removeClass("active");
				};
			})
			$(".page span").eq(1).click(function() {
				if(pageIndex > 2){
					$(".page span").eq(2).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+2);
					$(".page span").eq(3).html(parseInt(pageIndex)+1);
					$(".page span").eq(2).html(parseInt(pageIndex));
					$(".page span").eq(1).html(parseInt(pageIndex)-1);
					$(".page span").eq(0).html(parseInt(pageIndex)-2);
				}else if(pageIndex == 2){
					$(".page span").eq(1).attr("class","active").siblings().removeClass("active");
				};
			});
			$(".page span").eq(2).click(function() {
				if(pageIndex > 2){
					$(".page span").eq(2).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+2);
					$(".page span").eq(3).html(parseInt(pageIndex)+1);
					$(".page span").eq(2).html(parseInt(pageIndex));
					$(".page span").eq(1).html(parseInt(pageIndex)-1);
					$(".page span").eq(0).html(parseInt(pageIndex)-2);
				};
			});
			$(".page span").eq(3).click(function() {
				if(pageIndex > 3){
					$(".page span").eq(2).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+2);
					$(".page span").eq(3).html(parseInt(pageIndex)+1);
					$(".page span").eq(2).html(parseInt(pageIndex));
					$(".page span").eq(1).html(parseInt(pageIndex)-1);
					$(".page span").eq(0).html(parseInt(pageIndex)-2);
				}
				if(pageIndex == PageMax-1){
					$(".page span").eq(3).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+1);
					$(".page span").eq(3).html(parseInt(pageIndex));
					$(".page span").eq(2).html(parseInt(pageIndex)-1);
					$(".page span").eq(1).html(parseInt(pageIndex)-2);
					$(".page span").eq(0).html(parseInt(pageIndex)-3);
				}
			});
			$(".page span").eq(4).click(function() {
				if(pageIndex > 4){
					$(".page span").eq(2).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+2);
					$(".page span").eq(3).html(parseInt(pageIndex)+1);
					$(".page span").eq(2).html(parseInt(pageIndex));
					$(".page span").eq(1).html(parseInt(pageIndex)-1);
					$(".page span").eq(0).html(parseInt(pageIndex)-2);
				}
				if(pageIndex == PageMax){
					$(".page span").eq(4).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex));
					$(".page span").eq(3).html(parseInt(pageIndex)-1);
					$(".page span").eq(2).html(parseInt(pageIndex)-2);
					$(".page span").eq(1).html(parseInt(pageIndex)-3);
					$(".page span").eq(0).html(parseInt(pageIndex)-4);
				}
			});
			//---结束---
			
			
			function Pre() {  //上一页的点击事件
				if(pageIndex == 1){
					$(".page span").eq(0).attr("class","active").siblings().removeClass("active");
					pageIndex = 1;
					$(".homepage").attr("disabled");
					$(".prebtn").attr("disabled");
				}
				if(pageIndex == 2){
					$(".page span").eq(0).attr("class","active").siblings().removeClass("active");
					pageIndex--;
				}
				if(pageIndex == 3){
					$(".page span").eq(1).attr("class","active").siblings().removeClass("active");
					pageIndex--;
				}
				if(3 < pageIndex && pageIndex < PageMax-2){
					$(".page span").eq(2).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+1);
					$(".page span").eq(3).html(parseInt(pageIndex));
					$(".page span").eq(2).html(parseInt(pageIndex)-1);
					$(".page span").eq(1).html(parseInt(pageIndex)-2);
					$(".page span").eq(0).html(parseInt(pageIndex)-3);
					pageIndex--;
					return;
				}
				if(pageIndex == PageMax-2){
					$(".page span").eq(2).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+1);
					$(".page span").eq(3).html(parseInt(pageIndex));
					$(".page span").eq(2).html(parseInt(pageIndex)-1);
					$(".page span").eq(1).html(parseInt(pageIndex)-2);
					$(".page span").eq(0).html(parseInt(pageIndex)-3);
					pageIndex--;
					return;
				}
				if(pageIndex == parseInt(PageMax)-1){
					$(".page span").eq(2).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+1);
					$(".page span").eq(3).html(parseInt(pageIndex));
					$(".page span").eq(2).html(parseInt(pageIndex)-1);
					$(".page span").eq(1).html(parseInt(pageIndex)-2);
					$(".page span").eq(0).html(parseInt(pageIndex)-3);
					pageIndex--;
					return;
				}
				if(pageIndex == parseInt(PageMax)){
					$(".page span").eq(3).attr("class","active").siblings().removeClass("active");
					pageIndex--;
				}
			};
			function Next() {  //下一页的点击事件
				if(pageIndex == 1){
					$(".page span").eq(1).attr("class","active").siblings().removeClass("active");
					pageIndex++;
					return;
				}
				if(pageIndex == 2){
					$(".page span").eq(2).attr("class","active").siblings().removeClass("active");
					pageIndex++;
					return;
				}
				if(3 <= pageIndex && pageIndex <= PageMax-3){
					$(".page span").eq(2).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+3);
					$(".page span").eq(3).html(parseInt(pageIndex)+2);
					$(".page span").eq(2).html(parseInt(pageIndex)+1);
					$(".page span").eq(1).html(parseInt(pageIndex));
					$(".page span").eq(0).html(parseInt(pageIndex)-1);
					pageIndex++;
					return;
				}
				if(pageIndex == PageMax-2){
					$(".page span").eq(3).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+2);
					$(".page span").eq(3).html(parseInt(pageIndex)+1);
					$(".page span").eq(2).html(parseInt(pageIndex));
					$(".page span").eq(1).html(parseInt(pageIndex)-1);
					$(".page span").eq(0).html(parseInt(pageIndex)-2);
					pageIndex++;
					return;
				}
				if(pageIndex == PageMax-1){
					$(".page span").eq(4).attr("class","active").siblings().removeClass("active");
					$(".page span").eq(4).html(parseInt(pageIndex)+1);
					$(".page span").eq(3).html(parseInt(pageIndex));
					$(".page span").eq(2).html(parseInt(pageIndex)-1);
					$(".page span").eq(1).html(parseInt(pageIndex)-2);
					$(".page span").eq(0).html(parseInt(pageIndex)-3);
					pageIndex++;
					return;
				}
				if(pageIndex == PageMax){
					pageIndex = PageMax;
					 $(".nextbtn").attr("disabled");
					 $(".lastpage").attr("disabled");
				}
			};
			function HomePage() { //首页的点击事件
				pageIndex = 1;
//				$(".con").html(pageIndex);

			$.ajax({
            url: "chart.json",
            type: "get",
            contentType: "application/json",
//          data: { "ProductJsonData": l_ProductID }, //发送给服务器的数据
            dataType: "json",
            cache: false,
            success: function (data) {
            	if (data != undefined && data != null) {
//                  var st = "";
//                  st = '<p style=" font-weight:bold">产品名称：' + data[0].ProductName + '</p>';
//                  $(".ibox-title").append(st);
                    for (var i = 0; i <data.length; i++) {
//                  	alert(data[i].icon+"======="+data[i].content+"====="+data[i].chart_name+"========"+data[i].chart_time);
//                  	
                    	var str ="";
                        str = '<div class="line_left">';
	//                      str += '<a  href="../Upload/ProductPicture/' + data[i].Picture + '" >';
	                        str += '<img class="icon_left" src="'+data[i].icon+'" />';
	//                      str += '</a>';
//	                        str += ' <div class="leftborder ProductSet" id="' + data[i].Picture + '>主图</div>';
//	                        str += ' <div class="rightborder ProductDel">删除</div>';
                        str += '</div>';
                        str +='<div class="line_right">';
                        		str += '<p>' +data[i].content +'</p>';
                        		str +='<p><span>'+data[i].chart_name+'</span><span>'+data[i].chart_time+'</span>';
                        str +='</div>';
                       $(".con").append(str);
                    }
//                  $(".ibox-content").append('<div class="clear"></div>');
                }
            },
            error: function () {
                alert("出错了");
            }
        });
				
				
				
				
				
				
				
				
				
				
				
				
				$(".page span").eq(0).attr("class","active").siblings().removeClass("active");
				$(".page span").eq(4).html(parseInt(pageIndex)+4);
				$(".page span").eq(3).html(parseInt(pageIndex)+3);
				$(".page span").eq(2).html(parseInt(pageIndex)+2);
				$(".page span").eq(1).html(parseInt(pageIndex)+1);
				$(".page span").eq(0).html(parseInt(pageIndex));
			}
			function LastPage() { //尾页的点击事件
				pageIndex = PageMax;
				$(".page span").eq(4).attr("class","active").siblings().removeClass("active");
				$(".page span").eq(4).html(parseInt(pageIndex));
				$(".page span").eq(3).html(parseInt(pageIndex)-1);
				$(".page span").eq(2).html(parseInt(pageIndex)-2);
				$(".page span").eq(1).html(parseInt(pageIndex)-3);
				$(".page span").eq(0).html(parseInt(pageIndex)-4);
			}