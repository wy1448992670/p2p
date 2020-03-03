package utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import constants.Constants;
import constants.Constants.FileFormat;
import play.Logger;
import play.Play;
import play.db.jpa.Blob;

/**
 * 文件操作
 * 
 * @author bsr
 * @version 6.0
 * @created 2014-5-5 上午11:47:05
 */
public class FileUtil {

	public static FileType uploadFile(File imgFile, ErrorInfo error) {
		error.clear();

		if (null == imgFile) {
			error.code = -1;
			error.msg = "上传文件为空";

			return null;
		}

		FileType fileType = new FileType();

		fileType.file = imgFile;

		if (fileType.file == null) {
			error.code = -1;
			error.msg = "上传文件为空";

			return null;
		}

		String type = fileType.fileType;

		if (type == null) {
			error.code = -2;
			error.msg = "上传文件类型有误";

			return null;
		}

		FileInputStream fis = null;

		try {
			fis = new FileInputStream(imgFile);
			Blob blob = new Blob();
			blob.set(fis, "image/png");
			fileType.filePath = "/images?uuid=" + blob.getUUID();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Logger.error("上传图片 复制文件 出现异常!" + e.getMessage());
			error.code = -4;
			error.msg = "上传图片 复制文件 出现异常";
		}

		error.msg = "上传图片成功";

		return fileType;
	}
	
	/**
	 * 上传文件
	 * @param file
	 * @param type(1 图片、2 文本、3 视频、4 音频、5 表格)
	 * @param error
	 * @return
	 */
	public static Map<String, Object> uploadFile(File file, int type, ErrorInfo error) {
		error.clear();
		 
		if (null == file) {
			error.code = -1;
			error.msg = "上传文件为空";

			return null;
		}
		
		if (type < FileFormat.IMG || type > FileFormat.XLS) {
			error.code = -1;
			error.msg = "上传文件格式有误";

			return null;
		}
		
		String fileName = file.getName();
		String fileExt = fileName.substring(fileName.lastIndexOf(".")+1);
		
		switch (type) {
		case FileFormat.IMG:
			if (!"GIF,JPG,JPEG,PNG,BMP".contains(fileExt.toUpperCase())) {
				error.code = -1;
				error.msg = "文件格式有误，请上传图片(gif,jpg,jpeg,png,bmp)文件";

				return null;
			}
			
			if(file.length() > Constants.PICTURE_SIZE){
				error.code = -1;
				error.msg = "您上传的文件已超出最大限制10M，请重新上传";

				return null;
			}
			
			break;
		case FileFormat.TXT:
			if (!"TXT".contains(fileExt.toUpperCase())) {
				error.code = -1;
				error.msg = "文件格式有误，请上传文本(txt)文件";

				return null;
			}
			
			if(file.length() > Constants.TXT_SIZE){
				error.code = -1;
				error.msg = "您上传的文件已超出最大限制5M，请重新上传";

				return null;
			}
			
			break;
		case FileFormat.VIDEO:
			if (!"MP4,3GP,AVI,WMV,RM,RMVB".contains(fileExt.toUpperCase())) {
				error.code = -1;
				error.msg = "文件格式有误，请上传视频(mp4,3gp,avi,wmv,rm,rmvb)文件";

				return null;
			}
			
			if(file.length() > Constants.VIDEO_SIZE){
				error.code = -1;
				error.msg = "您上传的文件已超出最大限制100M，请重新上传";

				return null;
			}
			
			break;
		case FileFormat.AUDIO:
			if (!"MP3,WAV,WMA".contains(fileExt.toUpperCase())) {
				error.code = -1;
				error.msg = "文件格式有误，请上传音频(mp3,wav,wma)文件";

				return null;
			}
			
			if(file.length() > Constants.AUDIO_SIZE){
				error.code = -1;
				error.msg = "您上传的文件已超出最大限制100M，请重新上传";

				return null;
			}
			
			break;
		case FileFormat.XLS:
			if (!"XLS".contains(fileExt.toUpperCase())) {
				error.code = -1;
				error.msg = "文件格式有误，请上传表格(xls)文件";

				return null;
			}
			
			if(file.length() > Constants.XLS_SIZE){
				error.code = -1;
				error.msg = "您上传的文件已超出最大限制5M，请重新上传";

				return null;
			}
			
			break;
		default:
			break;
		}
		
		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			Logger.error(e.getMessage());
			error.code = -4;
			error.msg = "找不到文件"+file.getName();
			
			return null;
		}
		
