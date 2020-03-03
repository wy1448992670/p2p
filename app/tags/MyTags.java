package tags;

import groovy.lang.Closure;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.shove.Convert;

import constants.Constants;
import play.Play;
import play.templates.FastTags;
import play.templates.GroovyTemplate;
import play.templates.TagContext;
import play.templates.GroovyTemplate.ExecutableTemplate;
import play.templates.JavaExtensions;
import utils.Arith;
import utils.Page;

public class MyTags extends FastTags{

	/**
	 * 分页标签
	 */
	public static void _page(Map<String, Object> params, Closure body, PrintWriter out, 
			ExecutableTemplate template, int fromLine) {
		
		int currPage = (Integer) params.get("currPage");
		int pageSize = (Integer) (params.get("pageSize") == null ? Constants.TEN : params.get("pageSize"));
		int totalCount = (Integer) params.get("totalCount");
		int theme = (Integer) params.get("theme") == null ? Constants.PAGE_SIMPLE : (Integer) params.get("theme");
		int style = (Integer) params.get("style") == null ? Constants.PAGE_STYLE_DEFAULT : (Integer) params.get("style");
		String funMethod = (String) params.get("funMethod");
		String pageTitle = params.get("pageTitle") == null? "条记录":params.get("pageTitle")+"";
		String action = params.get("action") == null? null: params.get("action").toString();
		String condition = null;
		if(body != null) {
			condition = JavaExtensions.toString(body).trim();  //要去除空格，否则，可能会对拼接的HTML、JavaScript代码有有影响
		}
		
		
		Page page = new Page();
		
		page.currPage = currPage;
		page.pageSize = pageSize;
		page.totalCount = totalCount;
		page.funMethod = funMethod;
		page.pageTitle = pageTitle;
		page.style = style;
		page.action = action;
		if(condition != null) {
			page.conditions = condition;
		}
		
		if(theme == Constants.PAGE_SIMPLE) {
			String pageTag = page.getThemeNumber();
			out.println(pageTag);
		}
		
		if(theme == Constants.PAGE_ASYNCH) {
			String pageTag = page.getThemeNumberScript();
			out.println(pageTag);
		}
		
	}
	
//	public static void _table(Map<String, Object> params, Closure body, PrintWriter out, 
//			ExecutableTemplate template, int fromLine) { 
//		
//		List<String> columnNames =  (List<String>) params.get("columnNames");//表头列名
//		List<String> fields =  (List<String>) params.get("fields");//字段名
//		List<Object> listMap = (List<Object>) params.get("data");//对象集合
//		
//		String tableStyle = (String) params.get("tableStyle");//table样式
//		String thStyle = (String) params.get("thStyle");//table th样式
//		String trStyle = (String) params.get("trStyle");//table tr样式
//		String tdStyle = (String) params.get("tdStyle");//table td样式
//		
//		Sheet sheet = new Sheet();
//		String sheetTag = sheet.getTableCode(columnNames, fields, listMap,tableStyle,thStyle,trStyle,tdStyle);
//		
//		
//		out.println(sheetTag);
//		
//	}
	
	
	
//	public static void _page2(Map<String, Integer> params, Closure body, PrintWriter out, 
//			ExecutableTemplate template, int fromLine) {
//		
//		int currPage = params.get("currPage");
//		int pageSize = params.get("pageSize") == null ? 10 : params.get("pageSize");
//		int totalCount = params.get("totalCount");
//		int type = params.get("type");
//		
//		Page page = new Page();
//		
//		page.currPage = currPage;
//		page.pageSize = pageSize;
//		page.totalCount = totalCount;
//		page.type = type;
//		
//		
//		String pageTag = page.getThemeNumberScript();
//		out.println(pageTag);
//	}
	
	/**
	 *金额格式化标签
	 */
	public static void _format(Map<String, Object> params, Closure body, PrintWriter out, 
			ExecutableTemplate template, int fromLine) {
		double money = (Double) params.get("money");
		
		String result = "";
		
		if (money < 10000) {
			result = String.format("%.2f", money);
		}else if(10000 <= money &&  money< 100000000){
			result = Arith.round(money / 10000, 2)+"万";
		}else if(100000000 <= money &&  money< 1000000000000.00){
			result = Arith.round(money / 100000000, 4)+"亿";
		}else{
			result = Arith.round(money / 1000000000000.00, 4)+"万亿";
		}
		
		out.println(result);
	}
	
