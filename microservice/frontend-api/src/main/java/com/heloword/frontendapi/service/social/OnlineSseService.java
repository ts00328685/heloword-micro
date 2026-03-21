package com.heloword.frontendapi.service.social;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.heloword.frontendapi.model.response.OnlineUserDto;

@Log4j2
@Service
public class OnlineSseService {

  /** userId → one or more open SSE connections (multiple tabs / devices) */
  private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> userEmitters =
      new ConcurrentHashMap<>();

  // ── Lifecycle ────────────────────────────────────────────────────────────

  public SseEmitter createEmitterForUser(String userId) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

    Runnable cleanup = () -> removeEmitter(userId, emitter);
    emitter.onCompletion(cleanup);
    emitter.onTimeout(() -> { cleanup.run(); emitter.complete(); });
    emitter.onError(e -> cleanup.run());
    return emitter;
  }

  private void removeEmitter(String userId, SseEmitter emitter) {
    CopyOnWriteArrayList<SseEmitter> list = userEmitters.get(userId);
    if (list != null) {
      list.remove(emitter);
      if (list.isEmpty()) userEmitters.remove(userId);
    }
  }

  // ── Broadcasting ─────────────────────────────────────────────────────────

  /** Send the updated online-users list to every connected client. */
  public void broadcastOnlineUsers(List<OnlineUserDto> onlineUsers) {
    SseEmitter.SseEventBuilder event = SseEmitter.event()
        .name("online-users")
        .data(onlineUsers);
    int dead = 0;
    for (Map.Entry<String, CopyOnWriteArrayList<SseEmitter>> entry : userEmitters.entrySet()) {
      dead += sendToList(entry.getValue(), event);
    }
    log.debug("Broadcast online-users to {} users ({} dead removed)",
        userEmitters.size(), dead);
  }

  /** Send an event to a specific user's connections only. */
  public void sendToUser(String userId, SseEmitter.SseEventBuilder event) {
    CopyOnWriteArrayList<SseEmitter> list = userEmitters.get(userId);
    if (list != null) sendToList(list, event);
  }

  /** @return number of dead emitters removed */
  private int sendToList(CopyOnWriteArrayList<SseEmitter> emitters,
      SseEmitter.SseEventBuilder event) {
    List<SseEmitter> dead = new ArrayList<>();
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(event);
      } catch (Exception e) {
        dead.add(emitter);
      }
    }
    emitters.removeAll(dead);
    return dead.size();
  }
}
