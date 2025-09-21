package com.example.civicsense.service;

import com.example.civicsense.model.ReportDraft;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    public String handleMessage(ReportDraft draft, String message) {

        switch (draft.getState()) {
            case "AWAITING_ISSUE_TYPE":
                draft.setIssueType(message);
                draft.setState("AWAITING_LOCATION");
                return "Got it! Please share the location of the issue.";
            case "AWAITING_LOCATION":
                draft.setLocation(message);
                draft.setState("AWAITING_DESCRIPTION");
                return "Please provide more details about the issue.";
            case "AWAITING_DESCRIPTION":
                draft.setDescription(message);
                draft.setState("AWAITING_CONTACT");
                return "Finally, please provide your contact details (name + phone/email).";
            case "AWAITING_CONTACT":
                draft.setContact(message);
                draft.setState("CONFIRMED");
                return "Thank you! Your report has been submitted. Our team will look into it and contact you if needed.";
            default:
                draft.setState("AWAITING_ISSUE_TYPE");
                return "Hi! I'm Civi, your civic assistant. I can help you report city issues or get information about city services.\nPlease select the issue type (Pothole, Garbage, Streetlight, Water, Other).";
        }
    }
}
