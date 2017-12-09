package me.fliife.colbert.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import me.fliife.colbert.R;
import me.fliife.colbert.network.API;
import me.fliife.colbert.storage.StorageUtils;
import me.fliife.colbert.utils.Callbacks;
import me.fliife.colbert.utils.CredentialsHolder;

public class PronoteLoginFragment extends Fragment {
    OnLoginSuccess listener;

    public PronoteLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.login_fragment, container, false);

        final ProgressBar progressBar = rootView.findViewById(R.id.login_pb);
        final EditText username = rootView.findViewById(R.id.username_pronote);
        final EditText password = rootView.findViewById(R.id.password_pronote);
        final TextView error = rootView.findViewById(R.id.pronote_error_tv);
        final Button connect = rootView.findViewById(R.id.login_button);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                connect.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                username.setEnabled(false);
                password.setEnabled(false);
                API api = API.getInstance(getContext());
                api.setCredentials(new CredentialsHolder(getContext(), username.getText().toString(),
                        password.getText().toString(), API.COLBERT_URL));
                api.login(new Callbacks.Callback<Boolean>() {
                    @Override
                    public void onCallback(Boolean... args) {
                        boolean result = args[0];
                        if (result) {
                            StorageUtils.saveCredentials(getContext(), new CredentialsHolder(getContext(),
                                    username.getText().toString(),
                                    password.getText().toString(),
                                    API.COLBERT_URL));
                            listener.onLoginSuccess();
                        } else {
                            view.post(new Runnable() {
                                @Override
                                public void run() {
                                    username.setEnabled(true);
                                    password.setEnabled(true);
                                    password.setText("");
                                    progressBar.setVisibility(View.INVISIBLE);
                                    error.setVisibility(View.VISIBLE);
                                    connect.setEnabled(true);
                                }
                            });
                        }
                    }
                });
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnLoginSuccess) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public interface OnLoginSuccess {
        void onLoginSuccess();
    }
}
