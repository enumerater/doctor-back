package com.enumerate.disease_detection.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enumerate.disease_detection.POJO.PO.CropsPO;
import com.enumerate.disease_detection.POJO.VO.CropListVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CropsMapper extends BaseMapper<CropsPO> {
    List<CropListVO> getCrops();
}
