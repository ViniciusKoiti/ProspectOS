package dev.prospectos.ai.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class LlmScrapingLooseFieldParser {

    Map<String, Object> parseLooseFields(String text) {
        Map<String, Object> map = new LinkedHashMap<>();
        putScalar(text, "company_name", map);
        putScalar(text, "description", map);
        putScalar(text, "phone", map);
        putScalar(text, "industry", map);
        putScalar(text, "size", map);
        putArray(text, "emails", map);
        putArray(text, "technologies", map);
        putArray(text, "recent_news", map);
        return map;
    }

    private void putScalar(String text, String key, Map<String, Object> map) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL).matcher(text);
        if (matcher.find()) {
            map.put(key, matcher.group(1).trim());
        }
    }

    private void putArray(String text, String key, Map<String, Object> map) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL).matcher(text);
        if (!matcher.find()) {
            return;
        }
        Matcher quoted = Pattern.compile("\"(.*?)\"", Pattern.DOTALL).matcher(matcher.group(1));
        List<String> values = new ArrayList<>();
        while (quoted.find()) {
            String value = quoted.group(1).trim();
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        if (!values.isEmpty()) {
            map.put(key, values);
        }
    }
}
