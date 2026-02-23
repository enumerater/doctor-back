package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.POJO.PO.UserPO;
import com.enumerate.disease_detection.POJO.VO.AdminKnowledgeListVO;
import com.enumerate.disease_detection.POJO.VO.AdminStatsVO;
import com.enumerate.disease_detection.POJO.VO.AdminUserListVO;
import com.enumerate.disease_detection.Service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Slf4j
@CrossOrigin
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/stats")
    public Result<AdminStatsVO> stats() {
        log.info("admin stats");
        AdminStatsVO result = adminService.getStats();
        return Result.success(result);
    }

    @GetMapping("/users")
    public Result<AdminUserListVO> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role) {
        log.info("admin users: keyword={}, status={}, role={}", keyword, status, role);
        AdminUserListVO result = adminService.getUserList(keyword, status, role);
        return Result.success(result);
    }

    @PutMapping("/users/{id}/toggle-status")
    public Result<UserPO> toggleUserStatus(@PathVariable Long id) {
        log.info("toggleUserStatus: {}", id);
        UserPO result = adminService.toggleUserStatus(id);
        return Result.success(result);
    }
}
