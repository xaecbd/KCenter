package org.nesc.ec.bigdata.common;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
/**
 * 
 * @author Jacob.Q.Cao
 * @date 2019年3月29日
 * @version 1.0
 */
@MappedTypes(value = RoleEnum.class)
public class RoleHandler implements TypeHandler<RoleEnum> {

	@Override
	public void setParameter(PreparedStatement ps, int i, RoleEnum parameter,
			JdbcType jdbcType) throws SQLException {
		ps.setByte(i, (byte)parameter.getValue());
	}

	@Override
	public RoleEnum getResult(ResultSet rs, String columnName)
			throws SQLException {
		return RoleEnum.get(rs.getInt(columnName));
	}

	@Override
	public RoleEnum getResult(ResultSet rs, int columnIndex)
			throws SQLException {
		return RoleEnum.get(rs.getInt(columnIndex));
	}

	@Override
	public RoleEnum getResult(CallableStatement cs, int columnIndex)
			throws SQLException {
		return RoleEnum.get(cs.getInt(columnIndex));
	}
}
