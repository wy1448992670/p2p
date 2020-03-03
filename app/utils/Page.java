package utils;

import java.io.Serializable;
import org.apache.commons.lang.StringUtils;
import constants.Constants;
import play.mvc.Http.Request;

/**
 * 分页标签辅助类
 * @author think
 *
 */
/**
 * @author Administrator
 *
 */
public class Page  implements Serializable{

	private static final long serialVersionUID = 1L;
	/**
	 * 当前页
	 */
	public int currPage;
	/**
	 * 总记录数
	 */
	public int totalCount;
	/**
	 * 总页数
	 */
	public int totalPageCount;
	/**
	 * 每页的记录条数
	 */
	public int pageSize;
	/**
	 * 显示分页的页码数
	 */
	public int showPageCount = Constants.TEN;
	/**
	 * 显示的统计标题
	 */
	public String pageTitle = "";
	/**
	 * 风格
	 */
	public int style = 1;
	/**
	 * 页面显示的js方法
	 */
	public String funMethod;
	/**
	 * 页面显示的js方法
	 */
	private String defaultMethod = "showPage";
	/**
	 * 查询条件
	 */
	public String conditions;
	/**
	 * 接口Action
	 */
	public String action;
	
	public int getTotalPageCount () {
		return (this.totalCount - 1) / this.pageSize + 1;
	}
	
	/**
	 * 进行分页块的显示
	 * @return
	 */
	public String getThemeNumber() {
		//return getDefaultTheme();
		switch (style) {
		case 2:
			return getForeGroundTheme();
		case 3:
			return getMixedTheme();
		case 4:
			return getBackstageTheme();
		default:
			return getDefaultTheme();
		}
	}

	/**
	 * js
	 * 数字显示
	 * @return
	 */
	public String getThemeNumberScript(){
		//return getDefaultThemeScript();
		switch (style) {
		case 2:
			return getForeGroundThemeScript();
		case 3:
			return getMixedThemeScript();
		case 4:
			return getBackstageThemeScript();
		case 5:
			return getAppWapScript();
		default:
			return getDefaultThemeScript();
		}
	}
	
