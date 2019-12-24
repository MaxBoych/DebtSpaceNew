package com.example.debtspace.main.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.debtspace.R;
import com.example.debtspace.auth.activities.AuthActivity;
import com.example.debtspace.main.fragments.DebtListFragment;
import com.example.debtspace.main.fragments.FriendRequestDialogFragment;
import com.example.debtspace.main.fragments.GroupDebtFragment;
import com.example.debtspace.main.fragments.HistoryFragment;
import com.example.debtspace.main.fragments.ImageManagementFragment;
import com.example.debtspace.main.fragments.ProfileFragment;
import com.example.debtspace.main.fragments.RequestConfirmDialogFragment;
import com.example.debtspace.main.fragments.RequestListFragment;
import com.example.debtspace.main.fragments.StrikeDialogFragment;
import com.example.debtspace.main.fragments.UserSearchListFragment;
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

        initBottomNavigationView();
    }

    public void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lang", lang);
        editor.apply();
    }

    public void getLocale() {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String lang = preferences.getString("lang", "");
        setLocale(lang);
    }

    private void initBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById (R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_home);
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
                .commit();
    }

    @Override
    public void onUserSearchListScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new UserSearchListFragment())
                .commit();
    }

    @Override
    public void onGroupDebtScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new GroupDebtFragment())
                .commit();
    }

    @Override
    public void onGroupDebtScreen(GroupDebt debt) {
        GroupDebtFragment fragment = new GroupDebtFragment().newInstance(debt);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }

    @Override
    public void onFriendRequestScreen(User user) {
        FriendRequestDialogFragment dialog = new FriendRequestDialogFragment().newInstance(user);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onProfileScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new ProfileFragment())
                .commit();
    }

    @Override
    public void onImageManagementScreen(String id) {
        ImageManagementFragment fragment = new ImageManagementFragment().getInstance(id);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, fragment)
                .commit();
    }

    @Override
    public void onHistoryScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new HistoryFragment())
                .commit();
    }

    @Override
    public void onStrikeScreen(User user) {
        StrikeDialogFragment dialog = new StrikeDialogFragment().newInstance(user);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onRequestScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new RequestListFragment())
                .commit();
    }

    @Override
    public void onRequestConfirmScreen(User user) {
        RequestConfirmDialogFragment dialog = new RequestConfirmDialogFragment().newInstance(user);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onAuthScreen() {
        FirebaseUtilities.firebaseSignOut();

        startActivity(new Intent(this, AuthActivity.class));
        finish();
    }

    @Override
    public void onFailure(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.d("Error MAIN", errorMessage);
    }
}
