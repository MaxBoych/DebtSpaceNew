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
import com.example.debtspace.config.AppConfig;
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
    private ProgressBar mEventProgressBar;

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
        mEventProgressBar = view.findViewById(R.id.profile_event_progress_bar);

        getSavedData();

        initViewModel();

        mImage.setOnClickListener(this);
        view.findViewById(R.id.button_sign_out).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_image) {
            mOnMainStateChangeListener.onImageManagementScreen(AppConfig.NONE_ID, this);
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
        observeEventState();
        mViewModel.downloadUserData();
    }

    private void observeLoadState() {
        mViewModel.getState().observe(this, state -> {
            switch (state) {
                case SUCCESS_LOAD_DATA:
                    setUserData(mViewModel.getUser());
                    setLoadProgressBarVisibility(View.GONE);
                    break;
                case SUCCESS_LOAD_IMAGE:
                    mUri = mViewModel.getUri();
                    drawImage();
                    setLoadProgressBarVisibility(View.GONE);
                    break;
                case FAIL:
                    showError();
                    setLoadProgressBarVisibility(View.GONE);
                    break;
                case NONE:
                    setLoadProgressBarVisibility(View.GONE);
                    break;
                case PROGRESS:
                    setLoadProgressBarVisibility(View.VISIBLE);
                    break;
            }
        });
    }

    private void showError() {
        Toast.makeText(getContext(),
                mViewModel.getErrorMessage(),
                Toast.LENGTH_LONG)
                .show();
    }

    private void setLoadProgressBarVisibility(int view) {
        mProgressBar.setVisibility(view);
    }

    private void observeEventState() {
        mViewModel.getEventState().observe(this, state -> {
            switch (state) {
                case MODIFIED:
                    updateUserData(mViewModel.getUser());
                    setEventProgressBarVisibility(View.GONE);
                    break;
                case PROGRESS:
                    setEventProgressBarVisibility(View.VISIBLE);
                    break;
                case NONE:
                    setEventProgressBarVisibility(View.GONE);
                    break;
                case FAIL:
                    setEventProgressBarVisibility(View.GONE);
                    showError();
                    break;
            }
        });
    }

    private void setEventProgressBarVisibility(int view) {
        mEventProgressBar.setVisibility(view);
    }

    @SuppressLint("SetTextI18n")
    private void setUserData(User user) {
        if (mDoesDataSave) {
            setScore(user);
        } else {
            mName.setText(getString(R.string.user_full_name, user.getFirstName(), user.getLastName()));
            setScore(user);

            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(AppConfig.NAME_KEY, mName.getText().toString());
            editor.apply();
        }

        if (mUri == null) {
            mViewModel.downloadUserImage();
        }
        mViewModel.observeUserDataEvents();
    }

    private void updateUserData(User user) {
        setScore(user);
    }

    private void setScore(User user) {
        GradientDrawable scoreBackground = (GradientDrawable) mScore.getBackground();
        double scoreValue = Double.parseDouble(user.getScore());
        if (scoreValue > 0) {
            String val = Double.toString(scoreValue);
            mScore.setText(val);
            scoreBackground.setColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.red));
        } else if (scoreValue == 0) {
            mScore.setText(AppConfig.DEFAULT_DEBT_VALUE);
            scoreBackground.setColor(Color.GRAY);
        } else {
            String val = Double.toString(-scoreValue);
            mScore.setText(val);
            scoreBackground.setColor(Color.GREEN);
        }
    }

    private void getSavedData() {
        mDoesDataSave = false;
        mPreferences = Objects.requireNonNull(getContext())
                .getSharedPreferences(mUsername.getTitle().toString(), Context.MODE_PRIVATE);
        String name = mPreferences.getString(AppConfig.NAME_KEY, null);
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
                AppConfig.ENGLISH_LANGUAGE,
                AppConfig.RUSSIAN_LANGUAGE,
                AppConfig.GERMAN_LANGUAGE,
                AppConfig.FRENCH_LANGUAGE,
                AppConfig.SPAIN_LANGUAGE
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(AppConfig.TITLE_LANGUAGE);

        builder.setSingleChoiceItems(languageList, -1, (dialog, which) -> {
            switch (which) {
                case 0:
                    ((MainActivity) Objects.requireNonNull(getActivity())).setLocale(AppConfig.ENGLISH_LANGUAGE_ABBREVIATION);
                    dialog.dismiss();
                    break;
                case 1:
                    ((MainActivity) Objects.requireNonNull(getActivity())).setLocale(AppConfig.RUSSIAN_LANGUAGE_ABBREVIATION);
                    dialog.dismiss();
                    break;
                case 2:
                    ((MainActivity) Objects.requireNonNull(getActivity())).setLocale(AppConfig.GERMAN_LANGUAGE_ABBREVIATION);
                    dialog.dismiss();
                    break;
                case 3:
                    ((MainActivity) Objects.requireNonNull(getActivity())).setLocale(AppConfig.FRENCH_LANGUAGE_ABBREVIATION);
                    dialog.dismiss();
                    break;
                case 4:
                    ((MainActivity) Objects.requireNonNull(getActivity())).setLocale(AppConfig.SPAIN_LANGUAGE_ABBREVIATION);
                    dialog.dismiss();
                    break;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.nav_lang) {
            showChangeLanguageDialog();
        }
        return true;
    }
}
