package billy.model.telegram;

import billy.data.Chat;
import billy.model.telegram.parsers.UserMessageParser;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;


@Component
@Singleton
public class QASlaveOBot extends TelegramLongPollingBot {

    private final String server = "http://localhost:8084/api/"; //TODO Удалить это и написать нормально
    public int a = 0;
    public SendMessage lastMessage;
    public Update lastUpdate;
    private final UserMessageParser answerCommandParser = new UserMessageParser("answer");

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        lastUpdate = update;
        boolean flag = false;
        try {
            if (update.getMessage().getNewChatMembers().stream().anyMatch(n -> n.getId() == this.getBotId())) {
                flag = true;
            }
            if (!flag) { flag = update.getMessage().getGroupchatCreated(); }
        } catch (Exception ignored) {}
        if (flag) {
            // Логика если бота добавили в группу или создали группу с ним
            SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
            sendMessage.setText("Я не добавился, сорян.");
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Content-Type", "application/json");
            HttpEntity<Chat> request = new HttpEntity<Chat>(Chat.builder()
                    .chat_id(update.getMessage().getChatId())
                    .build(), httpHeaders);
            String s = restTemplate.postForObject(server + "new_orgs_chat", request, String.class);
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
            flag = answerCommandParser.parseMessage(update.getMessage().getText()).isCommand;
        } catch (Exception ignored) {}
        if (flag) {
            // Логика если есть команда /answer в начале
            SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
            if (update.getMessage().isReply()) sendMessage.setText("Спасибо за ответ!");
            else sendMessage.setText("Спасибо за ответ!  К сожалению, я не знаю, к какому именно вопросу" +
                                "он относится. Воспользуйтесь функцией \"Reply\" чтобы отвечать на вопросы.");
            lastMessage = sendMessage;
            execute(sendMessage);
        }
    }

    @SneakyThrows
    public void sendMessage() {
        lastMessage.setText("Кто-то меня потрогал!");
        execute(lastMessage);
    }

    @Override
    public String getBotUsername() {
        return "test25673Bot";
    }

    public int getBotId() {
        return 1389370639;
    }

    @Override
    public String getBotToken() {
        return "1389370639:AAFpHBYG3eBgyrtFFQNh5wyhi-DIjBK5HFY";
    }
}