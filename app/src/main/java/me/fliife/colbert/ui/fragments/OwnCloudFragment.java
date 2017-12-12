package me.fliife.colbert.ui.fragments;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.authentication.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;
import me.fliife.colbert.R;
import me.fliife.colbert.storage.StorageUtils;
import me.fliife.colbert.ui.adapters.OwnCloudAdapter;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class OwnCloudFragment extends Fragment implements OnRemoteOperationListener {

    private RecyclerView rv;
    private OwnCloudClient client;
    private ImageView loader;

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
        loader = rootView.findViewById(R.id.oc_loading);
        final AnimatedVectorDrawableCompat loading = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.loading);
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

        return rootView;
    }

    public void showLoading() {
        rv.setVisibility(GONE);
        loader.setVisibility(VISIBLE);
    }

    public void stopLoading() {
        rv.setVisibility(VISIBLE);
        loader.setVisibility(GONE);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult remoteOperationResult) {
        if(remoteOperation instanceof ReadRemoteFolderOperation) {
            onFolderContentLoaded(remoteOperationResult);
        }
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
                        System.out.println(((RemoteFile) remoteFile).getRemotePath());
                    }
                    rv.setLayoutManager(new LinearLayoutManager(getContext()));
                    rv.setAdapter(new OwnCloudAdapter(files.get(0), files, OwnCloudFragment.this, OwnCloudFragment.this.client));
                    stopLoading();
                }
            });
        }
    }
}
