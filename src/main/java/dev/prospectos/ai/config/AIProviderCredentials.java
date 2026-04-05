package dev.prospectos.ai.config;

public record AIProviderCredentials(
    String openaiKey,
    String anthropicKey,
    String groqKey
) {

    public boolean hasValidOpenAiKey() {
        return isValidApiKey(openaiKey);
    }

    public boolean hasValidAnthropicKey() {
        return isValidApiKey(anthropicKey);
    }

    public boolean hasValidGroqKey() {
        return isValidApiKey(groqKey);
    }

    private boolean isValidApiKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        String trimmedKey = key.trim();
        return !trimmedKey.equals("test-key")
            && !trimmedKey.equals("dummy-key")
            && !trimmedKey.equals("fake-key")
            && !trimmedKey.equals("mock-key")
            && !trimmedKey.startsWith("sk-test-")
            && !trimmedKey.matches("(?i)test.*|mock.*|fake.*|dummy.*|dev.*");
    }
}
