# CivicSense - WhatsApp AI Chatbot (Civi)

Civi is a production-ready WhatsApp chatbot for civic issue reporting and city service information.

## Features

- Friendly structured conversation for reporting issues
- Media support (images, videos, documents)
- AI FAQ fallback powered by OpenAI GPT-4
- Media stored securely in AWS S3
- Optional placeholder for vision analysis (image classification)

## Project Structure

CivicSense/
├── pom.xml
├── .gitignore
├── README.md
├── .env.example
└── src/
└── main/java/com/example/civicsense/
├── CivicSenseApplication.java
├── controller/WebhookController.java
├── service/
│ ├── ConversationService.java
│ ├── AiService.java
│ └── MediaService.java
└── model/
├── Report.java
└── ReportDraft.java
