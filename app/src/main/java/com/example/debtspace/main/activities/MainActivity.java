package com.example.debtspace.main.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.debtspace.R;
import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.auth.activities.AuthActivity;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.main.fragments.DebtListFragment;
import com.example.debtspace.main.fragments.DebtRemovalConfirmDialog;
import com.example.debtspace.main.fragments.FriendRemovalDialog;
import com.example.debtspace.main.fragments.FriendRequestDialog;
import com.example.debtspace.main.fragments.GroupDebtFragment;
import com.example.debtspace.main.fragments.HistoryFragment;
import com.example.debtspace.main.fragments.DebtRemovalDialog;
import com.example.debtspace.main.fragments.HistoryRemovalDialog;
import com.example.debtspace.main.fragments.ImageManagementDialog;
import com.example.debtspace.main.fragments.NetworkLostDialog;
import com.example.debtspace.main.fragments.ProfileFragment;
import com.example.debtspace.main.fragments.FriendRequestConfirmDialog;
import com.example.debtspace.main.fragments.NotificationListFragment;
import com.example.debtspace.main.fragments.StrikeDialog;
import com.example.debtspace.main.fragments.UserSearchListFragment;
import com.example.debtspace.main.interfaces.OnImageSharingListener;
import com.example.debtspace.main.interfaces.OnMainStateChangeListener;
import com.example.debtspace.main.interfaces.OnPassSignalListener;
import com.example.debtspace.models.DebtRequest;
import com.example.debtspace.models.FriendRequest;
import com.example.debtspace.models.GroupDebt;
import com.example.debtspace.models.HistoryItem;
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
                    if (networkState == AppConfig.NetworkState.LOST) {
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
    }

    private void initBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById (R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_debts);
        onDebtListScreen();
        /*bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_profile:
                    onProfileScreen();
                    break;
                case R.id.action_debts:
                    onDebtListScreen();
                    break;
                case R.id.action_history:
                    onHistoryScreen();
                    break;
                case R.id.action_notifications:
                    onNotificationListScreen();
                    break;
            }
            return true;
        });*/

        bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_profile:
                    switchScreen(new ProfileFragment(), AppConfig.FRAGMENT_PROFILE_TAG);
                    break;
                case R.id.action_debts:
                    hideUserSearch();
                    switchScreen(new DebtListFragment(), AppConfig.FRAGMENT_DEBT_LIST_TAG);
                    break;
                case R.id.action_history:
                    switchScreen(new HistoryFragment(), AppConfig.FRAGMENT_HISTORY_TAG);
                    break;
                case R.id.action_notifications:
                    switchScreen(new NotificationListFragment(), AppConfig.FRAGMENT_NOTIFICATION_LIST_TAG);
                    break;
            }
            return true;
        });
    }

    private void hideUserSearch() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment searchFragment = getSupportFragmentManager().findFragmentByTag(AppConfig.FRAGMENT_USER_SEARCH_TAG);
        if (searchFragment != null) {
            transaction.hide(searchFragment);
            transaction.remove(searchFragment);
        }
        transaction.commit();
    }

    private void switchScreen(Fragment newFragment, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        Fragment currentFragment = manager.getPrimaryNavigationFragment();
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        Fragment switchedFragment = manager.findFragmentByTag(tag);
        if (switchedFragment != null) {
            transaction.show(switchedFragment);
        } else {
            switchedFragment = newFragment;
            transaction.add(R.id.main_container, switchedFragment, tag)
                    .addToBackStack(null);
        }

        transaction.setPrimaryNavigationFragment(switchedFragment)
                .setReorderingAllowed(true)
                .commit();
    }

    @Override
    public void onDebtListScreen() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_container, new DebtListFragment(), AppConfig.FRAGMENT_DEBT_LIST_TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onUserSearchListScreen() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_container, new UserSearchListFragment(), AppConfig.FRAGMENT_USER_SEARCH_TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onGroupDebtScreen() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_container, new GroupDebtFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onGroupDebtScreen(GroupDebt debt) {
        GroupDebtFragment fragment = new GroupDebtFragment().newInstance(debt);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_container, fragment)
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
                .add(R.id.main_container, new ProfileFragment())
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
                .add(R.id.main_container, new HistoryFragment(), AppConfig.FRAGMENT_HISTORY_TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onStrikeScreen(User user) {
        StrikeDialog dialog = new StrikeDialog().newInstance(user);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onFriendRemovalScreen(String name, String username) {
        FriendRemovalDialog dialog = new FriendRemovalDialog().newInstance(name, username);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onNotificationListScreen() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_container, new NotificationListFragment(), AppConfig.FRAGMENT_NOTIFICATION_LIST_TAG)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRequestConfirmScreen(FriendRequest request) {
        FriendRequestConfirmDialog dialog = new FriendRequestConfirmDialog().newInstance(request);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onDebtRemovalScreen(HistoryItem item) {
        DebtRemovalDialog dialog = new DebtRemovalDialog().newInstance(item);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onDebtRemovalConfirmScreen(DebtRequest request) {
        DebtRemovalConfirmDialog dialog = new DebtRemovalConfirmDialog().newInstance(request);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onHistoryRemovalScreen(OnPassSignalListener listener) {
        HistoryRemovalDialog dialog = new HistoryRemovalDialog();
        dialog.setPassSignalListener(listener);
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
        Log.e(AppConfig.APPLICATION_LOG_TAG, errorMessage);
    }
}