	public static void _formatMoney(Map<String, Object> params, Closure body, PrintWriter out, 
			ExecutableTemplate template, int fromLine) {
		double money = (Double) params.get("money");
		  
        NumberFormat formater = new DecimalFormat("###,##0.00");
        
        String result = formater.format(money);
        
        if(result.indexOf(".") == -1) {   
            result += ".00";   
        }
        out.println(result);  
    } 
	
	public static void _table(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		TagContext.current().data.put("tr_class_odd", args.get("tr_class_odd"));
		TagContext.current().data.put("tr_class_even", args.get("tr_class_even"));
		
		out.print("<table " + serialize(args, "tr_class_odd", "tr_class_even") + ">");
        out.println(JavaExtensions.toString(body));
        out.print("</table>");
	}
	
	public static void _tr(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		TagContext.current().data.put("tr_index", args.get("tr_index"));
		
		TagContext.current().data.put("th_height", args.get("th_height"));
		TagContext.current().data.put("th_align", args.get("th_align"));
		TagContext.current().data.put("th_valign", args.get("th_valign"));
		TagContext.current().data.put("th_bgcolor", args.get("th_bgcolor"));
		
		TagContext.current().data.put("td_height", args.get("td_height"));
		TagContext.current().data.put("td_align", args.get("td_align"));
		TagContext.current().data.put("td_valign", args.get("td_valign"));
		TagContext.current().data.put("td_bgcolor_odd", args.get("td_bgcolor_odd"));
		TagContext.current().data.put("td_bgcolor_even", args.get("td_bgcolor_even"));
		
		Object cls = args.get("class");
		
		if (cls == null) {
			if (args.get("tr_index") == null) {
				cls = null;
			} else if (Convert.strToInt(args.get("tr_index")+"", 0) % 2 == 1) {
				cls = TagContext.parent("table").data.get("tr_class_odd");
			} else {
				cls = TagContext.parent("table").data.get("tr_class_even");
			}
		}

		cls = (cls == null) ? "" : " class = \"" + cls + "\"";
		
		out.print("<tr " + cls + serialize(args, "th_height", "th_align", "th_valign", "th_bgcolor", "td_height", "td_align", "td_valign", "td_bgcolor_odd", "td_bgcolor_even", "tr_index", "class") + ">");
        out.println(JavaExtensions.toString(body));
        out.print("</tr>");
	}
	
	public static void _th(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Object height = args.get("height");
		Object align = args.get("align");
		Object valign = args.get("valign");
		Object bgcolor = args.get("bgcolor");
		
		height = (height == null) ? TagContext.parent("tr").data.get("th_height") : height;
		align = (align == null) ? TagContext.parent("tr").data.get("th_align") : align;
		valign = (valign == null) ? TagContext.parent("tr").data.get("th_valign") : valign;
		bgcolor = (bgcolor == null) ? TagContext.parent("tr").data.get("th_bgcolor") : bgcolor;
		
		height = (height == null) ? "" : " height = \"" + height + "\"";
		align = (align == null) ? "" : " align = \"" + align + "\"";
		valign = (valign == null) ? "" : " valign = \"" + valign + "\"";
		bgcolor = (bgcolor == null) ? "" : " bgcolor = \"" + bgcolor + "\"";
		
		out.print("<th " + height + align + valign + serialize(args, "height", "align", "valign", "bgcolor") + ">");
        out.println(JavaExtensions.toString(body));
        out.print("</th>");
	}
	
	public static void _td(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		Object height = args.get("height");
		Object align = args.get("align");
		Object valign = args.get("valign");
		
		height = (height == null) ? TagContext.parent("tr").data.get("td_height") : height;
		align = (align == null) ? TagContext.parent("tr").data.get("td_align") : align;
		valign = (valign == null) ? TagContext.parent("tr").data.get("td_valign") : valign;
		
		height = (height == null) ? "" : " height = \"" + height + "\"";
		align = (align == null) ? "" : " align = \"" + align + "\"";
		valign = (valign == null) ? "" : " valign = \"" + valign + "\"";
		
		Object bgcolor = args.get("bgcolor");
		
		if (bgcolor == null) {
			if (TagContext.parent("tr").data.get("tr_index") == null) {
				bgcolor = null;
			} else if (Convert.strToInt(TagContext.parent("tr").data.get("tr_index")+"", 0) % 2 == 1) {
				bgcolor = TagContext.parent("tr").data.get("td_bgcolor_odd");
			} else {
				bgcolor = TagContext.parent("tr").data.get("td_bgcolor_even");
			}
		}

		bgcolor = (bgcolor == null) ? "" : " bgcolor = \"" + bgcolor + "\"";
		
		out.print("<td " + height + align + valign + bgcolor + serialize(args, "height", "align", "valign", "bgcolor") + ">");
        out.println(JavaExtensions.toString(body));
        out.print("</td>");
	}
	
