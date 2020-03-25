package org.nesc.ec.bigdata.model;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import org.nesc.ec.bigdata.common.RoleEnum;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@TableName("user_info")
public class UserInfo {
    @TableId(value="id",type= IdType.AUTO)
    private Long id;
    private String name;
    @TableField(value="real_name")
    private String realName;
    private String password;
    private RoleEnum role;
    private String email;
    @TableField(value="create_time")
    private Date createTime;

    @TableField(exist = false)
    private List<Long> teamIDs;
    @TableField(exist = false)
    private String picture;

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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Long> getTeamIDs() {
        return teamIDs;
    }

    public void setTeamIDs(List<Long> teamIDs) {
        this.teamIDs = teamIDs;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, realName, email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; };
        if (o == null || getClass() != o.getClass()) { return false; };
        UserInfo userInfo = (UserInfo) o;
        return 	Objects.equals(name.toLowerCase(), userInfo.name.toLowerCase()) &&
                Objects.equals(realName.toLowerCase(), userInfo.realName.toLowerCase()) &&
                Objects.equals(email.toLowerCase(), userInfo.email.toLowerCase());
    }
}
