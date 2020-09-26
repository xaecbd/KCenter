package org.nesc.ec.bigdata.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
@TableName("team_info")
public class TeamInfo {
	@TableId(value="id",type=IdType.AUTO)
	private Long id;
	private String name;
	private String own;
	@TableField(value="alarm_group")
	private String alarmGroup;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOwn() {
		return own;
	}
	public void setOwn(String own) {
		this.own = own;
	}

	public String getAlarmGroup() {
		return alarmGroup;
	}

	public void setAlarmGroup(String alarmGroup) {
		this.alarmGroup = alarmGroup;
	}
}
