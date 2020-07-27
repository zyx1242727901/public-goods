package com.xh.publicgoods.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestConnect {

    @RequestMapping("/testConnect")
    public String testConnect() {
        return "success";
    }
}
