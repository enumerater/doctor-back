package com.enumerate.disease_detection.MVC.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.VectorStorePO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VectorStoreMapper extends BaseMapper<VectorStorePO> {
}
