package com.participants_bot_service.telegram;

import com.participants_bot_service.data.Chat;
import com.participants_bot_service.data.Question;
import com.participants_bot_service.telegram.parser.UserMessageParser;
import com.participants_bot_service.utils.Poster;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.inject.Singleton;
import java.util.Optional;

@Component
@Singleton
public class QASlavePBot extends TelegramLongPollingBot {

    private final QASlavePBotCredentials credentials = new QASlavePBotCredentials();
    private final String server = "http://localhost:8084/api/"; //TODO Удалить это и написать нормально
    public SendMessage lastMessage = null;
    public Object lastUpdate;
    private final UserMessageParser questionCommandParser = new UserMessageParser("question");

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        lastUpdate = update;
        boolean flag = false;
        try {
            if (update.getMessage().getNewChatMembers().stream().anyMatch(n -> n.getUserName().equals(this.getBotUsername()))) {
                flag = true;
            }
            if (!flag) { flag = update.getMessage().getGroupchatCreated(); }
        } catch (Exception ignored) {} //TODO Написать это через Optional
        if (flag) {
            // Логика если бота добавили в группу или создали группу с ним
            SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
            sendMessage.setText("Я не добавился, сорян.");
            String s = (String) Poster.builder().aClassObject(Chat.class)
                    .aClassReturn(String.class)
                    .object(Chat.builder()
                            .chat_id(update.getMessage().getChatId())
                            .build())
                    .url(server + "new_participants_chat")
                    .build().post();
            sendMessage.setText(s);
            lastMessage = sendMessage;
            try {
                execute(sendMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        try {
            flag = questionCommandParser.parseMessage(update.getMessage().getText()).isCommand;
        } catch (Exception ignored) {} //TODO Написать это через Optional
        if (flag) {
            // Логика если есть команда /question в начале
            SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
            sendMessage.setText("Вопрос не был загружен");
            String s = (String) Poster.builder().aClassObject(Question.class)
                    .aClassReturn(String.class)
                    .object(Question.builder().participants_chat_id(update.getMessage().getChatId())
                            .orgs_chat_id(0)
                            .message_id(update.getMessage().getMessageId())
                            .text(questionCommandParser.text).build())
                    .url(server + "new_question")
                    .build().post();
            sendMessage.setText(s);
            lastMessage = sendMessage;
            execute(sendMessage);
        }
    }

    @SneakyThrows
    public void sendMessage(SendMessage sendMessage) {
        execute(sendMessage);
    }

    @Override
    public String getBotUsername() {
        return credentials.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return credentials.getBotToken();
    }
}
