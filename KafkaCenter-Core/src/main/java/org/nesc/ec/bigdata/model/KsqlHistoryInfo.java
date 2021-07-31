package org.nesc.ec.bigdata.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import java.util.Date;

@TableName(value = "ksql_history")
public class KsqlHistoryInfo {

    @TableId(type = IdType.AUTO)
    private long id;
    @TableField(value = "name")
    private String name;
    @TableField(value = "ksql_server_id")
    private String ksqlServerId;
    @TableField(value = "user")
    private String user;
    @TableField(value = "date")
    private Date date;
    @TableField(value = "operate")
    private String operate;

    @TableField(value = "type")
    private String type;

    @TableField(value = "script")
    private String script;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKsqlServerId() {
        return ksqlServerId;
    }

    public void setKsqlServerId(String ksqlServerId) {
        this.ksqlServerId = ksqlServerId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }


}
