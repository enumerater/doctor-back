package com.enumerate.disease_detection.MVC.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.CompressedHistoryPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CompressedHistoryMapper extends BaseMapper<CompressedHistoryPO> {
}
