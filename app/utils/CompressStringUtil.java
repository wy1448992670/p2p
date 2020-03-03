package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**

 * 这个方法主要用来完成对字符串的压缩, 解压缩, 以及Base64编码, 解码压缩结果

 *@author anwx

 */

public final class CompressStringUtil {

    /**

     * 解压缩

     * @param compressed

     * @return

     */

    public static final String decompress(byte[] compressed) {

        if (compressed == null)

            return null;

        ByteArrayOutputStream out = null;

        ByteArrayInputStream in = null;

        ZipInputStream zin = null;

        String decompressed;

        try {

            out = new ByteArrayOutputStream();

            in = new ByteArrayInputStream(compressed);

            zin = new ZipInputStream(in);

            ZipEntry entry = zin.getNextEntry();

            byte[] buffer = new byte[1024];

            int offset = -1;

            while ((offset = zin.read(buffer)) != -1) {

                out.write(buffer, 0, offset);

            }

            decompressed = new String(out.toByteArray(),"GBK");

        } catch (IOException e) {

            decompressed = null;

            throw new RuntimeException("解压缩字符串数据出错", e);

        } finally {

            if (zin != null) {

                try {

                    zin.close();

                } catch (IOException e) {

                	e.printStackTrace();
                }

            }

            if (in != null) {

                try {

                    in.close();

                } catch (IOException e) {

                	e.printStackTrace();
                }

            }

            if (out != null) {

                try {

                    out.close();

                } catch (IOException e) {

                	e.printStackTrace();
                }

            }

        }

        return decompressed;

    }

}

