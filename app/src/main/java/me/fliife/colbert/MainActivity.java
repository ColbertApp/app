package me.fliife.colbert;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.database.FirebaseDatabase;
import me.fliife.colbert.network.API;
import me.fliife.colbert.storage.StorageUtils;
import me.fliife.colbert.ui.fragments.*;
import me.fliife.colbert.utils.Callbacks;
import me.fliife.colbert.utils.CredentialsHolder;
import me.fliife.colbert.utils.PronoteBroadcastEventReceiver;
import me.fliife.colbert.utils.PronoteService;

import static me.fliife.colbert.utils.PronoteService.SERVICE_ACTION_FETCH;
import static me.fliife.colbert.utils.PronoteService.SERVICE_ACTION_GET_FROM_DATABASE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PronoteLoginFragment.OnLoginSuccess {

    public API api;
    public Snackbar loadingSnackBar;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private FloatingActionButton fab;
    private Menu menu;

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

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

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
    }

    public void doUpdate() {
        loadingSnackBar.dismiss();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        hideRefreshButton();

        Fragment fragment;

        if (id == R.id.nav_fb) {
            fragment = new FbFeedFragment();
        } else if (id == R.id.nav_mdl) {
            fragment = new MDLFragment();
        } else if (id == R.id.nav_taf) {
            showRefreshButton();
            fragment = new TafFragment();
        } else if (id == R.id.nav_marks) {
            showRefreshButton();
            fragment = new AvgFragment();
        } else if (id == R.id.nav_edt) {
            showRefreshButton();
            fragment = new EdtFragment();
        } else if (id == R.id.nav_login) {
            fragment = new PronoteLoginFragment();
        } else if (id == R.id.nav_menu) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.lyceecolbert-tg.org/menu-vivre/restauration-scolaire.html"));
            startActivity(i);
            return false;
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

        if (id != R.id.nav_fb) navigationView.getMenu().findItem(R.id.nav_fb).setChecked(false);
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
}
