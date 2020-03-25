package org.nesc.ec.bigdata.interceptor;

import org.nesc.ec.bigdata.common.SessionAttr;
import org.nesc.ec.bigdata.model.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author Truman.P.Du
 * @date 2019年4月16日 上午9:40:40
 * @version 1.0
 */
public class SessionTimeOutInterceptor implements HandlerInterceptor {

	// 可以随意访问的url
	public String[] allowUrls;

	public void setAllowUrls(String[] allowUrls) {
		this.allowUrls = allowUrls;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		String requestUrl = request.getRequestURI().replace(request.getContextPath(), "");
		if("/".equalsIgnoreCase(requestUrl)){
			return true;
		}
		HttpSession session = request.getSession(true);
		if (StringUtils.isNoneBlank(requestUrl)) {
			for (String url : allowUrls) {
				if (requestUrl.contains(url)) {
					return true;
				}
			}
		}
		UserInfo userInfo = (UserInfo) session.getAttribute(SessionAttr.USER.getValue());
		if (userInfo == null) {
			// 便于ajax请求获取到状态后，根据该状态强制重新登录
			response.setStatus(401);
			 return false;
		}
		return true;
	}
}
