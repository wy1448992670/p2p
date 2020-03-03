package utils.reapal.agent.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DsfFunction {



    /**
     * 普通TXT格式
     * @param fileName
     * @return
     */
    public static String readFileByLines(String fileName) {
        StringBuffer sb=new StringBuffer();

        FileInputStream  file = null;
        BufferedReader reader = null;
        try {
            file = new FileInputStream (fileName);
            InputStreamReader isr = new InputStreamReader(file, "GBK");
            reader = new BufferedReader(isr);
            String tempString = null;
            int line = 0;
            while ((tempString = reader.readLine()) != null) {
                //过滤空行
                if (tempString.trim().length()==0) {
                    continue;
                }
                line++;
                //过滤第一行
                if(line>1){
                    sb.append(tempString+"|");
                }

            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();

                } catch (IOException e1) {
                }
            }
        }
        //System.out.println(sb.toStrieng());
        return sb.toString();
    }
}
