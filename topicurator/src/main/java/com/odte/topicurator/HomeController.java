package com.odte.topicurator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public String home() {
        // 이 메시지가 웹 브라우저에 표시됩니다.
        return "<html><body><h1>환영합니다!</h1><p>토픽 큐레이터 애플리케이션이 실행 중입니다.</p></body></html>";
    }
}
