package com.enumerate.disease_detection.Controller;


import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.Mapper.AnnouncementMapper;
import com.enumerate.disease_detection.POJO.DTO.AnnouncementGenerateDTO;
import com.enumerate.disease_detection.POJO.DTO.AnnouncementPublishDTO;
import com.enumerate.disease_detection.POJO.PO.AnnouncementPO;
import com.enumerate.disease_detection.POJO.VO.AnnouncementGenerateVO;
import com.enumerate.disease_detection.POJO.VO.AnnouncementPageQueryVO;
import com.enumerate.disease_detection.Service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/announcement")
@Slf4j
@CrossOrigin
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @PostMapping("/generate")
    public Result<AnnouncementGenerateVO> generateAnnouncement(@RequestBody AnnouncementGenerateDTO announcementGenerateDTO) {
        log.info("开始生成公告");

        AnnouncementGenerateVO announcementGenerateVO = announcementService.generateAnnouncement(announcementGenerateDTO);

        return Result.success(announcementGenerateVO);
     }


    @PostMapping("/publish")
    public Result<AnnouncementPO> publishAnnouncement(@RequestBody AnnouncementPublishDTO announcementPublishDTO) {
        log.info("开始发布公告");

        AnnouncementPO res = announcementService.publishAnnouncement(announcementPublishDTO);

        return Result.success(res);
    }

    @GetMapping("/list")
    public Result<AnnouncementPageQueryVO> listAnnouncements(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String type) {
        log.info("开始列出所有公告");
        AnnouncementPageQueryVO res = announcementService.listAnnouncements(page, pageSize, type);
        return Result.success(res);
    }


    @Autowired
    private AnnouncementMapper announcementMapper;

    @DeleteMapping("/{id}")
    public Result<String> deleteAnnouncement(@PathVariable String id) {
        log.info("开始删除公告");
        announcementMapper.deleteById(id);

        return Result.success("删除成功");
    }





}
