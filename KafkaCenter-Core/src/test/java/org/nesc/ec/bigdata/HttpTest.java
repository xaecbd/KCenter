package org.nesc.ec.bigdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * @author Truman.P.Du
 * @date 2020/03/04
 * @description
 */
public class HttpTest {
    public static void main(String[] args) {
        InputStream eventStream;
        HttpClient httpClient = HttpClient.newBuilder().build();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:8088/query"))
                    .timeout(Duration.ofMinutes(1))
                    .header("Content-Type", "application/vnd.ksql.v1+json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"ksql\": \"SELECT * FROM truman EMIT CHANGES;\"}"))
                    .build();
            HttpResponse<InputStream> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            eventStream = httpResponse.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to get status event stream", e);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(eventStream));
        String line = "";

        try {
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read status event stream", e);
        }
    }
}
