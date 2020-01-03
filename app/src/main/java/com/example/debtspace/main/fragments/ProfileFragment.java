package com.example.debtspace.main.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.debtspace.R;
import com.example.debtspace.application.DebtSpaceApplication;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.activities.MainActivity;
import com.example.debtspace.main.interfaces.OnImageSharingListener;
import com.example.debtspace.main.interfaces.OnMainStateChangeListener;
import com.example.debtspace.main.viewmodels.ProfileViewModel;
import com.example.debtspace.models.User;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        OnImageSharingListener {

    private ProfileViewModel mViewModel;
    private SharedPreferences mPreferences;
    private boolean mDoesDataSave;

    private CircleImageView mImage;
    private Uri mUri;
    private TextView mName;
    private TextView mScore;
    private MenuItem mUsername;
    private ProgressBar mProgressBar;
    private Menu mMenu;

    private OnMainStateChangeListener mOnMainStateChangeListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mOnMainStateChangeListener = (OnMainStateChangeListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mImage = view.findViewById(R.id.profile_image);
        mName = view.findViewById(R.id.profile_name);
        mScore = view.findViewById(R.id.profile_total_debt_value);

        Log.d("#DS", DebtSpaceApplication.from(Objects.requireNonNull(getContext())).getUsername());

        NavigationView navigationView = view.findViewById(R.id.nav_view);
        mMenu = navigationView.getMenu();
        mUsername = mMenu.getItem(0).getSubMenu().getItem(0);
        mUsername.setTitle(DebtSpaceApplication.from(Objects.requireNonNull(getContext())).getUsername());
        navigationView.setNavigationItemSelectedListener(this);
        mProgressBar = view.findViewById(R.id.profile_progress_bar);

        getSavedData();

        initViewModel();
        observeState();
        mViewModel.downloadUserData(getContext());

        mImage.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_image) {
            mOnMainStateChangeListener.onImageManagementScreen(Configuration.NONE_ID, this);
        }
    }

    @Override
    public void onUploaded(Uri uri) {
        mUri = uri;
        drawImage();
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
    }

    private void observeState() {
        mViewModel.getState().observe(this, state -> {
            switch (state) {
                case SUCCESS_LOAD_DATA:
                    //Log.d("#DS", "in SUCCESS_LOAD_DATA");
                    setUserData(mViewModel.getUser());
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case SUCCESS_LOAD_IMAGE:
                    mUri = mViewModel.getUri();
                    drawImage();
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case FAIL:
                    Toast.makeText(getContext(),
                            mViewModel.getErrorMessage(),
                            Toast.LENGTH_LONG)
                            .show();
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case NONE:
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case PROGRESS:
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void setUserData(User user) {
        if (mDoesDataSave) {
            mScore.setText(user.getScore());
        } else {
            mName.setText(getString(R.string.user_full_name, user.getFirstName(), user.getLastName()));
            mScore.setText(user.getScore());

            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString("name", mName.getText().toString());
            editor.apply();
        }

        if (mUri == null) {
            mViewModel.downloadUserImage(getContext());
        }
    }

    private void getSavedData() {
        mDoesDataSave = false;
        mPreferences = Objects.requireNonNull(getContext())
                .getSharedPreferences(mUsername.getTitle().toString(), Context.MODE_PRIVATE);
        String name = mPreferences.getString("name", null);
        if (name != null) {
            mName.setText(name);
            mDoesDataSave = true;
        }

        if (mUri != null) {
            drawImage();
        }
    }

    private void drawImage() {
        Glide.with(this)
                .load(mUri)
                .centerCrop()
                .into(mImage);
    }

    private void showChangeLanguageDialog() {
        final String[] languageList = new String[]{
                Configuration.ENGLISH_LANGUAGE,
                Configuration.RUSSIAN_LANGUAGE,
                Configuration.GERMAN_LANGUAGE,
                Configuration.FRENCH_LANGUAGE,
                Configuration.SPAIN_LANGUAGE
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Language");
        builder.setSingleChoiceItems(languageList, -1, (dialog, which) -> {
            switch (which) {
                case 0:
                    ((MainActivity) getActivity()).setLocale("en");
                    dialog.dismiss();
                    break;
                case 1:
                    ((MainActivity) getActivity()).setLocale("ru");
                    dialog.dismiss();
                    break;
                case 2:
                    ((MainActivity) getActivity()).setLocale("de");
                    dialog.dismiss();
                    break;
                case 3:
                    ((MainActivity) getActivity()).setLocale("fr");
                    dialog.dismiss();
                    break;
                case 4:
                    ((MainActivity) getActivity()).setLocale("es");
                    dialog.dismiss();
                    break;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        showChangeLanguageDialog();
        return true;
    }
}
