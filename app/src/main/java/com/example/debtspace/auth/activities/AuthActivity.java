package com.example.debtspace.auth.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.debtspace.R;
import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.auth.fragments.AuthFragment;
import com.example.debtspace.auth.fragments.SignInFragment;
import com.example.debtspace.auth.fragments.SignUpFragment;
import com.example.debtspace.auth.interfaces.OnAuthStateChangeListener;
import com.example.debtspace.auth.repositories.AuthRepository;
import com.example.debtspace.main.activities.MainActivity;
import com.example.debtspace.main.fragments.NetworkLostDialog;

public class AuthActivity extends AppCompatActivity implements OnAuthStateChangeListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            if (new AuthRepository().getFirebaseAuth().getCurrentUser() != null) {
                onMainScreen();
            } else {
                onAuthScreen();
            }
        }

        observeNetworkState();
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

    @Override
    public void onAuthScreen() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.auth_container, new AuthFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSignInScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, new SignInFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSignUpScreen() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, new SignUpFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onMainScreen() {
        Log.d("Sign in", "successful!");

        finish();
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void onNetworkLostScreen() {
        NetworkLostDialog dialog = new NetworkLostDialog();
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onFailure(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.d("Error auth", errorMessage);
    }
}
