package net.alishahidi.mcpconductor.exception;

public class RateLimitExceededException extends RuntimeException {

    private final String clientId;
    private final long limit;
    private final long resetTimeMillis;

    public RateLimitExceededException(String clientId, long limit, long resetTimeMillis) {
        super(String.format("Rate limit exceeded for client %s. Limit: %d, Reset in: %d ms",
                clientId, limit, resetTimeMillis));
        this.clientId = clientId;
        this.limit = limit;
        this.resetTimeMillis = resetTimeMillis;
    }

    // Getters
    public String getClientId() { return clientId; }
    public long getLimit() { return limit; }
    public long getResetTimeMillis() { return resetTimeMillis; }
}