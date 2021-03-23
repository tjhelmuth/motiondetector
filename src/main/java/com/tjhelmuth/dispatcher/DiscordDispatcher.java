package com.tjhelmuth.dispatcher;

import com.tjhelmuth.AlertEngine;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Send an alert to a discord channel using the webhooks api
 */
@Slf4j
public class DiscordDispatcher implements AlertDispatcher {
    private static final HttpClient http = HttpClients.createDefault();
    private final String webhookUrl;

    public DiscordDispatcher(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void dispatch(Instant timestamp, byte[] media) {
        try {
            HttpPost upload = new HttpPost(webhookUrl);
            MultipartEntityBuilder formBuilder = MultipartEntityBuilder.create();
            formBuilder.addBinaryBody("file", media, ContentType.APPLICATION_OCTET_STREAM, String.format("%s.png", DateTimeFormatter.ISO_INSTANT.format(timestamp)));
            upload.setEntity(formBuilder.build());

            var response = http.execute(upload);
            upload.releaseConnection();
            log.debug("Discord alert response code {}", response.getStatusLine());
        } catch (Exception e){
            log.error("Error sending discord alert", e);
        }
    }

    @Override
    public AlertEngine.MediaType getMediaType() {
        return AlertEngine.MediaType.IMAGE;
    }
}