		Blob blob = new Blob();
		blob.set(fis, "");
		String filePre = (FileFormat.IMG == type) ? "/images?uuid=" : "";
		
		Map<String, Object> fileInfo = new HashMap<String, Object>();
		fileInfo.put("fileType", fileExt);
		fileInfo.put("size", Arith.div(file.length(), 1024, 2));
		fileInfo.put("fileName", filePre + blob.getUUID() + "." + fileExt);
		
		return fileInfo;
	}
	
	public static File zipFiles(String[] files, String targetZipFile) {
		Blob blob = new Blob();
		File targetFile = new File(targetZipFile);
		FileOutputStream target = null;
		
		try {
			target = new FileOutputStream(targetFile);
		} catch (FileNotFoundException e1) {
		}
			
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(target));
		int BUFFER_SIZE = 1024;
		int count;
		byte buff[] = new byte[BUFFER_SIZE];
		File file = null;
		
		for (int i = 0; i < files.length; i++) {
			String[] arr = files[i].split("\\.");
			
			if (arr.length < 1) {
				continue;
			}
			
			file = new File(blob.getStore(), arr[0]);
			FileInputStream fi = null;
			
			try {
				fi = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				continue;
			}
			
			BufferedInputStream origin = new BufferedInputStream(fi);
			ZipEntry entry = new ZipEntry(file.getName() + "." + (arr.length<2 ? "png" : arr[1]));
			
			try {
				out.putNextEntry(entry);
			} catch (IOException e) {
				continue;
			}
			
			
			try {
				while ((count = origin.read(buff)) != -1) {
					out.write(buff, 0, count);
				}
			} catch (IOException e) {
				continue;
			}
			
			try {
				origin.close();
			} catch (IOException e) {
				continue;
			}
		}
		
		try {
			out.close();
		} catch (IOException e) {
		}

		return targetFile;
	}
	
	public static File zipImages(String[] images, String targetZipFile) {
		Blob blob = new Blob();
		File targetFile = new File(targetZipFile);
		FileOutputStream target = null;
		
		try {
			target = new FileOutputStream(targetFile);
		} catch (FileNotFoundException e1) {
		}
			
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(target));
		int BUFFER_SIZE = 1024;
		int count;
		byte buff[] = new byte[BUFFER_SIZE];
		File file = null;
		
		for (int i = 0; i < images.length; i++) {
			file = new File(blob.getStore(), images[i].split("\\.")[0]);
			FileInputStream fi = null;
			
			try {
				fi = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				continue;
			}
			
			BufferedInputStream origin = new BufferedInputStream(fi);
			ZipEntry entry = new ZipEntry(file.getName() + ".png");
			
			try {
				out.putNextEntry(entry);
			} catch (IOException e) {
				continue;
			}
			
			
			try {
				while ((count = origin.read(buff)) != -1) {
					out.write(buff, 0, count);
				}
			} catch (IOException e) {
				continue;
			}
			
			try {
				origin.close();
			} catch (IOException e) {
				continue;
			}
		}
		
		try {
			out.close();
		} catch (IOException e) {
		}

		return targetFile;
	}
	
	public static File getStore(String path) {
        String name = path;
        File store = null;
        if(new File(name).isAbsolute()) {
            store = new File(name);
        } else {
            store = Play.getFile(name);
        }
        if(!store.exists()) {
            store.mkdirs();
        }
        return store;
    }
	
    public static byte[] getFile(String path) throws Exception {  
        byte[] b = null;  
        File file = new File(path);  
  
        FileInputStream fis = null;  
        ByteArrayOutputStream ops = null;  
        try {  
  
            if (!file.exists()) {  
                System.out.println("文件不存在！");  
            }  
            if (file.isDirectory()) {  
                System.out.println("不能上传目录！");  
            }  
  
            byte[] temp = new byte[2048];  
  
            fis = new FileInputStream(file);  
            ops = new ByteArrayOutputStream(2048);  
  
            int n;  
            while ((n = fis.read(temp)) != -1) {  
                ops.write(temp, 0, n);  
            }  
            b = ops.toByteArray();  
        } catch (Exception e) {  
            throw new Exception();  
        } finally {  
            if (ops != null) {  
                ops.close();  
            }  
            if (fis != null) {  
                fis.close();  
            }  
        }  
        return b;  
    }
    
    public static File strToFile(byte[] b, String path)  {  
    	File file = new File(path);  
    	FileOutputStream fis = null;  
    	BufferedOutputStream bos = null;  
    	try {  
    		fis = new FileOutputStream(file);  
    		bos = new BufferedOutputStream(fis);  
    		bos.write(b);  
    	} catch (Exception e) {  
    		e.printStackTrace();  
    	} finally {  
    		if (bos != null) {  
    			try {
					bos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
    		}  
    
    		if (fis != null) {  
    			try {
    				fis.close();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}  
    		}
    	}
    	
    	return file;
    }
	
	/**
	 * 创建文件夹
	 * @param path
	 */
	public static void mkDir(String path){
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
	}
	
	/**
	 * 根据当前日期获取文件夹路径 eg:2015\1\3
	 * @return
	 */
	public static String getPathByCurrentDate(){
		String path = File.separator + DateUtil.getYear() + File.separator + DateUtil.getMonth() + File.separator + DateUtil.getDay();
		return path;
	}
	
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Jun 7, 2018 9:50:03 AM 
	 * @description.  按行读取文件
	 * 
	 * @param filePath
	 * @return
	 */
	public static JSONObject readTxtFile(String filePath) {
		JSONObject result = new JSONObject();
		
		Set<String> data = new HashSet<String>();
		try {
			String encoding = "UTF8";
			File file = new File(filePath);
			if (file.isFile() && file.exists()) { // 判断文件是否存在
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;
				while ((lineTxt = bufferedReader.readLine()) != null) {
					data.add(lineTxt);
				}
				bufferedReader.close();
				read.close();
			} else {
				System.out.println("找不到指定的文件");
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		
		result.put("file", filePath);
		result.put("data", data);
		return result;
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Jun 7, 2018 10:27:38 AM 
	 * @description. 读文件文件夹中的全部文件 
	 * 
	 * @param data
	 * @param fileOrFolderPath
	 * @return
	 */
	public static JSONArray readTxtFile4Folder(JSONArray data, String fileOrFolderPath, String...fileSuffix) {
		if(data == null) {
			data = new JSONArray();
		}
		try {
			File file = new File(fileOrFolderPath);
			if(file.isDirectory()) {
				File[] fileList = file.listFiles();
				if(fileList != null) {
					for(File temp : fileList) {
						readTxtFile4Folder(data, temp.getPath(), fileSuffix);
					}
				}
			}else if(file.isFile()) {
				if(fileSuffix != null && fileSuffix.length > 0) {
					for(String suffix : fileSuffix) {
						if(StringUtils.endsWithIgnoreCase(file.getName(), suffix)) {
							data.add(readTxtFile(fileOrFolderPath));
						}
					}
				}else {
					data.add(readTxtFile(fileOrFolderPath));
				}
			}
		} catch (Exception e) {
			System.out.println("读取文件内容出错");
			e.printStackTrace();
		}
		return data;
	}
	
	/**
	 * 
	 * @author liulj
	 * @creationDate. Jun 7, 2018 10:44:22 AM 
	 * @description.  修改文件后缀
	 * 
	 * @param filePath
	 * @param suffix
	 */
	public static void renameFileSuffix(String filePath, String suffix) {
		try {
			File file = new File(filePath);
			file.renameTo(new File(filePath.concat(".").concat(suffix)));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 判断文件是否存在
	 * 
	 * @Description (TODO这里用一句话描述这个方法的作用)
	 * @param filePath
	 * @return 存在 true;不存在 false;
	 * @author: zj
	 */
	public boolean checkFileIsExists(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @Description // 判断文件夹是否存在 不存在则生成
	 * @param dirPath
	 * @author: zj
	 */
	public void mdDir(String dirPath) {
		File file = new File(dirPath);
		if (!file.exists()) {
			file.mkdir();
		}
	}
}
