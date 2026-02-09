package com.vms.vendor_management_system.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Root controller that redirects to the login page
 */
@Controller
public class RootController {

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }
}
