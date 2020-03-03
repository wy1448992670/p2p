/*center*/
$(function(){
	var h=$(window).height()-188;
	var hw=$(window).height();
	var hcs=$(window).height()-358;
	$(".y_center").css("min-height",h);
	$(".y_allbox").css("min-height",hw);
	$(".y_bdmain").css("min-height",hcs);
	if ($(".y_center").height()<276){
		$(".y_center").height(300);
	}
})








