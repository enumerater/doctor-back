package com.enumerate.disease_detection.Controller;

import com.enumerate.disease_detection.Common.Result;
import com.enumerate.disease_detection.POJO.DTO.UserDTO;
import com.enumerate.disease_detection.POJO.DTO.UserSessionUpdateDTO;
import com.enumerate.disease_detection.POJO.PO.UserPO;
import com.enumerate.disease_detection.POJO.VO.UserLoginVO;
import com.enumerate.disease_detection.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserDTO userDTO) {
        log.info("=== register controller ===");
        log.info(userDTO.toString());
        String res = userService.register(userDTO);
        return Result.success(res);
    }

    /**
     * 用户登录
     */
    @GetMapping("/login")
    @CrossOrigin
    public Result<UserLoginVO> login(UserDTO userDTO) {
        log.info("=== login controller ===");
        log.info(userDTO.toString());
        UserLoginVO res = userService.login(userDTO);
        return Result.success(res);
    }

    /**
     * 获取用户信息
     */

    @GetMapping("/getById")
    @CrossOrigin
    public Result<UserPO> getById(@RequestParam Long id) {
        log.info("=== getById controller ===");
        log.info(id.toString());
        UserPO res = userService.getById(id);
        return Result.success(res);
    }

    @PutMapping
    @CrossOrigin
    public Result<String> update(@RequestBody UserSessionUpdateDTO userSessionUpdateDTO) {
        log.info("=== update controller ===");
        log.info(String.valueOf(userSessionUpdateDTO));
        userService.updateById(userSessionUpdateDTO);
        return Result.success("更新成功");
    }

}
