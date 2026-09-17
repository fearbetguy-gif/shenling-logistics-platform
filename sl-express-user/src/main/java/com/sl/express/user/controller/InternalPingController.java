package com.sl.express.user.controller;

import com.sl.express.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 基础设施验证接口，后续业务接口完成后会删除或改为健康检查。 */
@RestController
@RequestMapping("/internal")
public class InternalPingController {

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("sl-express-user is running");
    }
}
