package com.enumerate.disease_detection.POJO.VO;

import com.enumerate.disease_detection.POJO.PO.AnnouncementPO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementPageQueryVO {
    private List<AnnouncementPO> list;

    private Integer total;

}
