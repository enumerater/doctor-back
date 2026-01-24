package com.enumerate.disease_detection.Utils;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

// 必须加@Component让Spring扫描到
@Component
public class TimeAutoFillHandler implements MetaObjectHandler {

    // 插入操作时填充字段
    @Override
    public void insertFill(MetaObject metaObject) {
        // 填充创建时间（仅插入时生效）
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // 填充更新时间（插入时也需要设置）
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    // 更新操作时填充字段
    @Override
    public void updateFill(MetaObject metaObject) {
        // 仅更新时填充更新时间
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}