	private String getMixedTheme(){
		StringBuffer strBuff = new StringBuffer("<div class=\"xf_wylc_page xf_cfzx_page\">");
		String myUrl = Request.current().actionMethod;
		if(totalPageCount == 1) {
			strBuff.append("<span class=\"xf_wylc_page_first\">共计<span class=\"xf_wylc_page_firsthot\">"+totalCount+"</span>"+pageTitle+"(共"+totalPageCount+"页）</span></div>");
			return strBuff.toString();
		}
		
		if(currPage <= 1) {
			currPage = 1;
		}
		
		if(currPage >= totalPageCount) {
			currPage = totalPageCount;
		}
		
		int nextPage = currPage+1 > totalPageCount ? totalPageCount : currPage + 1;
		String dwonPage = myUrl + "?currPage=" + nextPage + "&pageSize=" + pageSize+"&"+conditions;
		
		
		int lastPage = currPage - 1 < 1 ? 1  :currPage - 1;
		String upPage = myUrl +"?currPage=" + lastPage + "&pageSize=" + pageSize+"&"+conditions;
		strBuff.append("<span class=\"xf_wylc_page_first\">共计<span class=\"xf_wylc_page_firsthot\">"+totalCount+"</span>"+pageTitle+"（共"+totalPageCount+"页）</span>");
		
		// ---------样式 2012-07-10 增加 begin-------------
		if(currPage != 1){
			strBuff.append("<a href=\""+upPage+"\"><span class=\"xf_wylc_page_prev\">&nbsp;</span></a>");
		}
		
		if(totalPageCount < showPageCount){
			showPageCount = totalPageCount;
		}
		
		if(showPageCount != 2){
			int i = 1,sum = 0;
			int half = showPageCount/2;
			if(currPage > half){
				i = currPage - half;
			}
			if(totalPageCount - i < showPageCount){//如果总页数减去起始位置小于 要显示的页码数
				i = totalPageCount - showPageCount + 1;
			}
			int[] inter = getRount(showPageCount,totalPageCount,currPage);
			for(i = inter[0]; i <=totalPageCount; i++ ){
				if(sum == showPageCount){
					if(totalPageCount > i){
						strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\">...</span>");
					}
					strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\"><a href=\""+myUrl+"?currPage="+totalPageCount+"&pageSize="+pageSize+"&"+conditions+ "\">"+totalPageCount+"</a></span>");
					break;
				}
				sum ++;
				if(currPage == i){
					strBuff.append("&nbsp;<span class=\"xf_wylc_page_ishot\"><a href=\""+myUrl+"?currPage="+i+"&pageSize="+pageSize+"&"+conditions+"\">"+i+"</a></span>");
				}else{
					strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\"><a href=\""+myUrl+"?currPage="+i+"&pageSize="+pageSize+"&"+conditions+"\">"+i+"</a></span>");
				}
			}
		}else{
			int tempCurrPage = currPage;
			if(currPage == 1){
				lastPage = 1;
				nextPage = 2;
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_ishot\"><a href=\""+myUrl+"?currPage="+lastPage+"&pageSize="+pageSize+"&"+conditions+"\">"+tempCurrPage+"</a></span>");
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\"><a href=\""+myUrl+"?currPage="+nextPage+"&pageSize="+pageSize+"&"+conditions+"\">"+(tempCurrPage+1)+"</a></span>");
			}else{
				if(currPage == totalPageCount){
					tempCurrPage = totalPageCount - 1;
				}
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\"><a href=\""+myUrl+"?currPage="+lastPage+"&pageSize="+pageSize+"&"+conditions+"\">"+tempCurrPage+"</a></span>");
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_ishot\"><a href=\""+myUrl+"?currPage="+nextPage+"&pageSize="+pageSize+"&"+conditions+"\">"+(tempCurrPage+1)+"</a></span>");
			}
		}
		if(currPage != totalPageCount){
			strBuff.append("&nbsp;<a href=\""+dwonPage+"\"><span class=\"xf_wylc_page_next\">下一页</span></a>");
		}
		//strBuff.append("<span class=\"xf_wylc_page_text\">到第</span><span class=\"xf_wylc_page_input\">");
		//strBuff.append("<input type=\"text\" id=\"goPage\" value=\""+currPage+"\"></span>");
		//strBuff.append("<span class=\"xf_wylc_page_text\">页</span><span class=\"xf_wylc_page_last\" id=\"jumpPage\" onclick=\"javascript:jumpPage("+pageSize+")\">确定</span></div>");
		strBuff.append("</div>");
		return strBuff.toString();
	}
	
	private String getMixedThemeScript(){
		//2012-12-18 增加判断，如果没有传函数，那就执行默认方法
		if(StringUtils.isNotBlank(funMethod)){
			defaultMethod = funMethod;
		}
		
		StringBuffer strBuff = new StringBuffer("<div class=\"xf_wylc_page xf_cfzx_page\">");
		//总页数 == 1
		if(totalPageCount == 1){
			strBuff.append("<span class=\"xf_wylc_page_first\">共计<span class=\"xf_wylc_page_firsthot\">"+totalCount+"</span>"+pageTitle+"（共"+totalPageCount+"页）</span></div>");
			return strBuff.toString();
		}
		
		//当前页小于等于第一页
		if(currPage <= 1){
			currPage = 1;
		}
		
		//当前页大于等于总页数
		if(currPage >= totalPageCount){
			currPage = totalPageCount;
		}
		int down = (currPage+1 > totalPageCount?totalPageCount:currPage+1);
		int up = (currPage-1 == 0?1:currPage-1);
		strBuff.append("<span class=\"xf_wylc_page_first\">共计<span class=\"xf_wylc_page_firsthot\">"+totalCount+"</span>"+pageTitle+"（共"+totalPageCount+"页）</span>");
		if(currPage != 1){
			strBuff.append("<span class=\"xf_wylc_page_prev\" onclick=\"javascript:"+defaultMethod+"("+up+","+pageSize+");\">&nbsp;</span>");
		}
		
		// ---------样式 2012-07-10 增加 begin-------------
		if(totalPageCount < showPageCount){
			showPageCount = totalPageCount;
		}
		
		if(showPageCount != 2){
			int i = 1,sum = 0;
			int half = showPageCount/2;
			if(currPage > half){
				i = currPage - half;
			}
			if(totalPageCount - i < showPageCount){//如果总页数减去起始位置小于 要显示的页码数
				i = totalPageCount - showPageCount + 1;
			}
			int[] inter = getRount(showPageCount,totalPageCount,currPage);
			for(i = inter[0]; i <=totalPageCount; i++ ){
				
				if(sum == showPageCount){
					if(totalPageCount > i){
						strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\">...</span>");
					}
					strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\" onclick=\"javascript:"+defaultMethod+"("+totalPageCount+","+pageSize+")\">"+totalPageCount+"</span>");
					break;
				}
				sum ++;
				if(currPage == i){
					strBuff.append("&nbsp;<span class=\"xf_wylc_page_ishot\" onclick=\"javascript:"+defaultMethod+"("+i+","+pageSize+")\">"+i+"</span>");
				}else{
					strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\" onclick=\"javascript:"+defaultMethod+"("+i+","+pageSize+")\">"+i+"</span>");
				}
			}
		}else{
			up = 1;down = 2;
			if(currPage == 1){
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_ishot\" onclick=\"javascript:"+defaultMethod+"("+up+","+pageSize+")\">"+up+"</span>");
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\" onclick=\"javascript:"+defaultMethod+"("+down+","+pageSize+")\">"+down+"</span>");
			}else{
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\" onclick=\"javascript:"+defaultMethod+"("+up+","+pageSize+")\">"+up+"</span>");
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_ishot\" onclick=\"javascript:"+defaultMethod+"("+down+","+pageSize+")\">"+down+"</span>");
			}
		}
		
		if(currPage != totalPageCount){
			strBuff.append("&nbsp;<span class=\"xf_wylc_page_next\" onclick=\"javascript:"+defaultMethod+"("+down+","+pageSize+")\"><a href=\""+down+"\">下一页</a></span>");
		}
		
		strBuff.append("<span class=\"xf_wylc_page_text\">到第</span><span class=\"xf_wylc_page_input\">");
		strBuff.append("<input type=\"text\" id=\"goPage\" value=\""+currPage+"\"></span>");
		strBuff.append("<span class=\"xf_wylc_page_text\">页</span><span class=\"xf_wylc_page_last\" id=\"jumpPage\" onclick=\"javascript:jumpPage("+pageSize+")\">确定</span></div>");
		
		String script = "<script type=\"text/javascript\"> function jumpPage(pageSize){"
				+ "var currPage = $(\"#goPage\").val();"
				+ "if(isNaN(currPage)){"
				+ "alert(\"输入格式不正确，请重新输入!\");"
				+ "return;"
				+ "}"
				+ "if(currPage > "+ this.totalPageCount + "){"
				+ "alert(\"输入页数大于当前最大页!\");"
				+ "$(\"#goPage\").val("+this.totalPageCount+");"
				+ "var currPage = "+ this.totalPageCount
				+ "}"
				+ defaultMethod
				+ "(currPage,pageSize);"
				+ "}</script>";
		strBuff.append(script);
		return strBuff.toString();
	}
	
	private String getForeGroundTheme(){
		StringBuffer strBuff = new StringBuffer("");
		//String myUrl = Request.current().actionMethod;
		String myUrl = StringUtils.isBlank(action)? Request.current().actionMethod: action;
		if(totalPageCount == 1) {
			strBuff.append("<span class=\"xf_wylc_page_first\">共"+totalPageCount+"页&nbsp;</span>");
			return strBuff.toString();
		}
		
		if(currPage <= 1) {
			currPage = 1;
		}
		
		if(currPage >= totalPageCount) {
			currPage = totalPageCount;
		}
		
		conditions = StringUtils.isBlank(conditions)? "": ("&"+conditions);
		int nextPage = currPage+1 > totalPageCount ? totalPageCount : currPage + 1;
		String dwonPage = myUrl + "?currPage=" + nextPage + "&pageSize=" + pageSize+conditions;
		
		
		int lastPage = currPage - 1 < 1 ? 1  :currPage - 1;
		String upPage = myUrl +"?currPage=" + lastPage + "&pageSize=" + pageSize+conditions;
		
		// ---------样式 2012-07-10 增加 begin-------------
		if(currPage != 1){
			strBuff.append("<a href=\""+upPage+"\"><span class=\"xf_wylc_page_prev\">&nbsp;</span></a>");
		}
		
		if(totalPageCount < showPageCount){
			showPageCount = totalPageCount;
		}
		
		if(showPageCount != 2){
			int i = 1,sum = 0;
			int half = showPageCount/2;
			if(currPage > half){
				i = currPage - half;
			}
			if(totalPageCount - i < showPageCount){//如果总页数减去起始位置小于 要显示的页码数
				i = totalPageCount - showPageCount + 1;
			}
			int[] inter = getRount(showPageCount,totalPageCount,currPage);
			for(i = inter[0]; i <=totalPageCount; i++ ){
				if(sum == showPageCount){
					if(totalPageCount > i){
						strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\">...</span>");
					}
					strBuff.append("&nbsp;<a href=\""+myUrl+"?currPage="+totalPageCount+"&pageSize="+pageSize+conditions+ "\"><span class=\"xf_wylc_page_rele\">"+totalPageCount+"</span></a>");
					break;
				}
				sum ++;
				if(currPage == i){
					strBuff.append("&nbsp;<a href=\""+myUrl+"?currPage="+i+"&pageSize="+pageSize+conditions+"\"><span class=\"xf_wylc_page_ishot\">"+i+"</span></a>");
				}else{
					strBuff.append("&nbsp;<a href=\""+myUrl+"?currPage="+i+"&pageSize="+pageSize+conditions+"\"><span class=\"xf_wylc_page_rele\">"+i+"</span></a>");
				}
			}
		}else{
			int tempCurrPage = currPage;
			if(currPage == 1){
				lastPage = 1;
				nextPage = 2;
				
				strBuff.append("&nbsp;<a href=\""+myUrl+"?currPage="+lastPage+"&pageSize="+pageSize+conditions+"\"><span class=\"xf_wylc_page_ishot\">"+tempCurrPage+"</span></a>");
				strBuff.append("&nbsp;<a href=\""+myUrl+"?currPage="+nextPage+"&pageSize="+pageSize+conditions+"\"><span class=\"xf_wylc_page_rele\">"+(tempCurrPage+1)+"</span></a>");
			}else{
				if(currPage == totalPageCount){
					tempCurrPage = totalPageCount - 1;
				}
				strBuff.append("&nbsp;<a href=\""+myUrl+"?currPage="+lastPage+"&pageSize="+pageSize+conditions+"\"><span class=\"xf_wylc_page_rele\">"+tempCurrPage+"</span></a>");
				strBuff.append("&nbsp;<a href=\""+myUrl+"?currPage="+nextPage+"&pageSize="+pageSize+conditions+"\"><span class=\"xf_wylc_page_ishot\">"+(tempCurrPage+1)+"</span></a>");
			}
		}
		if(currPage != totalPageCount){
			strBuff.append("&nbsp;<a href=\""+dwonPage+"\"><span class=\"xf_wylc_page_next\">下一页</span></a>");
		}
		return strBuff.toString();
	}
	
	private String getForeGroundThemeScript(){
		//2012-12-18 增加判断，如果没有传函数，那就执行默认方法
		if(StringUtils.isNotBlank(funMethod)){
			defaultMethod = funMethod;
		}
		
		StringBuffer strBuff = new StringBuffer("");
		//总页数 == 1
		if(totalPageCount == 1){
			strBuff.append("<span class=\"xf_wylc_page_first\">共"+totalPageCount+"页&nbsp;</span>");
			return strBuff.toString();
		}
		
		//当前页小于等于第一页
		if(currPage <= 1){
			currPage = 1;
		}
		
		//当前页大于等于总页数
		if(currPage >= totalPageCount){
			currPage = totalPageCount;
		}
		int down = (currPage+1 > totalPageCount?totalPageCount:currPage+1);
		int up = (currPage-1 == 0?1:currPage-1);
		if(currPage != 1){
			strBuff.append("<span class=\"xf_wylc_page_prev\" onclick=\"javascript:"+defaultMethod+"("+up+","+pageSize+");\"><a href=\"javascript:void(0);\">&nbsp;</a></span>");
		}
		
		// ---------样式 2012-07-10 增加 begin-------------
		if(totalPageCount < showPageCount){
			showPageCount = totalPageCount;
		}
		
		if(showPageCount != 2){
			int i = 1,sum = 0;
			int half = showPageCount/2;
			if(currPage > half){
				i = currPage - half;
			}
			if(totalPageCount - i < showPageCount){//如果总页数减去起始位置小于 要显示的页码数
				i = totalPageCount - showPageCount + 1;
			}
			int[] inter = getRount(showPageCount,totalPageCount,currPage);
			for(i = inter[0]; i <=totalPageCount; i++ ){
				
				if(sum == showPageCount){
					if(totalPageCount > i){
						strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\">...</span>");
					}
					strBuff.append("&nbsp;<span class=\"xf_wylc_page_ishot\" onclick=\"javascript:"+defaultMethod+"("+totalPageCount+","+pageSize+")\"><a href=\"javascript:void(0);\">"+totalPageCount+"</a></span>");
					break;
				}
				sum ++;
				if(currPage == i){
					strBuff.append("&nbsp;<span class=\"xf_wylc_page_ishot\" onclick=\"javascript:"+defaultMethod+"("+i+","+pageSize+")\"><a href=\"javascript:void(0);\">"+i+"</a></span>");
				}else{
					strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\" onclick=\"javascript:"+defaultMethod+"("+i+","+pageSize+")\"><a href=\"javascript:void(0);\">"+i+"</a></span>");
				}
			}
		}else{
			up = 1;down = 2;
			if(currPage == 1){
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_ishot\" onclick=\"javascript:"+defaultMethod+"("+up+","+pageSize+")\">"+up+"</span>");
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\" onclick=\"javascript:"+defaultMethod+"("+down+","+pageSize+")\">"+down+"</span>");
			}else{
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_rele\" onclick=\"javascript:"+defaultMethod+"("+up+","+pageSize+")\">"+up+"</span>");
				strBuff.append("&nbsp;<span class=\"xf_wylc_page_ishot\" onclick=\"javascript:"+defaultMethod+"("+down+","+pageSize+")\">"+down+"</span>");
			}
		}
		
		if(currPage != totalPageCount){
			strBuff.append("&nbsp;<span class=\"xf_wylc_page_next\" onclick=\"javascript:"+defaultMethod+"("+down+","+pageSize+")\"><a href=\""+down+"\">下一页</a></span>");
		}
		
		String script = "<script type=\"text/javascript\"> function jumpPage(pageSize){"
				+ "var currPage = $(\"#goPage\").val();"
				+ "if(isNaN(currPage)){"
				+ "alert(\"输入格式不正确，请重新输入!\");"
				+ "return;"
				+ "}"
				+ "if(currPage > "+ this.totalPageCount + "){"
				+ "alert(\"输入页数大于当前最大页!\");"
				+ "$(\"#goPage\").val("+this.totalPageCount+");"
				+ "var currPage = "+ this.totalPageCount
				+ "}"
				+ defaultMethod
				+ "(currPage,pageSize);"
				+ "}</script>";
		strBuff.append(script);
		return strBuff.toString();
	}
	
	private String getAppWapScript(){
		//2012-12-18 增加判断，如果没有传函数，那就执行默认方法
		if(StringUtils.isNotBlank(funMethod)){
			defaultMethod = funMethod;
		}
		
		StringBuffer strBuff = new StringBuffer("");
		//总页数 == 1
		if(totalPageCount == 1){
			strBuff.append("<a href=\"javascript:void(0);\" title=\"\" target=\"\" class=\"on\">1</a>");
			return strBuff.toString();
		}
		
		//当前页小于等于第一页
		if(currPage <= 1){
			currPage = 1;
		}
		
		//当前页大于等于总页数
		if(currPage >= totalPageCount){
			currPage = totalPageCount;
		}
		int down = (currPage+1 > totalPageCount?totalPageCount:currPage+1);
		int up = (currPage-1 == 0?1:currPage-1);
		if(currPage != 1){
			strBuff.append("<a class=\"a1\" onclick=\"javascript:"+defaultMethod+"("+up+","+pageSize+");\">&lt;</a>");
		}
		
		// ---------样式 2012-07-10 增加 begin-------------
		if(totalPageCount < showPageCount){
			showPageCount = totalPageCount;
		}
		
		if(showPageCount != 2){
			int i = 1,sum = 0;
			int half = showPageCount/2;
			if(currPage > half){
				i = currPage - half;
			}
			if(totalPageCount - i < showPageCount){//如果总页数减去起始位置小于 要显示的页码数
				i = totalPageCount - showPageCount + 1;
			}
			int[] inter = getRount(showPageCount,totalPageCount,currPage);
			for(i = inter[0]; i <=totalPageCount; i++ ){
				
				if(sum == showPageCount){
					if(totalPageCount > i){
						strBuff.append("&nbsp;<a class=\"\">...</a>");
					}
					strBuff.append("&nbsp;<a class=\"on\" onclick=\"javascript:"+defaultMethod+"("+totalPageCount+","+pageSize+")\">"+totalPageCount+"</a>");
					break;
				}
				sum ++;
				if(currPage == i){
					strBuff.append("&nbsp;<a class=\"on\" onclick=\"javascript:"+defaultMethod+"("+i+","+pageSize+")\">"+i+"</a>");
				}else{
					strBuff.append("&nbsp;<a class=\"\" onclick=\"javascript:"+defaultMethod+"("+i+","+pageSize+")\">"+i+"</a>");
				}
			}
		}else{
			up = 1;down = 2;
			if(currPage == 1){
				strBuff.append("&nbsp;<a class=\"on\" onclick=\"javascript:"+defaultMethod+"("+up+","+pageSize+")\">"+up+"</a>");
				strBuff.append("&nbsp;<a class=\"\" onclick=\"javascript:"+defaultMethod+"("+down+","+pageSize+")\">"+down+"</a>");
			}else{
				strBuff.append("&nbsp;<a class=\"\" onclick=\"javascript:"+defaultMethod+"("+up+","+pageSize+")\">"+up+"</a>");
				strBuff.append("&nbsp;<a class=\"on\" onclick=\"javascript:"+defaultMethod+"("+down+","+pageSize+")\">"+down+"</a>");
			}
		}
		
		if(currPage != totalPageCount){
			strBuff.append("&nbsp;<a class=\"a1\" onclick=\"javascript:"+defaultMethod+"("+down+","+pageSize+")\">&gt;</a>");
		}
		
		String script = "<script type=\"text/javascript\"> function jumpPage(pageSize){"
				+ "var currPage = $(\"#goPage\").val();"
				+ "if(isNaN(currPage)){"
				+ "alert(\"输入格式不正确，请重新输入!\");"
				+ "return;"
				+ "}"
				+ "if(currPage > "+ this.totalPageCount + "){"
				+ "alert(\"输入页数大于当前最大页!\");"
				+ "$(\"#goPage\").val("+this.totalPageCount+");"
				+ "var currPage = "+ this.totalPageCount
				+ "}"
				+ defaultMethod
				+ "(currPage,pageSize);"
				+ "}</script>";
		strBuff.append(script);
		return strBuff.toString();
	}
	
	private String getBackstageTheme(){
		String myUrl = Request.current().actionMethod;

		//<div class="page_warp"> 
		StringBuffer strBuff = new StringBuffer("<div class=\"page_warp\">");  
		//总页数 == 1
		if(totalPageCount == 1){
			strBuff.append("共"+totalPageCount+"页&nbsp;</div>");
			return strBuff.toString();
		}
		
		//当前页小于等于第一页
		if(currPage <= 1){
			currPage = 1;
		}
		
		//当前页大于等于总页数
		if(currPage >= totalPageCount){
			currPage = totalPageCount;
		}
		
		//首页：<a href="javascript:void(0)" title="" target="">首页</a> 
		String firstPage = myUrl + "?currPage=1&pageSize=" + pageSize+"&"+conditions;
		strBuff.append("<a href=\"" + firstPage + "\" title=\"\" target=\"\">首页</a>");

		//上一页：<a href="javascript:void(0)" title="" target="">上一页</a> 
		if(currPage == 1){
			strBuff.append("<span class=\"c_color\">上一页</span>");
		}else{
			String upPage = myUrl + "?currPage=" + (currPage-1) + "&pageSize=" + pageSize+"&"+conditions;
			strBuff.append("<a href=\"" + upPage + "\" title=\"\" target=\"\">上一页</a>");
		}

		//下一页：<a href="javascript:void(0)" title="" target="">下一页</a> 
		if(currPage == totalPageCount){
			strBuff.append("<span class=\"c_color\">下一页</span>");
		}else{
			String dwonPage = myUrl + "?currPage=" + (currPage+1) + "&pageSize=" + pageSize+"&"+conditions;
			strBuff.append("<a href=\"" + dwonPage + "\" title=\"\" target=\"\">下一页</a>");
		}

		//末页：<a href="javascript:void(0)" title="" target="">末页</a> 
		String lastPage = myUrl + "?currPage=" + totalPageCount + "&pageSize=" + pageSize+"&"+conditions;
		strBuff.append("<a href=\"" + lastPage + "\" title=\"\" target=\"\">末页</a>");
		
		//共几页：<span>共<strong>10</strong>页</span> 
		strBuff.append("<span>共<strong>" + totalPageCount + "</strong>页</span>");
		
		//总记录数：<span><strong>50</strong>条记录</span> 
		strBuff.append("<span><strong>" + totalCount + "</strong>条记录</span>");

		//转到第几页：<span>转到<input type="text" class="l-text txt_test" value="1">页</span> 
		strBuff.append("<span>转到<input type=\"text\" id=\"goPage\" class=\"l-text txt_test\" value=\"" + currPage + "\">页</span>");
		
		//跳转按钮<a href="javascript:void(0)" title="" target="">GO</a> 
		strBuff.append("<a href=\"javascript:jumpPage()\" title=\"\" target=\"\">GO</a>");
		
		strBuff.append("</div>");
		
		//跳转脚本
		String script = "<script type=\"text/javascript\"> function jumpPage(){"
				+ "var currPage = $(\"#goPage\").val().trim(); var rule = /^\\+?[1-9][0-9]*$/;"
				+ "if(!rule.test(currPage)){"
				+ "alert(\"页码只能是正整数，请重新输入!\");"
				+ "return;"
				+ "}"
				+ "if(currPage > "+ this.totalPageCount + "){"
				+ "alert(\"输入页数大于当前最大页!\");"
				+ "$(\"#goPage\").val("+this.totalPageCount+");"
				+ "var currPage = "+ this.totalPageCount
				+ ";}" 
				+ "window.location.href=\"" + myUrl + "?currPage=\" + currPage + \"&pageSize=" + pageSize + "&" + conditions + "\";" 
				+ "}</script>";
		
		strBuff.append(script);

		return strBuff.toString();
	}
	
	private String getBackstageThemeScript(){
		
		//2012-12-18 增加判断，如果没有传函数，那就执行默认方法
		if(StringUtils.isNotBlank(funMethod)){
			defaultMethod = funMethod;
		}
		
		//<div class="page_warp"> 
		StringBuffer strBuff = new StringBuffer("<div class=\"page_warp\">");  
		//总页数 == 1
		if(totalPageCount == 1){
			strBuff.append("共"+totalPageCount+"页&nbsp;</div>");
			return strBuff.toString();
		}
		
		//当前页小于等于第一页
		if(currPage <= 1){
			currPage = 1;
		}
		
		//当前页大于等于总页数
		if(currPage >= totalPageCount){
			currPage = totalPageCount;
		}
		
		//首页：<a href="javascript:void(0)" title="" target="">首页</a> 
		strBuff.append("<a href=\"javascript:" + defaultMethod + "(1," + pageSize + ")\" title=\"\" target=\"\">首页</a>");

		//上一页：<a href="javascript:void(0)" title="" target="">上一页</a> 
		if(currPage == 1){
			strBuff.append("<span class=\"c_color\">上一页</span>");
		}else{
			strBuff.append("<a href=\"javascript:" + defaultMethod + "(" + (currPage-1) + "," + pageSize + ")\" title=\"\" target=\"\">上一页</a>");
		}

		//下一页：<a href="javascript:void(0)" title="" target="">下一页</a> 
		if(currPage == totalPageCount){
			strBuff.append("<span class=\"c_color\">下一页</span>");
		}else{
			strBuff.append("<a href=\"javascript:" + defaultMethod + "(" + (currPage+1) + "," + pageSize + ")\" title=\"\" target=\"\">下一页</a>");
		}

		//末页：<a href="javascript:void(0)" title="" target="">末页</a> 
		strBuff.append("<a href=\"javascript:" + defaultMethod + "(" + totalPageCount + "," + pageSize + ")\" title=\"\" target=\"\">末页</a>");
		
		//共几页：<span>共<strong>10</strong>页</span> 
		strBuff.append("<span>共<strong>" + totalPageCount + "</strong>页</span>");
		
		//总记录数：<span><strong>50</strong>条记录</span> 
		strBuff.append("<span><strong>" + totalCount + "</strong>条记录</span>");

		//转到第几页：<span>转到<input type="text" class="l-text txt_test" value="1">页</span> 
		strBuff.append("<span>转到<input type=\"text\" id=\"goPage\" class=\"l-text txt_test\" value=\"" + currPage + "\">页</span>");
		
		//跳转按钮<a href="javascript:void(0)" title="" target="">GO</a> 
		strBuff.append("<a href=\"javascript:jumpPage(" + pageSize + ")\" title=\"\" target=\"\">GO</a>");
		
		strBuff.append("</div>");
		String script = "<script type=\"text/javascript\"> function jumpPage(pageSize){"
				+ "var currPage = $(\"#goPage\").val().trim(); var rule = /^\\+?[1-9][0-9]*$/;"
				+ "if(!rule.test(currPage)){"
				+ "alert(\"页码只能是正整数，请重新输入!\");"
				+ "return;"
				+ "}"
				+ "if(currPage > "+ this.totalPageCount + "){"
				+ "alert(\"输入页数大于当前最大页!\");"
				+ "$(\"#goPage\").val("+this.totalPageCount+");"
				+ "var currPage = "+ this.totalPageCount
				+ "}"
				+ defaultMethod
				+ "(currPage,pageSize);"
				+ "}</script>";
		strBuff.append(script);
		return strBuff.toString();
	}
	
	private String getDefaultTheme(){
		StringBuffer strBuff = new StringBuffer("<div class=\"pageDivClass\">");
		String myUrl = Request.current().actionMethod;
		
		if(totalPageCount == 1) {
			strBuff.append("第"+currPage+"页").append("/共"+totalPageCount+"页</div>");
			return strBuff.toString();
		}
		
		if(currPage <= 1) {
			currPage = 1;
		}
		
		if(currPage >= totalPageCount) {
			currPage = totalPageCount;
		}
		
		int nextPage = currPage+1 > totalPageCount ? totalPageCount : currPage + 1;
		String dwonPage = myUrl + "?currPage=" + nextPage + "&pageSize=" + pageSize+"&"+conditions;
		
		
		int lastPage = currPage - 1 < 1 ? 1  :currPage - 1;
		String upPage = myUrl +"?currPage=" + lastPage + "&pageSize=" + pageSize+"&"+conditions;
		strBuff.append("第" + currPage + "页").append("/共" + totalPageCount + "页&nbsp;&nbsp;");
		
		// ---------样式 2012-07-10 增加 begin-------------
		String upColor = getColor(currPage-1 == 0?1:currPage == totalPageCount?totalPageCount:currPage-1,lastPage);
		String downColor = getColor(currPage,nextPage);
		// ---------样式 2012-07-10 增加 begin-------------
		
		String homeUrl = myUrl + "?currPage=1&pageSize="+pageSize+"&"+conditions;
		String notPage =  myUrl + "?currPage="+totalPageCount+"&pageSize="+pageSize+"&"+conditions;
		
		if(currPage != 1){
			strBuff.append("<a href=\""+homeUrl+"\">首页</a>&nbsp;<a href=\""+upPage+"\">&nbsp;</a>");
		}
		
		if(totalPageCount < showPageCount){
			showPageCount = totalPageCount;
		}
		
		if(showPageCount != 2){
			int i = 1,sum = 0;
			int half = showPageCount/2;
			if(currPage > half){
				i = currPage - half;
			}
			if(totalPageCount - i < showPageCount){//如果总页数减去起始位置小于 要显示的页码数
				i = totalPageCount - showPageCount + 1;
			}
			for(; i <=totalPageCount; i++ ){
				if(sum == showPageCount){
					break;
				}
				sum ++;
				// ---------样式 2012-07-10 增加 begin-------------
				String curColor = getColor(currPage, i);
				// ---------样式 2012-07-10 增加 begin-------------
				strBuff.append("&nbsp;<a href=\""+myUrl+"?currPage="+i+"&pageSize="+pageSize+"&"+conditions+"\" "+curColor+">"+i+"</a>&nbsp;");
			}
		}else{
			int tempCurrPage = currPage;
			if(currPage == 1){
				lastPage = 1;
				nextPage = 2;
			}else{
				if(currPage == totalPageCount){
					tempCurrPage = totalPageCount - 1;
				}
			}
			strBuff.append("&nbsp;<a href=\""+myUrl+"?currPage="+lastPage+"&pageSize="+pageSize+"&"+conditions+"\" " + upColor + ">"+tempCurrPage+"</a>&nbsp;");
			strBuff.append("&nbsp;<a href=\""+myUrl+"?currPage="+nextPage+"&pageSize="+pageSize+"&"+conditions+"\" " + downColor + ">"+(tempCurrPage+1)+"</a>&nbsp;");
		}
		if(currPage != totalPageCount){
			strBuff.append("<a href=\""+dwonPage+"\">下一页</a>&nbsp;<a href=\""+notPage+"\">末页</a> ");
		}
		String inputText = " <input type=\"text\" size=4 value=\""+currPage+"\" id=\"currPageText\"/> <a  id=\"jumpPage\">跳转</a></div>";
		strBuff.append(inputText);
		return strBuff.toString();
	}
	
	private String getDefaultThemeScript(){
		//2012-12-18 增加判断，如果没有传函数，那就执行默认方法
		if(StringUtils.isNotBlank(funMethod)){
			defaultMethod = funMethod;
		}
		
		StringBuffer strBuff = new StringBuffer("<div class='pageDivClass'>");
		//总页数 == 1
		if(totalPageCount == 1){
			strBuff.append("第"+currPage+"页").append("/共"+totalPageCount+"页</div>");
			return strBuff.toString();
		}
		
		//当前页小于等于第一页
		if(currPage <= 1){
			currPage = 1;
		}
		//当前页大于等于总页数
		if(currPage >= totalPageCount){
			currPage = totalPageCount;
		}
		int down = (currPage+1 > totalPageCount?totalPageCount:currPage+1);
		int up = (currPage-1 == 0?1:currPage-1);
		strBuff.append("第"+currPage+"页").append("/共"+totalPageCount+"页&nbsp;&nbsp;");
		if(currPage != 1){
			strBuff.append("<a href='javascript:"+defaultMethod+"(1,"+pageSize+");'>首页</a>&nbsp;<a href='javascript:"+defaultMethod+"("+up+","+pageSize+")'>&nbsp;</a>");
		}
		// ---------样式 2012-07-10 增加 begin-------------
		String upColor = getColor(currPage-1 == 0?1:currPage == totalPageCount?totalPageCount:currPage-1,up);
		String downColor = getColor(currPage,down);
		// ---------样式 2012-07-10 增加 begin-------------
		
		
		if(totalPageCount < showPageCount){
			showPageCount = totalPageCount;
		}
		
		if(showPageCount != 2){
			int i = 1,sum = 0;
			int half = showPageCount/2;
			if(currPage > half){
				i = currPage - half;
			}
			if(totalPageCount - i < showPageCount){//如果总页数减去起始位置小于 要显示的页码数
				i = totalPageCount - showPageCount + 1;
			}
			for(; i <=totalPageCount; i++ ){
				if(sum == showPageCount){
					break;
				}
				sum ++;
				// ---------样式 2012-07-10 增加 begin-------------
				String curColor = getColor(currPage, i);
				// ---------样式 2012-07-10 增加 begin-------------
				strBuff.append("&nbsp;<a href='javascript:"+defaultMethod+"("+i+","+pageSize+")' "+curColor+">"+i+"</a>&nbsp;");
			}
		}else{
			int tempcurrPage =currPage;
			if(currPage == 1){
				up = 1;down = 2;
			}else{
				if(currPage == totalPageCount){
					tempcurrPage = totalPageCount - 1;
				}
			}
			strBuff.append("&nbsp;<a href='javascript:"+defaultMethod+"("+up+","+pageSize+")' " + upColor + ">"+tempcurrPage+"</a>&nbsp;");
			strBuff.append("&nbsp;<a href='javascript:"+defaultMethod+"("+down+","+pageSize+")' " + downColor + ">"+(tempcurrPage+1)+"</a>&nbsp;");
		}
		if(currPage != totalPageCount){
			strBuff.append("<a href='javascript:"+defaultMethod+"("+down+","+pageSize+")'>下一页</a>&nbsp;");
		}
		
		if(currPage != totalPageCount){
			strBuff.append("<a href='javascript:"+defaultMethod+"("+totalPageCount+","+pageSize+")'>末页</a>");
		}
		String inputText = " <input type='text' size=4 value="+currPage+" id=\"goPage\"/> <a  href='javascript:jumpPage("+pageSize+")' id='jumpPage'>跳转</a></div>";
		String script = "<script type=\"text/javascript\"> function jumpPage(pageSize){"
				+ "var currPage = $(\"#goPage\").val();"
				+ "if(isNaN(currPage)){"
				+ "alert(\"输入格式不正确，请重新输入!\");"
				+ "return;"
				+ "}"
				+ "if(currPage > "+ this.totalPageCount + "){"
				+ "alert(\"输入页数大于当前最大页!\");"
				+ "$(\"#goPage\").val("+this.totalPageCount+");"
				+ "var currPage = "+ this.totalPageCount
				+ "}"
				+ defaultMethod
				+ "(currPage,pageSize);"
				+ "}</script>";
		strBuff.append(inputText);
		strBuff.append(script);
		return strBuff.toString();
	}

	
	/**
	 * 返回颜色
	 */
	private String getColor(int currPage, int i){
		if(currPage == i){
			return "class='currPageColor'";
		}
		return "";
	}

	public int[] getRount(int showPageCount,int totalPageCount,int currPage){
        int[] inter = new int[2];//定义一个具有两个元素的数组
        int rount = totalPageCount - showPageCount + 1;
        if(rount > 0){
            if(rount > showPageCount){
                if(currPage >= showPageCount && currPage <= rount){
                    inter[0] = currPage;
                    inter[1] = showPageCount + currPage - 1;
                }else if(currPage > rount && currPage <= totalPageCount){
                    inter[0] = rount;
                    inter[1] = totalPageCount;
                }else{
                    inter[0] = 1;
                    inter[1] = showPageCount;
                }
            }else{
                if(currPage >= showPageCount){
                    inter[0] = rount;
                    inter[1] = totalPageCount;
                }else{
                    inter[0] = 1;
                    inter[1] = showPageCount;
                }
            }
        }else{
            inter[0] = 1;
            inter[1] = showPageCount;
        }
        return inter;
    }
	
	public static void main(String[] args) {
		Page page = new Page();
		page.currPage = 3;
		page.totalCount = 200;
		page.totalPageCount = 20;
		page.pageSize = 10;
		int[] inter = page.getRount(3,50,9);
        for(int i = inter[0]; i <= inter[1]; i++){
           System.out.print(i + ",");
        }
	}
}
