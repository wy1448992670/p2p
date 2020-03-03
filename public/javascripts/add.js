/*选项卡*/
$(function(){


var $menu=$("#menu1");
sTab($menu);


var $menu2=$("#menu2");
sTab($menu2);

// var $menu3=$("#menu3");
// sTab($menu3);

  function sTab($menu){
        var $menuDiv=$menu.siblings();
       var $mLi=$menu.find("li");
       var $mDiv=$menuDiv.children("div");
       var mLen=$mLi.length;
       var curr=0;
       
       //初始化
       $mLi.eq(0).addClass("off");
       $mDiv.css("display","none");
       $mDiv.eq(0).css("display","block");

       //点击
       $mLi.on("click",function(){
          curr=$(this).index();
          $mLi.eq(curr).addClass("off").siblings().removeClass("off");
          $mDiv.eq(curr).css("display","block").siblings().css("display","none");
       })   
  }

});


/*end_选项卡*/

/*yysj_bot*/
$(function(){
  var Main  = $(".yysj_bot_cn");
  var Mul  = Main.children("ul");
  var Mli  = Mul.children("li");
    Mli.each(function(){
      var curr_li = "yysj_bot_" + ($(this).index() + 1);
      $(this).addClass(curr_li);
    });

});
/*end_yysj_bot*/