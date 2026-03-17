package com.enumerate.disease_detection.MVC.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enumerate.disease_detection.MVC.POJO.PO.UserReminderPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserReminderMapper extends BaseMapper<UserReminderPO> {

    List<UserReminderPO> selectAllEnabledTasks();
}
