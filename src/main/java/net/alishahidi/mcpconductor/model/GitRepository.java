package net.alishahidi.mcpconductor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitRepository {
    private String path;
    private String remoteUrl;
    private String currentBranch;
    private String currentCommit;
    private List<String> branches;
    private List<String> tags;
    private boolean hasUncommittedChanges;
    private List<String> modifiedFiles;
    private List<String> untrackedFiles;
    private LocalDateTime lastFetch;
    private String lastCommitMessage;
    private String lastCommitAuthor;
}