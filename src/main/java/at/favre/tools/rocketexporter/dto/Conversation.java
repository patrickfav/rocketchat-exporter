package at.favre.tools.rocketexporter.dto;

public interface Conversation {
    String get_id();

    String getName();

    final class AllConversations implements Conversation {
        @Override
        public String get_id() {
            return null;
        }

        @Override
        public String getName() {
            return "[ALL]";
        }
    }
}
