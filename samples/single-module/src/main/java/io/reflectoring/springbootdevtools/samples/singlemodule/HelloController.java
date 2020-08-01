package io.reflectoring.springbootdevtools.samples.singlemodule;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
class HelloController {

    @GetMapping("/")
    ModelAndView hello() {
        Map<String, Object> model = new HashMap<>();
        model.put("name", "Dave");
        return new ModelAndView("hello.html", model);
    }
}
