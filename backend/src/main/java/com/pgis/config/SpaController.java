package com.pgis.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({"/device-info/{path:[^.]*}", "/device-location/{path:[^.]*}"})
    public String spa() {
        return "forward:/index.html";
    }
}
