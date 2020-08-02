package io.reflectoring.module2;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
class Module2Controller {

    @GetMapping("/module2")
    ModelAndView hello() {
        Map<String, Object> model = new HashMap<>();
        model.put("module", "Module 2");
        return new ModelAndView("module2.html", model);
    }

}
