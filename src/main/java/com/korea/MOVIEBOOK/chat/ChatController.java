package com.korea.MOVIEBOOK.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("chat")
@RequiredArgsConstructor
public class ChatController {


    @GetMapping("enter")
    public String enter(@RequestParam("username") String username, Model model) {
        model.addAttribute("username", username);
        return "chat";
    }
}