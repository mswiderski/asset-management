package org.kie.asset.management.model;

import java.io.Serializable;
import java.util.Date;

public class CommitInfo implements Serializable {

    private String commitId;
    private String message;
    private String author;
    private Date commitDate;

    public CommitInfo(String commitId, String message, String author, Date commitDate) {
        this.commitId = commitId;
        this.message = message;
        this.author = author;
        this.commitDate = commitDate;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCommitDate() {
        return commitDate;
    }

    public void setCommitDate(Date commitDate) {
        this.commitDate = commitDate;
    }

    @Override public String toString() {
        return "CommitInfo{" +
                "commitId='" + commitId + '\'' +
                ", message='" + message + '\'' +
                ", author='" + author + '\'' +
                ", commitDate=" + commitDate +
                '}';
    }
}
