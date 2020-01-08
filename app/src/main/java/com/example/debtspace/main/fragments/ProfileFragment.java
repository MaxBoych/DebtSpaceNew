package com.example.debtspace.main.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
import androidx.core.content.ContextCompat;
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

        NavigationView navigationView = view.findViewById(R.id.nav_view);
        Menu mMenu = navigationView.getMenu();
        mUsername = mMenu.getItem(0).getSubMenu().getItem(0);
        mUsername.setTitle(DebtSpaceApplication.from(Objects.requireNonNull(getContext())).getUsername());
        navigationView.setNavigationItemSelectedListener(this);
        mProgressBar = view.findViewById(R.id.profile_progress_bar);

        getSavedData();

        initViewModel();

        mImage.setOnClickListener(this);
        view.findViewById(R.id.button_sign_out).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_image) {
            mOnMainStateChangeListener.onImageManagementScreen(Configuration.NONE_ID, this);
        } else if (v.getId() == R.id.button_sign_out) {
            mOnMainStateChangeListener.onAuthScreen();
        }
    }

    @Override
    public void onUploaded(Uri uri) {
        mUri = uri;
        drawImage();
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        mViewModel.setContext(getContext());
        observeLoadState();
        mViewModel.downloadUserData();
    }

    private void observeLoadState() {
        mViewModel.getState().observe(this, state -> {
            switch (state) {
                case SUCCESS_LOAD_DATA:
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

    @SuppressLint("SetTextI18n")
    private void setUserData(User user) {
        if (mDoesDataSave) {
            mScore.setText(user.getScore());
            GradientDrawable debtBackground = (GradientDrawable) mScore.getBackground();
            double debtValue = Double.parseDouble(user.getScore());
            if (debtValue < 0) {
                mScore.setText(Double.toString(-debtValue));
                debtBackground.setColor(Color.RED);
            } else if (debtValue == 0) {
                mScore.setText(0);
                debtBackground.setColor(Color.GRAY);
            } else {
                mScore.setText(Double.toString(debtValue));
                debtBackground.setColor(Color.GREEN);
            }
        } else {
            mName.setText(getString(R.string.user_full_name, user.getFirstName(), user.getLastName()));
            mScore.setText(user.getScore());
            GradientDrawable debtBackground = (GradientDrawable) mScore.getBackground();
            double debtValue = Double.parseDouble(user.getScore());
            if (debtValue < 0) {
                mScore.setText(Double.toString(-debtValue));
                debtBackground.setColor(Color.RED);
            } else if (debtValue == 0) {
                mScore.setText(0);
                debtBackground.setColor(Color.GRAY);
            } else {
                mScore.setText(Double.toString(debtValue));
                debtBackground.setColor(Color.GREEN);
            }

            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(Configuration.NAME_KEY, mName.getText().toString());
            editor.apply();
        }

        if (mUri == null) {
            mViewModel.downloadUserImage();
        }
    }

    private void getSavedData() {
        mDoesDataSave = false;
        mPreferences = Objects.requireNonNull(getContext())
                .getSharedPreferences(mUsername.getTitle().toString(), Context.MODE_PRIVATE);
        String name = mPreferences.getString(Configuration.NAME_KEY, null);
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
        builder.setTitle(Configuration.TITLE_LANGUAGE);

        builder.setSingleChoiceItems(languageList, -1, (dialog, which) -> {
            switch (which) {
                case 0:
                    ((MainActivity) Objects.requireNonNull(getActivity())).setLocale(Configuration.ENGLISH_LANGUAGE_ABBREVIATION);
                    dialog.dismiss();
                    break;
                case 1:
                    ((MainActivity) Objects.requireNonNull(getActivity())).setLocale(Configuration.RUSSIAN_LANGUAGE_ABBREVIATION);
                    dialog.dismiss();
                    break;
                case 2:
                    ((MainActivity) Objects.requireNonNull(getActivity())).setLocale(Configuration.GERMAN_LANGUAGE_ABBREVIATION);
                    dialog.dismiss();
                    break;
                case 3:
                    ((MainActivity) Objects.requireNonNull(getActivity())).setLocale(Configuration.FRENCH_LANGUAGE_ABBREVIATION);
                    dialog.dismiss();
                    break;
                case 4:
                    ((MainActivity) Objects.requireNonNull(getActivity())).setLocale(Configuration.SPAIN_LANGUAGE_ABBREVIATION);
                    dialog.dismiss();
                    break;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == 2131296514) {
            showChangeLanguageDialog();
        }
        return true;
    }
}
