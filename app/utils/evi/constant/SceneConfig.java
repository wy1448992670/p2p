package utils.evi.constant;

/***
 * @Description: 场景式存证_常用配置信息类
 * @Team: 公有云技术支持小组
 * @Author: 天云小生
 * @Date: 2018年01月11日
 */
public class SceneConfig {

	// 项目ID(公共应用ID)-模拟环境,正式环境下贵司将拥有独立的应用ID
	public static final String PROJECT_ID = "1111565047";
	// 项目Secret(公共应用Secret)-模拟环境,正式环境下贵司将拥有独立的应用Secret
	public static final String PROJECT_SECRET = "16fc37cf0ddf9d6145a5a8cecdebc924";
	// 编码格式
	public static final String ENCODING = "UTF-8";
	// 哈希算法
	public static final String ALGORITHM = "HmacSHA256";
	// 创建行业类型API接口调用地址(模拟地址)-创建所属行业类型
//	public static final String HTTP_BUS_TYPE_SIMULATE_APIURL = "http://smlcunzheng.tsign.cn:8083/evi-service/evidence/v1/sp/temp/bus/add";
	// 创建行业类型API接口调用地址(正式地址)
	public static final String HTTP_BUS_TYPE_OFFICIAL_APIURL = "http://evislb.tsign.cn:8080/evi-service/evidence/v1/sp/temp/bus/add";

	// 创建存证场景类型API接口调用地址(模拟地址)
//	public static final String HTTP_SCENE_TYPE_SIMULATE_APIURL = "http://smlcunzheng.tsign.cn:8083/evi-service/evidence/v1/sp/temp/scene/add";
	// 创建存证场景类型API接口调用地址(正式地址)
	public static final String HTTP_SCENE_TYPE_OFFICIAL_APIURL = "http://evislb.tsign.cn:8080/evi-service/evidence/v1/sp/temp/scene/add";

	// 创建存证场景环节类型API接口调用地址(模拟地址)
//	public static final String HTTP_SEG_TYPE_SIMULATE_APIURL = "http://smlcunzheng.tsign.cn:8083/evi-service/evidence/v1/sp/temp/seg/add";
	// 创建存证场景环节类型API接口调用地址(正式地址)
	public static final String HTTP_SEG_TYPE_OFFICIAL_APIURL = "http://evislb.tsign.cn:8080/evi-service/evidence/v1/sp/temp/seg/add";

	// 创建存证场景环节类型API接口调用地址(模拟地址)
//	public static final String HTTP_SEGPROP_SIMULATE_APIURL = "http://smlcunzheng.tsign.cn:8083/evi-service/evidence/v1/sp/temp/seg-prop/add";
	// 创建存证场景环节类型API接口调用地址(正式地址)
	public static final String HTTP_SEGPROP_OFFICIAL_APIURL = "http://evislb.tsign.cn:8080/evi-service/evidence/v1/sp/temp/seg-prop/add";

	// 创建存证环节(原文基础版)API接口调用地址(模拟地址)
//	public static final String HTTP_ORIGINAL_STANDARD_SIMULATE_APIURL = "http://smlcunzheng.tsign.cn:8083/evi-service/evidence/v1/sp/segment/original-std/url";
	// 创建存证环节(原文基础版)API接口调用地址(正式地址)
	public static final String HTTP_ORIGINAL_STANDARD_OFFICIAL_APIURL = "http://evislb.tsign.cn:8080/evi-service/evidence/v1/sp/segment/original-std/url";

	// 创建存证环节(原文高级版)API接口调用地址(模拟地址)
//	public static final String HTTP_ORIGINAL_ADVANCED_SIMULATE_APIURL = "http://smlcunzheng.tsign.cn:8083/evi-service/evidence/v1/sp/segment/original-adv/url";
	// 创建存证环节(原文高级版)API接口调用地址(正式地址)
	public static final String HTTP_ORIGINAL_ADVANCED_OFFICIAL_APIURL = "http://evislb.tsign.cn:8080/evi-service/evidence/v1/sp/segment/original-adv/url";

	// 创建存证环节(摘要版)API接口调用地址(模拟地址)
//	public static final String HTTP_ORIGINAL_DIGEST_SIMULATE_APIURL = "http://smlcunzheng.tsign.cn:8083/evi-service/evidence/v1/sp/segment/abstract/url";
	// 创建存证环节(摘要版)API接口调用地址(正式地址)
	public static final String HTTP_ORIGINAL_DIGEST_OFFICIAL_APIURL = "http://evislb.tsign.cn:8080/evi-service/evidence/v1/sp/segment/abstract/url";

	// 构建证据链API接口调用地址(模拟地址)
//	public static final String HTTP_VOUCHER_SIMULATE_APIURL = "http://smlcunzheng.tsign.cn:8083/evi-service/evidence/v1/sp/scene/voucher";
	// 构建证据链API接口调用地址(正式地址)
	public static final String HTTP_VOUCHER_OFFICIAL_APIURL = "http://evislb.tsign.cn:8080/evi-service/evidence/v1/sp/scene/voucher";

	// 追加证据链API接口调用地址(模拟地址)
//	public static final String HTTP_VOUCHER_APPEND_SIMULATE_APIURL = "http://smlcunzheng.tsign.cn:8083/evi-service/evidence/v1/sp/scene/append";
	// 追加证据链API接口调用地址(正式地址)
	public static final String HTTP_VOUCHER_APPEND_OFFICIAL_APIURL = "http://evislb.tsign.cn:8080/evi-service/evidence/v1/sp/scene/append";

	// 场景式存证编号关联到指定的用户API接口调用地址(模拟地址)
//	public static final String HTTP_RELATE_USER_SIMULATE_APIURL = "http://smlcunzheng.tsign.cn:8083/evi-service/evidence/v1/sp/scene/relate";
	// 场景式存证编号关联到指定的用户API接口调用地址(正式地址)
	public static final String HTTP_RELATE_USER_OFFICIAL_APIURL = "http://evislb.tsign.cn:8080/evi-service/evidence/v1/sp/scene/relate";

	// 存证证明查看页面Url(模拟地址)
//	public static final String HTTPS_VIEWPAGE_SIMULATE_URL = "https://smlcunzheng.tsign.cn/evi-web/static/certificate-info.html";
	// 存证证明查看页面Url(正式地址)
	public static final String HTTPS_VIEWPAGE_OFFICIAL_URL = "https://eviweb.tsign.cn/evi-web/static/certificate-info.html";
}
