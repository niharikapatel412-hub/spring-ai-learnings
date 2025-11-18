package com.example.niharika.spring.ai.rag;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/web")
    public String chatPage() {
        return "chat";
    }

    @GetMapping("/ragweb")
    public String ragweb() {
        return "rag";
    }
}