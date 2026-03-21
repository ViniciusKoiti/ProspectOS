package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.api.dto.LeadSearchAsyncSnapshotResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory SSE emitter hub for async lead search updates.
 */
@Component
class LeadSearchAsyncSseHub {

    private static final long NO_TIMEOUT = 0L;

    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    SseEmitter subscribe(UUID requestId, LeadSearchAsyncSnapshotResponse snapshot) {
        SseEmitter emitter = new SseEmitter(NO_TIMEOUT);
        emitters.computeIfAbsent(requestId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeEmitter(requestId, emitter));
        emitter.onTimeout(() -> removeEmitter(requestId, emitter));
        sendSnapshot(emitter, snapshot);
        return emitter;
    }

    void publish(LeadSearchAsyncSnapshotResponse snapshot) {
        List<SseEmitter> requestEmitters = emitters.get(snapshot.requestId());
        if (requestEmitters == null || requestEmitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : requestEmitters) {
            sendSnapshot(emitter, snapshot);
        }
    }

    void complete(UUID requestId) {
        List<SseEmitter> requestEmitters = emitters.remove(requestId);
        if (requestEmitters == null) {
            return;
        }
        for (SseEmitter emitter : requestEmitters) {
            emitter.complete();
        }
    }

    private void sendSnapshot(SseEmitter emitter, LeadSearchAsyncSnapshotResponse snapshot) {
        try {
            emitter.send(
                SseEmitter.event()
                    .name("snapshot")
                    .id(snapshot.updatedAt().toString())
                    .data(snapshot)
            );
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
    }

    private void removeEmitter(UUID requestId, SseEmitter emitter) {
        List<SseEmitter> requestEmitters = emitters.get(requestId);
        if (requestEmitters == null) {
            return;
        }
        requestEmitters.remove(emitter);
        if (requestEmitters.isEmpty()) {
            emitters.remove(requestId);
        }
    }
}
