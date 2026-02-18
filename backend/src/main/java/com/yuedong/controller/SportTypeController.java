package com.yuedong.controller;

import com.yuedong.common.Result;
import com.yuedong.service.SportTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sport")
public class SportTypeController {

    @Autowired
    private SportTypeService sportTypeService;

    @GetMapping("/list")
    public Result<?> list() {
        return sportTypeService.listEnabled();
    }
}
