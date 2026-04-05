package dev.prospectos.ai.client.impl;

import java.util.Arrays;
import java.util.Map;

public final class SpringAIToolResolver {

    private final Map<String, Object> toolBeans;

    public SpringAIToolResolver(Map<String, Object> toolBeans) {
        this.toolBeans = Map.copyOf(toolBeans);
    }

    public static SpringAIToolResolver empty() {
        return new SpringAIToolResolver(Map.of());
    }

    public Object[] resolve(String... functionNames) {
        if (functionNames == null || functionNames.length == 0) {
            return new Object[0];
        }
        return Arrays.stream(functionNames)
            .map(toolBeans::get)
            .filter(java.util.Objects::nonNull)
            .toArray();
    }
}
