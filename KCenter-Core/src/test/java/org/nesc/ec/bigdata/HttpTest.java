package org.nesc.ec.bigdata;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Truman.P.Du
 * @date 2020/03/04
 * @description
 */
public class HttpTest {
    public static void main(String[] args) throws Exception {
        HttpTest test = new HttpTest();
        test.sendPost("http://127.0.0.1:8088/query","{\n" +
                "  \"ksql\": \"SELECT * FROM truman EMIT CHANGES;\",\n" +
                "  \"streamsProperties\": {}\n" +
                "}");
    }

    public  String sendPost(String u, String json) throws Exception {
        StringBuffer sbf = new StringBuffer();
        try {
            URL url = new URL(u);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            connection.setRequestProperty("Charset", "UTF-8");
            connection.addRequestProperty("Content-Type", "application/vnd.ksql.v1+json; charset=utf-8");
            connection.connect();
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            if (!"".equals(json)) {
                out.writeBytes(json);
            }
            out.flush();
            out.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                System.out.println(lines);
                sbf.append(lines);
            }
            System.out.println(sbf);
            reader.close();
            // 断开连接
            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sbf.toString();
    }
}
