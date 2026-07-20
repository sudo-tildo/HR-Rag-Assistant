package sugarcube.rh_rag_assistant.chat;

import org.springframework.ai.chat.client.advisor.api.Advisor;

public class PromptLoggingAdvisor implements Advisor {
    int maxMessages;
    public PromptLoggingAdvisor(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
