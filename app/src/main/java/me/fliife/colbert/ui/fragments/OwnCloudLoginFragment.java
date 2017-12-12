package me.fliife.colbert.ui.fragments;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.authentication.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import me.fliife.colbert.R;
import me.fliife.colbert.storage.StorageUtils;

public class OwnCloudLoginFragment extends Fragment implements OnRemoteOperationListener {

    private TextView error;
    private Button login;
    private EditText username, password;
    private ProgressBar pb;
    private String user, pass;
    OnOwnCloudSuccess listener;

    public OwnCloudLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_own_cloud_login, container, false);

        username = rootView.findViewById(R.id.username_oc);
        password = rootView.findViewById(R.id.password_oc);
        error = rootView.findViewById(R.id.oc_error_tv);
        login = rootView.findViewById(R.id.login_button_oc);
        pb = rootView.findViewById(R.id.login_pb_oc);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login.setEnabled(false);
                pb.setVisibility(View.VISIBLE);
                username.setEnabled(false);
                password.setEnabled(false);
                login(username.getText().toString(), password.getText().toString());
            }
        });

        return rootView;
    }

    public void login(String username, String password) {
        user = username;
        pass = password;
        OwnCloudClient client = OwnCloudClientFactory
                .createOwnCloudClient(Uri.parse("https://colbertserv.lyceecolbert-tg.org/owncloud"),
                        getContext(), true);
        client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(username, password));

        ReadRemoteFolderOperation folderOperation = new ReadRemoteFolderOperation("/");
        folderOperation.execute(client, this, new Handler());
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult remoteOperationResult) {
        if(remoteOperationResult.isSuccess()) {
            StorageUtils.saveOwnCloudCredentials(getContext().getApplicationContext(), user, pass);
            listener.onOwnCloudLogin();
        } else {
            login.setEnabled(true);
            pb.setVisibility(View.GONE);
            username.setEnabled(true);
            password.setEnabled(true);
            error.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnOwnCloudSuccess) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public interface OnOwnCloudSuccess {
        void onOwnCloudLogin();
    }
}
