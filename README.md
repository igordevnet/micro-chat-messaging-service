<div align="center">

# 💬 Micro-Chat: Messaging & Real-Time Signaling Service

**The asynchronous event engine powering WebRTC signaling, live chat messaging, read receipts, and user presence via Spring Boot WebSockets and RabbitMQ.**

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-brightgreen?style=for-the-badge&logo=spring-boot)](#)
[![WebSockets](https://img.shields.io/badge/STOMP-WebSockets-blue?style=for-the-badge)](#)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Message_Broker-FF6600?style=for-the-badge&logo=rabbitmq)](#)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Relational_DB-4169E1?style=for-the-badge&logo=postgresql)](#)
[![MongoDB](https://img.shields.io/badge/MongoDB-Document_DB-47A248?style=for-the-badge&logo=mongodb)](#)
[![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=for-the-badge&logo=redis)](#)
[![MinIO](https://img.shields.io/badge/MinIO-S3_Storage-C72C48?style=for-the-badge&logo=minio)](#)

</div>

<br/>

## 🏗️ Architecture Overview

This service implements a CQRS-style real-time architecture:
1. **Commands (Inbound):** Clients send messages via STOMP `SEND` frames to application-level routes (typically prefixed with `/app`).
2. **Events (Outbound):** The backend processes business logic, saves to the database, and publishes events to RabbitMQ. RabbitMQ then broadcasts these events to STOMP `SUBSCRIBE` channels (prefixed with `/topic` or `/queue`).

### 🗄️ Polyglot Persistence & Storage Strategy
To handle the unique demands of a real-time chat application, this service distributes data across specialized databases:
*   **PostgreSQL:** Handles strongly-typed, relational entities (Chat Rooms, Participants, Friendships, and User relations).
*   **MongoDB:** Stores the high-volume chat history (`Message` documents) for fast, schema-less horizontal scaling.
*   **Redis:** Acts as an aggressive caching layer for paginated message history and user chat lists to relieve database I/O.
*   **MinIO (S3 Compatible):** Object storage for media processing. Stores file attachments and audio messages, serving them to clients via securely generated Pre-Signed URLs.

### 🔐 Authentication
All WebSocket connections must be authenticated. Clients must pass their `Bearer` JWT token during the initial STOMP `CONNECT` frame. The backend intercepts this to securely populate the `Principal` for all subsequent socket interactions.

---

## ⚙️ Environment Variables

Configure these variables in your deployment environment to connect to the infrastructure.

| Variable | Description | Default Fallback |
| :--- | :--- | :--- |
| **Database: PostgreSQL** | | |
| `SPRING_DATASOURCE_URL` | JDBC connection string | `jdbc:postgresql://localhost:5432/db_micro_chat` |
| `SPRING_DATASOURCE_USERNAME` | Postgres user | `user` |
| `SPRING_DATASOURCE_PASSWORD` | Postgres password | `password` |
| **Database: MongoDB** | | |
| `MONGO_URI` | Mongo connection string | `mongodb://admin:password@localhost:27017/chat_messages_db?authSource=admin` |
| **Cache: Redis** | | |
| `SPRING_DATA_REDIS_HOST` | Redis host | `localhost` |
| **Broker: RabbitMQ** | | |
| `SPRING_RABBITMQ_HOST` | RabbitMQ host | `localhost` |
| `SPRING_RABBITMQ_CLIENT_USERNAME` | STOMP client user | `guest` |
| `SPRING_RABBITMQ_CLIENT_PASSWORD` | STOMP client pass | `guest` |
| `SPRING_RABBITMQ_SYSTEM_USERNAME` | System admin user | `guest` |
| `SPRING_RABBITMQ_SYSTEM_PASSWORD` | System admin pass | `guest` |
| **Storage: MinIO (S3)** | | |
| `MINIO_URL` | Object storage endpoint | `http://localhost:9000` |
| `MINIO_KEY` | Access Key | `admin` |
| `MINIO_SECRET` | Secret Key | `password` |
| `STORAGE_BUCKET` | Target bucket name | `chat-attachments` |
| **Security** | | |
| `JWT_SECRET` | Base64 encoded secret | `404E...` |

---

## 📡 1. Subscriptions (Listening for Events)
Clients must subscribe to the following destinations to receive real-time updates pushed by the RabbitMQ `ChatListener`.

| Destination Type | STOMP Topic / Queue | What you receive | Description |
| :--- | :--- | :--- | :--- |
| **Chat Room** | `/topic/chat.{chatId}` | `MessageResponse`<br/>`ReadReceiptEvent`<br/>`MessageDeletedEvent` | Multi-purpose channel for a specific chat. Receives new messages, read receipts, and deletion alerts. |
| **Push Notifications** | `/topic/notification.{userId}` | `NotificationResponse` | Cross-service system alerts (e.g., "New Message" popups) originating from the Notification Microservice. |
| **WebRTC Signaling** | `/queue/signaling.{userId}` | `SignalingPayload` | Receives direct P2P connection data (Offers, Answers, ICE Candidates, Hang-ups) targeted at your specific user ID. |
| **User Presence** | `/topic/presence.{userId}` | `UserStatusEvent` | Real-time online/offline status updates for users on your accepted friends list. |
| **Friendships** | `/queue/user.{userId}` | `FriendshipResponse` | Receives incoming friend requests or updates to existing friendship statuses. |

---

## ✉️ 2. Publishing (Sending Commands)
Clients use the STOMP `SEND` command to hit these endpoints. *(Note: Assuming the standard Spring STOMP application prefix `/app` is configured in your WebSocketRegistry).*

### 💬 Chat Operations

**1. Send a Text Message**
* **Destination:** `/app/chat/{chatId}/sendMessage`
* **Payload:** `SendMessageRequest`
```json
{
  "content": "Hello world!",
  "createdAt": "2026-05-08T10:00:00",
  "messageType": "TEXT"
}
```
**2. Edit a Message**

   **Destination:** /app/chat/{chatId}/editMessage

   **Payload:** EditMessageRequest

```json
{
  "id": "msg-uuid-here",
  "content": "Edited text content"
}
```

**3. Delete a Message**

   **Destination:** /app/chat/{chatId}/deleteMessage

   **Payload:** String (Just the raw Message ID)

```plaintext

 msg-uuid-here
```

**4. Send a Read Receipt**

   **Destination:** /app/chat/{chatId}/read

   **Payload:** None required. (The backend securely extracts your User ID from your JWT Principal to mark the chat as read).

📞 WebRTC Signaling Operations

**1. Send WebRTC Signal**

   **Destination:** /app/call/signaling

   **Payload:** SignalingRequest

```JSON

{
  "type": "OFFER",
  "chatId": "chat-uuid-here",
  "targetId": 12345,
  "data": "sdp-or-ice-candidate-payload-string"
}
```

(Valid Signaling Types: OFFER, ANSWER, ICE_CANDIDATE, HANG_UP, REJECTED, MISSED)
## 🛠️ Typical Frontend Flow (Example)

    Connect: Client opens WebSocket and sends CONNECT with JWT.

    Subscribe (Global): Client subscribes to /queue/signaling.{myUserId}, /queue/user.{myUserId}, and /topic/presence.{myUserId}.

    Open Chat View: User opens Chat A. Client subscribes to /topic/chat.{chatIdA}.

    Mark as Read: Client sends an empty message to /app/chat/{chatIdA}/read.

    Send Message: Client sends SendMessageRequest to /app/chat/{chatIdA}/sendMessage.

    Receive Echo: RabbitMQ processes it and broadcasts MessageResponse back to /topic/chat.{chatIdA}. Client renders the new message.