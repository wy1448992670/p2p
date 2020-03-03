/******************************************************************
 *
 *    Java Lib For Android, Powered By Shenzhen Jiuzhou.
 *
 *    Copyright (c) 2001-2014 Digital Telemedia Co.,Ltd
 *    http://www.d-telemedia.com/
 *
 *    Package:     utils.faceId.utils
 *
 *    Filename:    ToolUtil.java
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
 *    Create at:   2018年12月24日 上午10:37:45
 *
 *    Revision:
 *
 *    2018年12月24日 上午10:37:45
 *        - first revision
 *
 *****************************************************************/
package utils.faceId.utils;

import java.io.File;

import play.Play;

/**
 * @ClassName ToolUtil
 * @Description
 * @author zj
 * @Date 2018年12月24日 上午10:37:45
 * @version 1.0.0
 */
public class ToolUtil {

	/**
	 * @Description TODO(这里用一句话描述这个方法的作用)
	 */
	public ToolUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @Description 获取文件名称，包含文件格式后缀名
	 * @param relativePath 相对路径
	 * @return
	 * @author: zj
	 */
	public static String getFileName(String relativePath) {
		return relativePath.substring(relativePath.lastIndexOf("/") + 1, relativePath.length());
	}

	/**
	 * @Description 获取文件的上级路径
	 * @param relativePath
	 * @return
	 * @author: zj
	 */
	public static String getParentPath(String relativePath) {
		return relativePath.substring(0, relativePath.lastIndexOf("/") + 1);
	}


	public static void main(String[] args) {
		System.out.println(getParentPath("/public/images/userImg.png"));
	}
}
