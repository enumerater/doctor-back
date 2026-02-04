package com.enumerate.disease_detection.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enumerate.disease_detection.POJO.DTO.DiseaseRecordDTO;
import com.enumerate.disease_detection.POJO.VO.DiseaseRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiseaseRecordMapper extends BaseMapper<DiseaseRecordDTO> {

    List<DiseaseRecordVO> getDiseaseRecord(@Param("user_id") Long userId);
}
