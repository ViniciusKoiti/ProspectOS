package dev.prospectos.ai.dto;

public record OutreachMessage(
    String subject,
    String body,
    String channel,
    String tone,
    String[] callsToAction
) {}