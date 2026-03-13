package com.enumerate.disease_detection.MVC.Service;

import com.enumerate.disease_detection.MVC.POJO.PO.PesticideRecordPO;
import com.enumerate.disease_detection.MVC.POJO.PO.FieldNotePO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.Map;

public interface PlotManagementService {
    // 施药管理
    List<PesticideRecordPO> getPesticideRecords(Long plotId);
    void addPesticideRecord(Long plotId, PesticideRecordPO record);
    void updatePesticideEffect(Long recordId, Integer evaluation, String remarks);

    // 田间随笔
    Page<FieldNotePO> getFieldNotes(Long plotId, String month, int page, int pageSize);
    void addFieldNote(Long plotId, FieldNotePO note);
    void deleteFieldNote(Long noteId);

    // AI 生成
    String generateFieldNote(Long plotId, String theme, List<String> keywords, Map<String, Object> context);

    void updatePesticide(String plotId, String id, PesticideRecordPO record);

    void updateFieldNote(String plotId, String id, FieldNotePO record);

    void deletePesticide(Long id);
}
