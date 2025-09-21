package com.example.civicsense.controller;

import com.example.civicsense.model.Report;
import com.example.civicsense.model.ReportDraft;
import com.example.civicsense.service.AiService;
import com.example.civicsense.service.ConversationService;
import com.example.civicsense.service.MediaService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/twilio/whatsapp")
public class WebhookController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private AiService aiService;

    @Autowired
    private MediaService mediaService;

    private final Map<String, ReportDraft> draftMemory = new HashMap<>();
    private final Map<String, List<Report>> reportMemory = new HashMap<>();

    @Value("${TWILIO_ACCOUNT_SID}")
    private String twilioSid;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String twilioAuthToken;

    @Value("${TWILIO_PHONE_NUMBER}")
    private String twilioPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(twilioSid, twilioAuthToken);
    }

    @PostMapping
    public void receiveMessage(@RequestParam Map<String, String> body) {

        String from = body.get("From");
        String messageBody = body.getOrDefault("Body", "").trim();
        String numMediaStr = body.getOrDefault("NumMedia", "0");
        int numMedia = 0;
        try { numMedia = Integer.parseInt(numMediaStr); } catch (Exception ignored) {}

        if (!draftMemory.containsKey(from)) {
            draftMemory.put(from, new ReportDraft());
            draftMemory.get(from).setPhone(from);
            draftMemory.get(from).setState(null);
            draftMemory.get(from).setUpdatedAt(LocalDateTime.now());
        }
        ReportDraft draft = draftMemory.get(from);

        // Handle media if any
        List<File> uploadedFiles = new ArrayList<>();
        if (numMedia > 0) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
            for (int i = 0; i < numMedia; i++) {
                try {
                    String mediaUrl = body.get("MediaUrl" + i);
                    String contentType = body.get("MediaContentType" + i);

                    String ext = "bin";
                    if (contentType != null) {
                        if (contentType.contains("jpeg") || contentType.contains("jpg")) ext = "jpg";
                        else if (contentType.contains("png")) ext = "png";
                        else if (contentType.contains("gif")) ext = "gif";
                        else if (contentType.contains("mp4")) ext = "mp4";
                        else if (contentType.contains("mov")) ext = "mov";
                        else if (contentType.contains("pdf")) ext = "pdf";
                        else if (contentType.contains("mpeg")) ext = "mp3";
                    }

                    String filename = LocalDateTime.now().format(fmt) + "_" + i + "." + ext;
                    File tempFile = new File(System.getProperty("java.io.tmpdir"), filename);

                    try (InputStream in = new URL(mediaUrl).openStream();
                         FileOutputStream fos = new FileOutputStream(tempFile)) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            fos.write(buffer, 0, read);
                        }
                    }
                    uploadedFiles.add(tempFile);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!uploadedFiles.isEmpty()) {
                List<String> mediaUrls = mediaService.uploadFiles(uploadedFiles, from);
                draft.setMediaUrls(String.join(",", mediaUrls));
                sendMessage(from, "Thanks! Received your attachment(s). Continue with the report.");
            }
        }

        // Handle structured flow or fallback
        String reply;
        if (draft.getState() == null || !Arrays.asList("AWAITING_ISSUE_TYPE","AWAITING_LOCATION","AWAITING_DESCRIPTION","AWAITING_CONTACT","CONFIRMED").contains(draft.getState())) {
            // Start conversation
            reply = conversationService.handleMessage(draft, messageBody);
        } else if (draft.getState().equals("CONFIRMED")) {
            // Save report
            Report report = new Report();
            report.setPhone(draft.getPhone());
            report.setIssueType(draft.getIssueType());
            report.setLocation(draft.getLocation());
            report.setDescription(draft.getDescription());
            report.setContact(draft.getContact());
            report.setMediaUrls(draft.getMediaUrls());
            report.setSubmittedAt(LocalDateTime.now());

            reportMemory.computeIfAbsent(from, k -> new ArrayList<>()).add(report);

            draftMemory.put(from, new ReportDraft()); // reset draft
            reply = "Report saved successfully. You can report another issue or ask a question.";
        } else {
            // Continue structured conversation
            reply = conversationService.handleMessage(draft, messageBody);
        }

        // If user typed random question outside flow, fallback to AI
        if (draft.getState() == null || draft.getState().equals("CONFIRMED")) {
            if (!messageBody.isBlank()) {
                String aiResponse = aiService.askAi(messageBody);
                if (!aiResponse.isBlank()) {
                    reply += "\n\n" + aiResponse;
                }
            }
        }

        sendMessage(from, reply);
    }

    private void sendMessage(String to, String message) {
        try {
            Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioPhoneNumber),
                    message
            ).create();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
