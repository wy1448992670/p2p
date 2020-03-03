// *根据屏幕尺寸改变根元素字体
(function(doc, win) {
	var docEl = doc.documentElement,
		resizeEvt = 'orientationchange' in window ? 'orientationchange' : 'resize',
		recalc = function() {
			var clientWidth = docEl.clientWidth >= 1024 ? 540 : docEl.clientWidth;
			if (!clientWidth) return;
			docEl.style.fontSize = 10 * (clientWidth / 320) + 'px';
		};

	if (!doc.addEventListener) return;
	win.addEventListener(resizeEvt, recalc, false);
	doc.addEventListener('DOMContentLoaded', recalc, false);
})(document, window);

// 通用选项卡
function tabclick(li,tabox,name){
	li.eq(0).addClass(name);
	tabox.eq(0).show();
	li.click(function(){
		$(this).addClass(name).siblings().removeClass(name);
		tabox.hide().eq(li.index(this)).show();
	})
}

//弹窗居中
function dialogCenter(dialog){
	var $dialog = $(dialog);
		$cont = $dialog.children('.p-dialogcont'),
		$close = $dialog.find('.p-dialogbg,.p-dialogclose,.p-dialogconsole');
	$dialog.show();
	var w = $cont.width(),
		h = $cont.height();
	$cont.css({
		'left': '50%',
		'margin-left': -w/2,
		'top': '48%',
		'margin-top': -h/2
	});
	var timeid = setInterval(function(){
		var width = $cont.width(),
			height = $cont.height();
		if($dialog.is(':hidden')){
			clearInterval(timeid);
		}
		if(w != width || h != height){
			$cont.css({
				
			});
			w = width;
			h = height;
		}
	},10);
	$close.click(function(){
		$dialog.hide();
		clearInterval(timeid);
	});
}

$(function(){
	tabclick($('.p-xtxxkul li'),$('.p-xtabox'),'cur');
	$('.p-botshouc').click(function(){
		$(this).toggleClass('cur');
	});
	$('.p-botfatie').click(function(){
		$('.p-tzhuifutc').show();
	});
	$('.p-tzhuifutc').click(function(){
		$(this).hide();
	});
	$('.p-tzhfwrite').click(function(event) {
		event.stopPropagation();
	});
	$('.p-gomoradd').click(function() {
		$(this).toggleClass('cur');
	});
	$('.p-physical li:nth-child(2n)').css('border-right','none');
	// 20160218 new
	$('.p-fseleor li').click(function() {
		$(this).addClass('cur').siblings().removeClass('cur');
	});
})





