	/**
	 * 转换 '-' 为逾期的标签
	 * @param args
	 * @param body
	 * @param out
	 * @param template
	 * @param fromLine
	 */
	public static void _overdue(Map<?, ?> args, Closure body, PrintWriter out, ExecutableTemplate template, int fromLine) {
		String day = args.get("day") == null ? null : args.get("day").toString();
		
		if(null == day) 
			return;
		
		if(day.contains("-")) {
			 out.println(day.replace("-", "逾期"));
		}
	}
	
	 public static void _img(Map args, Closure body, PrintWriter out, GroovyTemplate.ExecutableTemplate template, int fromLine){
        boolean srcBoolean = args.containsKey("src");
        String src = (new StringBuilder()).append(args.get("src")).append("").toString();
        String escape = args.containsKey("escape") ? "\\" : "";
        String default_img = (new StringBuilder()).append(" onerror=\"this.src=").append(escape).append("'").append(Play.ctxPath).append("/public/images/default.png").append(escape).append("'\"").toString();
       
        if(srcBoolean)
        {
            String ctxPath = Play.ctxPath;
            if(src.startsWith(ctxPath))
                ctxPath = "";
            src = src.replaceAll("\\@", "").replaceAll("\\{", "").replaceAll("\\}", "");
            if(!src.startsWith("http"))
                src = (new StringBuilder()).append(ctxPath).append(src).toString();
            
            
            if (StringUtils.contains(src, "images?uuid=")){
            	if(!src.startsWith("http")){
            		String uuid = StringUtils.substring(src, StringUtils.lastIndexOf(src, '=') + 1);
            		String attachmentPath = Play.configuration.getProperty("attachments.path");
            		src = (new StringBuilder().append(Play.ctxPath).append('/').append(attachmentPath).append('/').append(uuid)).toString();
            	}
            }
        }
        String data_original = args.containsKey("data_original") ? (new StringBuilder()).append(args.get("data_original")).append("").toString() : "";
        
        if(StringUtils.isNotBlank(data_original))
        {
        	String ctxPath = Play.ctxPath;
        	if(data_original.startsWith(ctxPath))
        		ctxPath = "";
        	
        	if(!data_original.startsWith("http"))
        		data_original = (new StringBuilder()).append(ctxPath).append(data_original).toString();
        	
        	
        	if (StringUtils.contains(data_original, "images?uuid=")){
            	if(!src.startsWith("http")){
            		String uuid = StringUtils.substring(data_original, StringUtils.lastIndexOf(data_original, '=') + 1);
            		String attachmentPath = Play.configuration.getProperty("attachments.path");
            		data_original = (new StringBuilder().append(Play.ctxPath).append('/').append(attachmentPath).append('/').append(uuid)).toString();
            	}
            }
        	
        	data_original = (new StringBuilder()).append(" data-original=\"").append(data_original).append("\"").toString();
        }

        String alt = args.containsKey("alt") ? (new StringBuilder()).append(" alt=\"").append(args.get("alt")).append("\"").toString() : "";
        String id = args.containsKey("id") ? (new StringBuilder()).append(" id=\"").append(args.get("id")).append("\"").toString() : "";
        String name = args.containsKey("name") ? (new StringBuilder()).append(" id=\"").append(args.get("name")).append("\"").toString() : "";
        String title = args.containsKey("title") ? (new StringBuilder()).append(" title=\"").append(args.get("title")).append("\"").toString() : "";
        String width = args.containsKey("width") ? (new StringBuilder()).append(" width=\"").append(args.get("width")).append("\"").toString() : "";
        String height = args.containsKey("height") ? (new StringBuilder()).append(" height=\"").append(args.get("height")).append("\"").toString() : "";
        String cl = args.containsKey("class") ? (new StringBuilder()).append(" class=\"").append(args.get("class")).append("\"").toString() : "";
        String align = args.containsKey("align") ? (new StringBuilder()).append(" align=\"").append(args.get("align")).append("\"").toString() : "";
        String style = args.containsKey("style") ? (new StringBuilder()).append(" style=\"").append(args.get("style")).append("\"").toString() : "";
        out.print((new StringBuilder()).append("<img src=\"").append(src).append("\"").append(id).append(name).append(title).append(alt).append(width).append(height).append(cl).append(align).append(style).append(data_original).append(default_img).append("/>").toString());
    }
}
