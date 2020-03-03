package utils.evi.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.evi.bean.CertificateBean;
import utils.evi.bean.IdsBean;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JSONHelper {
    @SuppressWarnings("unused")
    private static Logger LOG = LoggerFactory.getLogger(JSONHelper.class);

    /***
     * 获取原文保全上传的请求参数JSON字符串
     * 
     * @param eviName
     *            存证名称
     * @param deadline 原文保全预设截止日期
     * @param filePath
     *            待保全文档路径
     * @param eSignIds
     *            电子签名证据ID列表
     * @param bizIds
     *            e签宝业务ID列表
     * @return JSON字符串
     */
    public static String getOriginalRequestParameter(String eviName, String filePath, String deadline,
	    Map<String, List<String>> eSignIds, Map<String, List<String>> bizIds) {

	JSONObject eviObj = new JSONObject();
	eviObj.put("eviName", eviName);
	eviObj.put("content", getFileContent(filePath,deadline));
	// 电子签名证据ID列表与e签宝业务ID列表
	eviObj.element("eSignIds", getIds(eSignIds));
	// 电子签名证据ID列表与e签宝业务ID列表（允许为空）
	if (null != bizIds) {
	    eviObj.element("bizIds", getIds(bizIds));
	}
	return eviObj.toString();
    }

    /***
     * 获取原文保全上传的请求参数JSON字符串
     * 
     * @param evId
     *            存证编号
     * @param filePath
     *            待保全文档路径
     * @param eSignIds
     *            电子签名证据ID列表
     * @param bizIds
     *            e签宝业务ID列表
     * @return JSON字符串
     */
    public static String getRelateRequestParameter(String evId, List<CertificateBean> certificates) {
	JSONObject relateObj = new JSONObject();
	relateObj.put("evid", evId);
	relateObj.element("certificates", getCertificates(certificates));
	return relateObj.toString();
    }

    /***
     * 获取待保全文档的文件信息
     * 
     * @param filePath
     *            待保全文档路径
     * @param deadline
     *            原文保全预设截止日期
     * @return
     */
    public static String getFileContent(String filePath, String deadline) {
	Map<String, String> fileInfo = FileHelper.getFileInfo(filePath);
	JSONObject contentObj = new JSONObject();
	// 原文保全预设截止日期
	contentObj.put("storageLife", deadline);
	// 待保全文档名称（文件名中不允许含有? * : " < > \ / | [ ] 【】）
	contentObj.put("contentDescription", fileInfo.get("FileName"));
	// 待保全文档大小，单位：字节
	contentObj.put("contentLength", fileInfo.get("FileLength"));
	// 待保全文档内容字节流MD5的Base64编码值
	contentObj.put("contentBase64Md5", DigestHelper.getContentMD5(filePath));
	return contentObj.toString();
    }

    /***
     * ID列表
     * 
     * @param ids
     * @return JSON字符串
     */
    public static String getIds(Map<String, List<String>> ids) {
	LinkedList<IdsBean> eSignIds = new LinkedList<IdsBean>();
	for (Map.Entry<String, List<String>> entry : ids.entrySet()) {
	    String type = entry.getKey();
	    List<String> list = entry.getValue();
	    if (!(ToolsHelper.isNull(list))) {
		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
		    String value = iter.next();
		    IdsBean eSignIdBean = new IdsBean();
		    eSignIdBean.setType(type);
		    eSignIdBean.setValue(value);
		    eSignIds.add(eSignIdBean);
		}
	    }
	}

	JSONArray idsArray = JSONArray.fromObject(eSignIds);
	return idsArray.toString();
    }

    /***
     * 用户证件信息
     * 
     * @param ids
     * @return JSON字符串
     */
    public static String getCertificates(List<CertificateBean> certificates) {
	JSONArray idsArray = JSONArray.fromObject(certificates);
	return idsArray.toString();
    }

    /***
     * 将JSON字符串转成JSON对象
     * 
     * @param str
     * @return JSON对象
     */
    public static JSONObject toJSONObject(String str) {
	JSONObject obj = JSONObject.fromObject(str);
	return obj;
    }
}
