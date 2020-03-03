/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     vo
 *
 *    Filename:    OcridcardVO.java
 *
 *    Description: TODO(用一句话描述该文件做什么)
 *
 *    Copyright:   Copyright (c) 2001-2014
 *
 *    Company:     Digital Telemedia Co.,Ltd
 *
 *    @author:     zj
 *
 *    @version:    1.0.0
 *
 *    Create at:   2018年12月21日 下午1:25:14
 *
 *    Revision:
 *
 *    2018年12月21日 下午1:25:14
 *        - first revision
 *
 *****************************************************************/
package vo;

/**
 * @ClassName OcridcardVO
 * @Description face++ 身份证ocr 验证结果
 * @author zj
 * @Date 2018年12月21日 下午1:25:14
 * @version 1.0.0
 */
public class OcridcardVO {

	/**
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 */
	public OcridcardVO() {
		// TODO Auto-generated constructor stub
	}

	private String name;
	private String address;
	private String idcardNumber;
	private String validDateStart;
	private String validDateEnd;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getIdcardNumber() {
		return idcardNumber;
	}

	public void setIdcardNumber(String idcardNumber) {
		this.idcardNumber = idcardNumber;
	}

	public String getValidDateStart() {
		return validDateStart;
	}

	public void setValidDateStart(String validDateStart) {
		this.validDateStart = validDateStart;
	}

	public String getValidDateEnd() {
		return validDateEnd;
	}

	public void setValidDateEnd(String validDateEnd) {
		this.validDateEnd = validDateEnd;
	}

	@Override
	public String toString() {
		return "OcridcardVO [name=" + name + ", address=" + address + ", idcardNumber=" + idcardNumber
				+ ", validDateStart=" + validDateStart + ", validDateEnd=" + validDateEnd + "]";
	}

}
