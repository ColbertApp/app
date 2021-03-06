package me.fliife.colbert;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import me.fliife.colbert.network.API;
import me.fliife.colbert.storage.StorageUtils;
import me.fliife.colbert.ui.fragments.*;
import me.fliife.colbert.utils.Callbacks;
import me.fliife.colbert.utils.CredentialsHolder;
import me.fliife.colbert.utils.PronoteBroadcastEventReceiver;
import me.fliife.colbert.utils.PronoteService;

import java.io.File;
import java.io.IOException;

import static me.fliife.colbert.utils.PronoteService.SERVICE_ACTION_FETCH;
import static me.fliife.colbert.utils.PronoteService.SERVICE_ACTION_GET_FROM_DATABASE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PronoteLoginFragment.OnLoginSuccess, OwnCloudLoginFragment.OnOwnCloudSuccess {

    public API api;
    public Snackbar loadingSnackBar;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private FloatingActionButton fab;
    private Menu menu;
    public static String OWNCLOUD_NOTIFICATION_CHANNEL_ID = "colbert-owncloud-downloads";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        CredentialsHolder credentials = StorageUtils.getCredentials(this);

        if (!credentials.username.equals("") && !credentials.password.equals("")) {
            // Remove pronote login option
            navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);
            registerPronote(credentials, false);
        } else {
            // Remove pronote features and show only login
            navigationView.getMenu().findItem(R.id.nav_edt).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_marks).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_taf).setVisible(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupNotificationChannel();
        }

        try {
            StorageUtils.copy(getAssets().open("knownServers_ics.bks"), new File(getFilesDir(), "knownServers.bks"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void setupNotificationChannel() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        CharSequence name = "Owncloud Colbert";
        String description = "Téléchargements de fichiers des dossiers personnels et/ou communs.";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(OWNCLOUD_NOTIFICATION_CHANNEL_ID, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.enableVibration(false);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    public void doUpdate() {
        loadingSnackBar.dismiss();
        if (navigationView.getMenu().findItem(R.id.nav_taf).isChecked())
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_taf));
        if (navigationView.getMenu().findItem(R.id.nav_marks).isChecked())
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_marks));
        if (navigationView.getMenu().findItem(R.id.nav_edt).isChecked())
            onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_edt));
    }

    public void registerPronote(CredentialsHolder credentials, boolean fetch) {
        api = API.getInstance(this);
        api.setCredentials(credentials);

        Intent serviceIntent = new Intent(this, PronoteService.class);
        serviceIntent.putExtra("action", fetch ? SERVICE_ACTION_FETCH : SERVICE_ACTION_GET_FROM_DATABASE);
        startService(serviceIntent);

        IntentFilter endIntentService = new IntentFilter(PronoteService.BROADCAST_SERVICE_FINISHED);
        PronoteBroadcastEventReceiver endBroadcastReceiver = new PronoteBroadcastEventReceiver(new Callbacks.SimpleCallback() {
            @Override
            public void onCallback() {
                Log.d("BroadcastEventReceiver", "Fetch succeeded");
                doUpdate();
            }

            @Override
            public void onFail() {
                loadingSnackBar.dismiss();
                Log.d("BroadcastEventReceiver", "Fetch failed");
                Snackbar.make(drawer, "Une erreur est survenue.", Snackbar.LENGTH_LONG).setAction("RÉESSAYER", new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadingSnackBar.show();
                        Intent pronoteService = new Intent(view.getContext(), PronoteService.class);
                        pronoteService.putExtra("action", SERVICE_ACTION_FETCH);
                        getApplication().startService(pronoteService);
                    }
                }).show();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(endBroadcastReceiver, endIntentService);

        loadingSnackBar = Snackbar.make(drawer, "Récupération des données...", Snackbar.LENGTH_INDEFINITE).setAction("Action", null);
        loadingSnackBar.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_fb));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            Intent pronoteService = new Intent(this, PronoteService.class);
            pronoteService.putExtra("action", SERVICE_ACTION_FETCH);
            getApplication().startService(pronoteService);
            loadingSnackBar.show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showRefreshButton() {
        menu.findItem(R.id.action_refresh).setVisible(true);
    }

    public void hideRefreshButton() {
        menu.findItem(R.id.action_refresh).setVisible(false);
    }

    public void showLogoutButton() {
        menu.findItem(R.id.action_logout).setVisible(true);
    }

    public void hideLogoutButton() {
        menu.findItem(R.id.action_logout).setVisible(false);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        hideRefreshButton();
        hideLogoutButton();

        Fragment fragment;

        if (id == R.id.nav_fb) {
            fragment = new FbFeedFragment();
        } else if (id == R.id.nav_mdl) {
            fragment = new MDLFragment();
        } else if (id == R.id.nav_taf) {
            showRefreshButton();
            showLogoutButton();
            fragment = new TafFragment();
        } else if (id == R.id.nav_marks) {
            showRefreshButton();
            showLogoutButton();
            fragment = new AvgFragment();
        } else if (id == R.id.nav_edt) {
            showRefreshButton();
            showLogoutButton();
            fragment = new EdtFragment();
        } else if (id == R.id.nav_login) {
            fragment = new PronoteLoginFragment();
        } else if (id == R.id.nav_files) {
            // OwnCloud
            if (StorageUtils.getOwnCloudCredentials(this).getUsername().equals("")) {
                fragment = new OwnCloudLoginFragment();
            } else {
                showLogoutButton();
                fragment = new OwnCloudFragment();
            }
        } else if (id == R.id.nav_menu) {
            fragment = new MenuFragment();
        } else if (id == R.id.nav_site) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.lyceecolbert-tg.org/"));
            startActivity(i);
            return false;
        } else {
            // Fallback to fb feed
            fragment = new FbFeedFragment();
        }

        FragmentManager fmanager = getSupportFragmentManager();
        fmanager.beginTransaction().replace(R.id.flcontent, fragment).commit();

        getSupportActionBar().setTitle(item.getTitle());

        int[] ids = {R.id.nav_files, R.id.nav_edt, R.id.nav_fb, R.id.nav_login, R.id.nav_marks, R.id.nav_mdl, R.id.nav_menu, R.id.nav_site, R.id.nav_taf};
        for (int menuId : ids) {
            if (menuId != id) navigationView.getMenu().findItem(menuId).setChecked(false);
        }
        drawer.closeDrawer(GravityCompat.START);

        item.setChecked(true);

        return true;
    }

    @Override
    public void onLoginSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                navigationView.getMenu().findItem(R.id.nav_login).setVisible(false);

                navigationView.getMenu().findItem(R.id.nav_edt).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_marks).setVisible(true);
                navigationView.getMenu().findItem(R.id.nav_taf).setVisible(true);

                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_taf));

                registerPronote(StorageUtils.getCredentials(getApplicationContext()), true);
            }
        });
    }

    @Override
    public void onOwnCloudLogin() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onNavigationItemSelected(navigationView.getMenu().findItem(R.id.nav_files));
            }
        });
    }
}
