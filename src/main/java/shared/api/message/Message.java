package shared.api.message;

import java.time.Instant;

public record Message(Instant date, String message, long senderId, long spaceIdentifier){};
