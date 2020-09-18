package controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import telegram.BotApplication;
import telegram.MasterBot;

@RestController
@RequestMapping("/api/bot/")
public class BotController {

    @Autowired
    public MasterBot qaMasterBot;

    @GetMapping("hello")
    public String hello(){
        return "Hi, i'm a QAMasterBotController";
    }

    @GetMapping("last_update")
    public String last_update_o() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(qaMasterBot.lastUpdate);
    }

    @GetMapping("last_message")
    public String last_message() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(qaMasterBot.lastMessage);
    }
}
