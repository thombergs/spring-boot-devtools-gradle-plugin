package io.reflectoring.module1;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
class Module1Controller {

    @GetMapping("/module1")
    ModelAndView hello(){
        Map<String, Object> model = new HashMap<>();
        model.put("module", "Module 1");
        return new ModelAndView("module1.html", model);
    }

}
