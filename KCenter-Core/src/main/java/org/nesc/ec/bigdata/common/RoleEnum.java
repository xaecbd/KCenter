package org.nesc.ec.bigdata.common;

import org.nesc.ec.bigdata.constant.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rd87
 */
public enum RoleEnum {
	/**
	 * 成员
	 */
	MEMBER(100, Constants.Role.MEMBER),
	/**
	 * 普通管理员
	 */
	MASTER(10, Constants.Role.MASTER),
	/**
	 * 超级管理员
	 */
    ADMIN(1, Constants.Role.ADMIN);
	private static final Map<Integer, RoleEnum> LOOKUP = new HashMap<>();

	static {
		for (RoleEnum role : RoleEnum.values()) {
			LOOKUP.put(role.getValue(), role);
		}
	}

	private int value;
	private String description;

	RoleEnum(int value, String description) {
		this.value = value;
		this.description = description;
	}

	public static RoleEnum get(int value) {
		return LOOKUP.get(value);
	}

	public int getValue() {
		return value;
	}

	public String getDescription() {
		return description;
	}

}
