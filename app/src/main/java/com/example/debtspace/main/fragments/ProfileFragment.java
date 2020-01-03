package com.example.debtspace.main.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.debtspace.R;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.activities.MainActivity;
import com.example.debtspace.main.interfaces.OnImageSharingListener;
import com.example.debtspace.main.interfaces.OnMainStateChangeListener;
import com.example.debtspace.main.viewmodels.ProfileViewModel;
import com.example.debtspace.models.User;
import com.example.debtspace.utilities.CircleTransform;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment implements View.OnClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        OnImageSharingListener {

    private ImageView mImage;
    private ProfileViewModel mViewModel;

    private ProgressBar mProgressBar;
    private Menu mMenu;

    private TextView mName;
    private TextView mScore;
    private MenuItem mUsername;

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
        mImage = view.findViewById(R.id.profile_image);


        NavigationView navigationView = view.findViewById(R.id.nav_view);
        mMenu = navigationView.getMenu();
        mUsername = mMenu.getItem(0).getSubMenu().getItem(0);
        navigationView.setNavigationItemSelectedListener(this);
        mProgressBar = view.findViewById(R.id.profile_progress_bar);

        initViewModel();
        observeState();
        mViewModel.downloadUserData();

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
        drawImage(uri);
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
    }

    private void observeState() {
        mViewModel.getState().observe(this, state -> {
            switch (state) {
                case SUCCESS:
                    setUserData(mViewModel.getUser());
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case FAIL:
                    mProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(),
                            mViewModel.getErrorMessage().getValue(),
                            Toast.LENGTH_LONG)
                            .show();
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
        mName.setText(getString(R.string.user_full_name, user.getFirstName(), user.getLastName()));
        mScore.setText(user.getScore());
        mUsername.setTitle(user.getUsername());
        drawImage(user.getImageUri());
    }

    private void drawImage(Uri uri) {
        Picasso.get()
                .load(uri)
                .resize(Configuration.IMAGE_SIZE_128, Configuration.IMAGE_SIZE_128)
                .centerCrop()
                .transform(new CircleTransform())
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
