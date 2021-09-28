package org.project.currencyconverter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import springfox.documentation.annotations.ApiIgnore;

/**
 * This class redirects to the swagger dashboard where all supported APIs are shown
 */
@Controller
@ApiIgnore
public class HomeController
{

    @RequestMapping("/")
    public String home()
    {
        return "redirect:/swagger-ui/";
    }

}
