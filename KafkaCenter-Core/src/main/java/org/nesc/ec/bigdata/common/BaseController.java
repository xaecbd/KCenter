package org.nesc.ec.bigdata.common;

import org.nesc.ec.bigdata.model.UserInfo;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author：Truman.P.Du
 * @createDate: 2019年1月29日 上午10:01:18
 * @version:1.0
 * @description:
 */
public class BaseController {

	public UserInfo  getCurrentUser() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		HttpSession session = request.getSession();
		UserInfo userInfo = (UserInfo) session.getAttribute(SessionAttr.USER.getValue());
		return userInfo;
	}

	public RestResponse SUCCESS() {
		return new RestResponse();
	}

	public RestResponse SUCCESS(String message) {
		return new RestResponse(200, message);
	}

	public RestResponse SUCCESS_DATA(Object data) {
		return new RestResponse(200, null, data);
	}

	public RestResponse SUCCESS(String message, Object data) {
		return new RestResponse(200, message, data);
	}

	public RestResponse ERROR(String message) {
		return new RestResponse(500, message);
	}
}
