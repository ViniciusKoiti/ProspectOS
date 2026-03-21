package dev.prospectos.infrastructure.service.leads;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * SSE stream subscription contract for async lead search jobs.
 */
public interface LeadSearchAsyncStreamService {

    SseEmitter subscribe(UUID requestId);
}
