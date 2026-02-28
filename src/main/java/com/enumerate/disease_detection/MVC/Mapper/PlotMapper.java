package com.enumerate.disease_detection.MVC.Mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.FarmPO;
import com.enumerate.disease_detection.MVC.POJO.PO.PlotPO;
import com.enumerate.disease_detection.MVC.POJO.VO.PlotVOO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PlotMapper extends BaseMapper<PlotPO> {
}
