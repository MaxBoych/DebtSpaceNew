package com.example.debtspace.main.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.debtspace.R;
import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.auth.activities.AuthActivity;
import com.example.debtspace.main.fragments.DebtListFragment;
import com.example.debtspace.main.fragments.FriendRequestDialog;
import com.example.debtspace.main.fragments.GroupDebtFragment;
import com.example.debtspace.main.fragments.HistoryFragment;
import com.example.debtspace.main.fragments.ImageManagementDialog;
import com.example.debtspace.main.fragments.NetworkLostDialog;
import com.example.debtspace.main.fragments.ProfileFragment;
import com.example.debtspace.main.fragments.RequestConfirmDialog;
import com.example.debtspace.main.fragments.RequestListFragment;
import com.example.debtspace.main.fragments.StrikeDialog;
import com.example.debtspace.main.fragments.UserSearchListFragment;
import com.example.debtspace.main.interfaces.OnImageSharingListener;
import com.example.debtspace.main.interfaces.OnMainStateChangeListener;
import com.example.debtspace.models.GroupDebt;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.FirebaseUtilities;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMainStateChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        observeNetworkState();
        initBottomNavigationView();
    }

    private void observeNetworkState() {
        DebtSpaceApplication.from(getApplicationContext())
                .getNetworkState()
                .observe(this, networkState -> {
                    if (networkState == com.example.debtspace.config.Configuration.NetworkState.LOST) {
                        onNetworkLostScreen();
                    }
                });
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);

        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;

        DisplayMetrics metrics = res.getDisplayMetrics();
        res.updateConfiguration(conf, metrics);
        Intent refresh = new Intent(this, MainActivity.class);
        finish();
        startActivity(refresh);






        /*Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lang", lang);
        editor.apply();*/
    }

    public void getLocale() {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String lang = preferences.getString("lang", "");
        setLocale(lang);
    }

    private void initBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById (R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_home);
        onDebtListScreen();
        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_profile:
                    onProfileScreen();

                    break;

                case R.id.action_home:
                    onDebtListScreen();
                    break;

                case R.id.action_history:
                    onHistoryScreen();
                    break;

                case R.id.action_notifications:
                    onRequestScreen();
                    break;
            }
            return true;
        });
    }

    @Override
    public void onDebtListScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new DebtListFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onUserSearchListScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new UserSearchListFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onGroupDebtScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new GroupDebtFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onGroupDebtScreen(GroupDebt debt) {
        GroupDebtFragment fragment = new GroupDebtFragment().newInstance(debt);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFriendRequestScreen(User user) {
        FriendRequestDialog dialog = new FriendRequestDialog().newInstance(user);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onProfileScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new ProfileFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onImageManagementScreen(String id, OnImageSharingListener listener) {
        ImageManagementDialog dialog = new ImageManagementDialog().newInstance(id);
        dialog.setImageSharingListener(listener);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onHistoryScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new HistoryFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onStrikeScreen(User user) {
        StrikeDialog dialog = new StrikeDialog().newInstance(user);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onRequestScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new RequestListFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRequestConfirmScreen(User user) {
        RequestConfirmDialog dialog = new RequestConfirmDialog().newInstance(user);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onAuthScreen() {
        FirebaseUtilities.firebaseSignOut();

        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    @Override
    public void onNetworkLostScreen() {
        NetworkLostDialog dialog = new NetworkLostDialog();
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onFailure(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.d("Error MAIN", errorMessage);
    }
}
