package org.nesc.ec.bigdata.common;

/**
 * @author：Truman.P.Du
 * @createDate: 2019年3月28日 下午1:07:00
 * @version:1.0
 * @description:
 */
public enum SessionAttr {
	/**
	 * session_user
	 */
	USER("session_user");
	
	private String value;

	private SessionAttr(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
