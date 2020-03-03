var myScroll,
pullUpEl, pullUpOffset;

loaded = function(pullUp, wrapper, div, s_onloading, pullUpAction) {
	pullUpEl = document.getElementById(pullUp);
	pullUpOffset = pullUpEl.offsetHeight;
	
	myScroll = new iScroll(wrapper, {
		scrollbarClass: '',
		/* 重要样式  myScrollbar*/
		useTransition: false,
		/* 此属性不知用意，本人从true改为false */
		//topOffset: pullDownOffset,
		onRefresh: function () {
				if (pullUpEl.className.match('loading')) {
					pullUpEl.className = '';
					var $s = $(pullUpEl);
					$s.children(div).removeClass(s_onloading);
				}
			},
			onScrollMove: function () {
				if (this.y < (this.maxScrollY - 5) && !pullUpEl.className.match('flip')) {
					pullUpEl.className = 'flip';
					this.maxScrollY = this.maxScrollY;
				} else if (this.y > (this.maxScrollY + 5) && pullUpEl.className.match('flip')) {
					pullUpEl.className = '';
					this.maxScrollY = pullUpOffset;
					//this.maxScrollY = this.maxScrollY;
				}
			},
			onScrollEnd: function () {
				if (pullUpEl.className.match('flip')) {
					pullUpEl.className = 'loading';
					var $s = $(pullUpEl);
					$s.children(div).html("").addClass(s_onloading);
					pullUpAction; // Execute custom function (ajax call?)
				}
			}
	});

	setTimeout(function () {
		document.getElementById(wrapper).style.left = '0';
	}, 800);
}
