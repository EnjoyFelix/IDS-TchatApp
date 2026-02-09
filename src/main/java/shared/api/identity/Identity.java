package shared.api.identity;

import java.io.Serializable;

public record Identity(String username, String token) implements Serializable {}
