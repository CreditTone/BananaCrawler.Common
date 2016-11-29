package banana.core.protocol;



import org.apache.hadoop.ipc.VersionedProtocol;

import banana.core.NodeStatus;
import banana.core.exception.DownloadException;

public interface DownloadProtocol extends VersionedProtocol {
	
	public static final long versionID = 1L;
	
	boolean startDownloadTracker(String taskId,int thread,Task config) throws DownloadException;
	
	void resubmitTaskConfig(String taskId,int thread,Task config) throws DownloadException;
	
	boolean isWaitRequest(String taskId) throws DownloadException;
	
	void stopDownloadTracker(String taskId) throws DownloadException;
	
	void stopDownloader() throws DownloadException;
	
	NodeStatus healthCheck() throws DownloadException;
}
