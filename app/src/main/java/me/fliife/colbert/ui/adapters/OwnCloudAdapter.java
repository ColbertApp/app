package me.fliife.colbert.ui.adapters;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;
import me.fliife.colbert.R;
import me.fliife.colbert.network.OwnCloudOperations.CustomDownloadOwncloudOperation;
import me.fliife.colbert.ui.fragments.OwnCloudFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class OwnCloudAdapter extends RecyclerView.Adapter<OwnCloudAdapter.ViewHolder> {
    private final FragmentManager fragmentManager;
    private ArrayList<RemoteFile> dataset;
    private RemoteFile first;
    private OwnCloudFragment listener;
    private OwnCloudClient client;

    public OwnCloudAdapter(final RemoteFile first, ArrayList<RemoteFile> dataset, OwnCloudFragment onRemoteOperationListener, OwnCloudClient client, FragmentManager fragmentManager) {
        this.first = first;
        this.dataset = dataset;
        this.dataset.remove(0);
        Collections.sort(this.dataset, new Comparator<RemoteFile>() {
            @Override
            public int compare(RemoteFile t0, RemoteFile t1) {
                if ((t0.getMimeType().equals("DIR") && !t1.getMimeType().equals("DIR")) ||
                        (!t0.getMimeType().equals("DIR") && t1.getMimeType().equals("DIR"))) {
                    return t0.getMimeType().equals("DIR") ? -1 : 1;
                } else {
                    return t0.getRemotePath().compareToIgnoreCase(t1.getRemotePath());
                }
            }
        });
        if (!first.getRemotePath().equals("/")) {
            this.dataset.add(0, first);
        }
        this.listener = onRemoteOperationListener;
        this.client = client;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public OwnCloudAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.oc_card, parent, false);

        ImageView icon = v.findViewById(R.id.oc_folder_icon);
        TextView title = v.findViewById(R.id.oc_title);
        CardView card = v.findViewById(R.id.oc_card);

        ViewHolder vh = new ViewHolder(icon, title, card);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final RemoteFile file = dataset.get(position);
        if (file.equals(first)) {
            holder.icon.setImageResource(R.drawable.ic_folder_red);
            holder.title.setText("…");
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReadRemoteFolderOperation folderOperation = new ReadRemoteFolderOperation(file.getRemotePath()
                            .replaceAll("/[^/]+?/$", ""));
                    folderOperation.execute(client, listener, null);
                    listener.showLoading();
                }
            });
        } else if (file.getMimeType().equals("DIR")) {
            holder.icon.setImageResource(R.drawable.ic_folder_red);
            holder.title.setText(file.getRemotePath().replace(first.getRemotePath(), "")
                    .replaceAll("/$", ""));
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ReadRemoteFolderOperation folderOperation = new ReadRemoteFolderOperation(file.getRemotePath());
                    folderOperation.execute(client, listener, null);
                    listener.showLoading();
                }
            });
        } else {
            holder.icon.setImageResource(R.drawable.ic_file_oc);
            holder.title.setText(file.getRemotePath().replace(first.getRemotePath(), ""));
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ContextCompat.checkSelfPermission(listener.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(listener.getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        //TODO: Handle result
                    } else {
                        System.out.println("Downloading to " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/Colbert");
                        File result = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/Colbert"
                                + file.getRemotePath().replaceAll("/[^/]+$", ""));
                        System.out.println(result.mkdirs());
                        CustomDownloadOwncloudOperation downloadRemoteFileOperation =
                                new CustomDownloadOwncloudOperation(file.getRemotePath(),
                                        Environment.getExternalStorageDirectory().getAbsolutePath() + "/Colbert");
                        downloadRemoteFileOperation.addDatatransferProgressListener(listener);
                        downloadRemoteFileOperation.execute(client, listener, null);
                        listener.showDialog(file.getRemotePath());
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView icon;
        public CardView card;

        public ViewHolder(ImageView icon, TextView title, CardView card) {
            super(icon.getRootView());
            this.title = title;
            this.icon = icon;
            this.card = card;
        }
    }
}