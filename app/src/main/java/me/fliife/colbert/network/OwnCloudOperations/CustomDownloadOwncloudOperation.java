package me.fliife.colbert.network.OwnCloudOperations;

import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;

public class CustomDownloadOwncloudOperation extends DownloadRemoteFileOperation {

    private String remotePath;

    public CustomDownloadOwncloudOperation(String remotePath, String localFolderPath) {
        super(remotePath, localFolderPath);
        this.remotePath = remotePath;
    }


    public String getRemotePath() {
        return remotePath;
    }
}