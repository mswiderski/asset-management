package org.kie.asset.management.model;

import java.io.Serializable;

public class FileInfo implements Serializable {

	private static final long serialVersionUID = -7265680195223943876L;

	private String name; 
	private String path;
	private long size;
	private int mode;
	private String objectId;
	private String commitId; 
	private String type;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getCommitId() {
		return commitId;
	}
	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "FileInfo [name=" + name + ", path=" + path + ", size=" + size
				+ ", mode=" + mode + ", objectId=" + objectId + ", commitId="
				+ commitId + ", type=" + type + "]";
	}
	
	public static FileInfo build(String name, String path, long size, int mode, String objectId,
			String commitId, String type) {
		FileInfo fileInfo = new FileInfo();
		fileInfo.setName(name);
		fileInfo.setPath(path);
		fileInfo.setSize(size);
		fileInfo.setMode(mode);
		fileInfo.setObjectId(objectId);
		fileInfo.setCommitId(commitId);
		fileInfo.setType(type);
		
		return fileInfo;
	}
}
