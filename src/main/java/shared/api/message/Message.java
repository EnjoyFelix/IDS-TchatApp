package shared.api.message;

import java.io.Serializable;
import java.time.Instant;

public record Message(String username, String message, Instant date) implements Serializable {};
