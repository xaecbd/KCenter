package org.nesc.ec.bigdata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author Truman.P.Du
 * @date 2019年4月20日 上午8:30:41
 * @version 1.0
 */
@Component
public class InitServer {
	@Bean
	public RestTemplate buildRestTemplate() {
		return new RestTemplate(simpleClientHttpRequestFactory());
	}

	private ClientHttpRequestFactory simpleClientHttpRequestFactory() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setReadTimeout(10000);
		factory.setConnectTimeout(15000);
		return factory;
	}
}
