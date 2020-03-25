package org.nesc.ec.bigdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author：Truman.P.Du
 * @createDate: 2019年3月19日 上午11:13:24
 * @version:1.0
 * @description: 启动类
 */
@EnableAsync
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
