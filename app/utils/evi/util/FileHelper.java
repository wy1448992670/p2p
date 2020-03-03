package utils.evi.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/***
 * 
* @Description: 文件辅助类
* @Team: 公有云技术支持小组
* @Author: 天云小生
* @Date: 2017年11月19日
 */
public class FileHelper {
    private static Logger LOG = LoggerFactory.getLogger(FileHelper.class);
    /***
     * 获取文件基本信息
     * @param filePath
     * @return
     */
    public static Map<String, String> getFileInfo(String filePath) {
        Map<String, String> fileInfo = new LinkedHashMap<String, String>();
        File file = new File(filePath);
        fileInfo.put("FileName", file.getName());
        fileInfo.put("FileLength", String.valueOf(file.length()));
        return fileInfo;
    }
    /***
     * 获取文件的Bytes
     * 
     * @param filePath
     * @return
     * @throws IOException
     */
    public static byte[] getBytes(String filePath) {
        File file = new File(filePath);
        FileInputStream fis = null;
        byte[] buffer = null;
        try {
            fis = new FileInputStream(file);
            buffer = new byte[(int) file.length()];
            fis.read(buffer);
            fis.close();
        }
        catch (FileNotFoundException e) {
            LOG.error("获取文件二进制字节流异常：" + e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e) {
            LOG.error("获取文件二进制字节流异常：" + e.getMessage());
            e.printStackTrace();
        }
        finally {
            try {
                fis.close();
            }
            catch (IOException e) {
        	 LOG.error("文件二进制字节流关闭时发生异常：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return buffer;
    }
}