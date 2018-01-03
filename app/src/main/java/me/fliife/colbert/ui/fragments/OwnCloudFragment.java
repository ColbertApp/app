package me.fliife.colbert.ui.fragments;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;
import me.fliife.colbert.MainActivity;
import me.fliife.colbert.R;
import me.fliife.colbert.network.OwnCloudOperations.CustomDownloadOwncloudOperation;
import me.fliife.colbert.storage.StorageUtils;
import me.fliife.colbert.ui.adapters.OwnCloudAdapter;

import java.io.File;
import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class OwnCloudFragment extends Fragment implements OnRemoteOperationListener, OnDatatransferProgressListener {

    private RecyclerView rv;
    private OwnCloudClient client;
    private ImageView loader;
    private AnimatedVectorDrawableCompat loading;
    private Button reload;
    private NotificationManager notificationManager;

    public OwnCloudFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_own_cloud, container, false);

        client = OwnCloudClientFactory
                .createOwnCloudClient(Uri.parse("https://colbertserv.lyceecolbert-tg.org/owncloud"),
                        getContext(), true);
        client.setCredentials(StorageUtils.getOwnCloudCredentials(getContext()));

        ReadRemoteFolderOperation folderOperation = new ReadRemoteFolderOperation("/");
        folderOperation.execute(client, this, null);

        rv = rootView.findViewById(R.id.owncloud_rv);
        reload = rootView.findViewById(R.id.owncloud_reload_button);
        loader = rootView.findViewById(R.id.oc_loading);
        loading = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.loading);
        loader.setImageDrawable(loading);
        loading.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        loading.start();
                    }
                });
            }
        });
        loading.start();

        showLoading();

        notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        return rootView;
    }

    public void showLoading() {
        rv.post(new Runnable() {
            @Override
            public void run() {
                loader.setImageDrawable(loading);
                rv.setVisibility(GONE);
                loader.setVisibility(VISIBLE);
            }
        });
    }

    public void stopLoading() {
        rv.post(new Runnable() {
            @Override
            public void run() {
                rv.setVisibility(VISIBLE);
                loader.setVisibility(GONE);
            }
        });
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult remoteOperationResult) {
        if(!remoteOperationResult.isSuccess()){
            System.out.println("Not a success");
            if(remoteOperation instanceof ReadRemoteFolderOperation) {
                onFolderLoadingError((ReadRemoteFolderOperation) remoteOperation);
            } else if (remoteOperation instanceof CustomDownloadOwncloudOperation) {
                onDownloadError((CustomDownloadOwncloudOperation) remoteOperation);
            }
            return;
        }
        if(remoteOperation instanceof ReadRemoteFolderOperation) {
            onFolderContentLoaded(remoteOperationResult);
        } else if (remoteOperation instanceof CustomDownloadOwncloudOperation) {
            onFileDownloadFinished((CustomDownloadOwncloudOperation) remoteOperation);
        }
    }

    private void onDownloadError(CustomDownloadOwncloudOperation remoteOperation) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), MainActivity.OWNCLOUD_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_cross)
                .setContentTitle(remoteOperation.getRemotePath().replaceAll("^.*/",""))
                .setContentText("Téléchargement échoué")
                .setDefaults(0);

        System.out.println("error id " + remoteOperation.getRemotePath().replaceAll("^.*/","").hashCode());

        notificationManager.cancel(remoteOperation.getRemotePath().replaceAll("^.*/","").hashCode());
        notificationManager.notify(-remoteOperation.getRemotePath().replaceAll("^.*/","").hashCode(), builder.build());
    }

    private void onFolderLoadingError(final ReadRemoteFolderOperation remoteOperation) {
        reload.post(new Runnable() {
            @Override
            public void run() {
                loader.setImageDrawable(getResources().getDrawable(R.drawable.ic_sentiment_very_dissatisfied));
                reload.setVisibility(VISIBLE);
                reload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        remoteOperation.execute(client, OwnCloudFragment.this, null);
                        showLoading();

                        reload.setVisibility(GONE);
                    }
                });
                rv.setVisibility(GONE);
                loader.setVisibility(VISIBLE);
            }
        });
    }

    private void onFileDownloadFinished(CustomDownloadOwncloudOperation operation) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), MainActivity.OWNCLOUD_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check)
                .setContentTitle(operation.getRemotePath().replaceAll("^.*/", ""))
                .setContentText("Téléchargé !")
                .setDefaults(0);

        Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(operation.getRemotePath().replaceAll("^.*[.]",""));

        File savedFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Colbert" + operation.getRemotePath());
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", savedFile);
        openFileIntent.setDataAndType(uri, mimeType);

        openFileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        builder.setContentIntent(PendingIntent.getActivity(getContext(), 0, openFileIntent, PendingIntent.FLAG_CANCEL_CURRENT));

        notificationManager.cancel(operation.getRemotePath().replaceAll("^.*/", "").hashCode());
        notificationManager.notify(-operation.getRemotePath().replaceAll("^.*/", "").hashCode(), builder.build());
    }

    private void onFolderContentLoaded(final RemoteOperationResult remoteOperationResult) {
        if(remoteOperationResult.isSuccess()) {
            // Run on UI thread
            rv.post(new Runnable() {
                @Override
                public void run() {
                    ArrayList<RemoteFile> files = new ArrayList<>();
                    for(Object remoteFile : remoteOperationResult.getData()) {
                        files.add((RemoteFile) remoteFile);
                        // System.out.println(((RemoteFile) remoteFile).getRemotePath());
                    }
                    rv.setLayoutManager(new LinearLayoutManager(getContext()));
                    rv.setAdapter(new OwnCloudAdapter(files.get(0), files, OwnCloudFragment.this, OwnCloudFragment.this.client, getFragmentManager()));
                    stopLoading();
                }
            });
        }
    }

    @Override
    public void onTransferProgress(long progressRate, long transfered, long total, final String filename) {
        long progress = transfered * 100 / total;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), MainActivity.OWNCLOUD_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_file_download)
                .setContentTitle(filename)
                .setContentText("Téléchargement en cours - " + progress + "%")
                .setDefaults(0);

        System.out.println("error id2 " + filename.hashCode());

        notificationManager.notify(filename.hashCode(), builder.build());
    }

    public void showDialog(String remotePath) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), MainActivity.OWNCLOUD_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_file_download)
                .setContentTitle(remotePath.replaceAll("^.*/", ""))
                .setContentText("Téléchargement en cours")
                .setPriority(NotificationCompat.PRIORITY_LOW);

        notificationManager.notify(remotePath.replaceAll("^.*/", "").hashCode(), builder.build());
    }
}
