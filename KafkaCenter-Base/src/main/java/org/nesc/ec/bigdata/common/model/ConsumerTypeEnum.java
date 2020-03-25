package org.nesc.ec.bigdata.common.model;

/**
 * @author Truman.P.Du
 * @date 2019年4月10日 下午3:12:32
 * @version 1.0
 */
public enum ConsumerTypeEnum {
	ZOOKERPER(0), BROKER(1);

	private int type;
	

	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}


	private ConsumerTypeEnum(int type) {
		this.type = type;
	}
}
