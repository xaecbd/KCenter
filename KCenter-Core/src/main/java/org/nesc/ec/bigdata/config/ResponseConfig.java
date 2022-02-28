package org.nesc.ec.bigdata.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import org.nesc.ec.bigdata.common.RestResponse;

/**
 * @author：Truman.P.Du
 * @createDate: 2019年1月29日 上午10:27:09
 * @version:1.0
 * @description:
 */

@Configuration
public class ResponseConfig {
	@Bean
	public ResponseBodyWrapFactoryBean getResponseBodyWrap() {
		return new ResponseBodyWrapFactoryBean();
	}

	class ResponseBodyWrapFactoryBean implements InitializingBean {

		@Autowired
		private RequestMappingHandlerAdapter adapter;

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void afterPropertiesSet() throws Exception {

			List<HandlerMethodReturnValueHandler> returnValueHandlers = adapter.getReturnValueHandlers();
			List<HandlerMethodReturnValueHandler> handlers = new ArrayList(returnValueHandlers);
			decorateHandlers(handlers);
			adapter.setReturnValueHandlers(handlers);

		}

		private void decorateHandlers(List<HandlerMethodReturnValueHandler> handlers) {

			for (HandlerMethodReturnValueHandler handler : handlers) {
				if (handler instanceof RequestResponseBodyMethodProcessor) {
					ResponseBodyWrapHandler decorator = new ResponseBodyWrapHandler(handler);
					int index = handlers.indexOf(handler);
					handlers.set(index, decorator);
					break;
				}
			}

		}

	}

	static class ResponseBodyWrapHandler implements HandlerMethodReturnValueHandler {
		private final HandlerMethodReturnValueHandler delegate;

		public ResponseBodyWrapHandler(HandlerMethodReturnValueHandler delegate) {
			this.delegate = delegate;
		}

		@Override
		public boolean supportsReturnType(MethodParameter returnType) {
			return delegate.supportsReturnType(returnType);
		}

		@Override
		public void handleReturnValue(Object returnValue, MethodParameter returnType,
				ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
			RestResponse restResponse = null;
			if (returnValue instanceof RestResponse) {
				restResponse = (RestResponse) returnValue;
			} else {
				restResponse = new RestResponse(returnValue);
			}
			delegate.handleReturnValue(restResponse, returnType, mavContainer, webRequest);
		}

	}

}
