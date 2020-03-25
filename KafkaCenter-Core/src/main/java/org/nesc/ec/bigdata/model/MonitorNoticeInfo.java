package org.nesc.ec.bigdata.model;

import java.util.List;

import org.nesc.ec.bigdata.common.model.OffsetInfo;

/**
 * @author Truman.P.Du
 * @date 2019年4月19日 上午8:26:29
 * @version 1.0
 */
public class MonitorNoticeInfo {
	private AlertGoup alertGoup;
	private String sendType;
	List<OffsetInfo> offsetInfos;


	public MonitorNoticeInfo() {
	}

	public MonitorNoticeInfo(AlertGoup alertGoup, List<OffsetInfo> offsetInfos,String sendType) {
		this.alertGoup = alertGoup;
		this.offsetInfos = offsetInfos;
		this.sendType = sendType;
	}

	public String getSendType() {
		return sendType;
	}

	public void setSendType(String sendType) {
		this.sendType = sendType;
	}

	public AlertGoup getAlertGoup() {
		return alertGoup;
	}

	public void setAlertGoup(AlertGoup alertGoup) {
		this.alertGoup = alertGoup;
	}

	public List<OffsetInfo> getOffsetInfos() {
		return offsetInfos;
	}

	public void setOffsetInfos(List<OffsetInfo> offsetInfos) {
		this.offsetInfos = offsetInfos;
	}

}
