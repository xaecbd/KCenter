package org.nesc.ec.bigdata.mapper;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.nesc.ec.bigdata.model.KsqlHistoryInfo;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface KsqlHistoryMapper extends BaseMapper<KsqlHistoryInfo> {
}
