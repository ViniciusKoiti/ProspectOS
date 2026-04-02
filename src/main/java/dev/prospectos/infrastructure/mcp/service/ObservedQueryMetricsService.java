package dev.prospectos.infrastructure.mcp.service;

import java.util.List;

import dev.prospectos.api.mcp.QueryMetricsRecorder;
import dev.prospectos.api.mcp.QueryMetricsService;
import dev.prospectos.api.mcp.QueryTimeWindow;

interface ObservedQueryMetricsService extends QueryMetricsService, QueryMetricsRecorder {

    List<QueryMetricsObservation> observations(QueryTimeWindow timeWindow, String provider);

    List<String> observedProviders(QueryTimeWindow timeWindow);
}
