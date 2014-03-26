package org.kie.asset.management.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class CommitInfo implements Serializable {

	private static final long serialVersionUID = -6255022381087425142L;
	private String commitId;
    private String message;
    private String author;
    private Date commitDate;
    
    private List<FileInfo> filesInCommit;

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
                "\n"+
                filesInCommit +
                '}';
    }

	public List<FileInfo> getFilesInCommit() {
		return filesInCommit;
	}

	public void setFilesInCommit(List<FileInfo> filesInCommit) {
		this.filesInCommit = filesInCommit;
	}
}
