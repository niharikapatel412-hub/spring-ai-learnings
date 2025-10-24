package com.example.niharika.spring.ai.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ImageToTextController {

    private final ChatClient chatClient;

    @Value("classpath:static/images.jpeg")
    Resource image;

    public ImageToTextController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/image-to-text")
    public String describeImage(){
        String prompt = "Describe the image in detail.";
        return chatClient
                .prompt()
                .user(e->{
                    e.text(prompt);
                    e.media(MimeTypeUtils.IMAGE_JPEG, image);
                })
                .call()
                .content();

    }
}
