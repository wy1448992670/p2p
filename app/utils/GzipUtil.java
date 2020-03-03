package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public final class GzipUtil {

    public static String uncompress(InputStream gzippedResponse) throws IOException {

        InputStream decompressedResponse = new GZIPInputStream(gzippedResponse);
        Reader reader = new InputStreamReader(decompressedResponse, "UTF-8");
        StringWriter writer = new StringWriter();

        char[] buffer = new char[10240];
        for(int length = 0; (length = reader.read(buffer)) > 0;){
            writer.write(buffer, 0, length);
        }

        writer.close();
        reader.close();
        decompressedResponse.close();
        gzippedResponse.close();

        return writer.toString();
    }


    public static void main(String[] args) throws IOException {
        // {mobile} 申请人的手机号
        // {task_id} 用户授权认证，创建运营商采集任务的任务ID
        // 请注意修改
        // 样例：URL url = new URL("https://api.51datakey.com/carrier/v3/mobiles/13100000000/mxdata?task_id=008ef370-5f11-11e6-909c-00163e004a23“);
        URL url = new URL("https://api.51datakey.com/carrier/v3/mobiles/18750637468/mxdata?task_id=ec795fc0-4aee-11e9-9a39-00163e0f4efb");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
        connection.setRequestProperty("Accept", "application/json");

        // value 为魔蝎分配的token
        // 请注意修改
        // 样例：connection.setRequestProperty("Authorization", "token 090b2fd02d034dbea409bbd5f9900bc2");
        connection.setRequestProperty("Authorization", "token " + "ab5e7115d8994cbebbc0399fb5b865e9");
        connection.connect();
        System.out.println(uncompress(connection.getInputStream()));
        connection.disconnect();

    }
}
