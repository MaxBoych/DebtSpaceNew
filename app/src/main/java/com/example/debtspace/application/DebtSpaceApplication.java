package com.example.debtspace.application;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.debtspace.config.Configuration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class DebtSpaceApplication extends Application {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDatabase;
    private FirebaseStorage mStorage;
    private MutableLiveData<Configuration.NetworkState> mNetworkState;

    @Override
    public void onCreate() {
        super.onCreate();

        mNetworkState = new MutableLiveData<>();
        mNetworkState.setValue(Configuration.NetworkState.LOST);
        addNetworkListener();
    }

    public static DebtSpaceApplication from(Context context) {
        return (DebtSpaceApplication) context.getApplicationContext();
    }

    private void addNetworkListener() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(),
                    new ConnectivityManager.NetworkCallback() {

                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    mNetworkState.postValue(Configuration.NetworkState.AVAILABLE);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    mNetworkState.postValue(Configuration.NetworkState.LOST);
                }
            });
        }
    }

    public LiveData<Configuration.NetworkState> getNetworkState() {
        return mNetworkState;
    }
}
