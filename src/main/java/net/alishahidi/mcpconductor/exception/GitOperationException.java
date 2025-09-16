package net.alishahidi.mcpconductor.exception;

public class GitOperationException extends RuntimeException {

    private final String repository;
    private final String branch;
    private final GitOperation operation;

    public enum GitOperation {
        CLONE, PULL, PUSH, CHECKOUT, COMMIT, MERGE, FETCH, STATUS
    }

    public GitOperationException(String message, String repository, GitOperation operation) {
        super(String.format("[Git %s] %s - Repo: %s", operation, message, repository));
        this.repository = repository;
        this.branch = null;
        this.operation = operation;
    }

    public GitOperationException(String message, String repository, String branch, GitOperation operation, Throwable cause) {
        super(String.format("[Git %s] %s - Repo: %s, Branch: %s", operation, message, repository, branch), cause);
        this.repository = repository;
        this.branch = branch;
        this.operation = operation;
    }

    // Getters
    public String getRepository() { return repository; }
    public String getBranch() { return branch; }
    public GitOperation getOperation() { return operation; }
}