package com.example.debtspace.main.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.debtspace.R;
import com.example.debtspace.config.AppConfig;
import com.example.debtspace.config.ErrorsConfig;
import com.example.debtspace.main.interfaces.OnImageSharingListener;
import com.example.debtspace.main.viewmodels.ImageManagementViewModel;
import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ImageManagementDialog extends DialogFragment implements View.OnClickListener {

    private OnImageSharingListener mOnImageSharingListener;

    private Button mUploadImage;

    private ImageView mSelectedImage;
    private Uri mImageUri;
    private String mImageID;

    private ImageManagementViewModel mViewModel;

    private ProgressBar mProgressBar;
    private NavigationView mToolbar;

    public ImageManagementDialog newInstance(String id) {
        Bundle args = new Bundle();
        args.putString(AppConfig.ID_KEY, id);
        ImageManagementDialog fragment = new ImageManagementDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public void setImageSharingListener(OnImageSharingListener listener) {
        mOnImageSharingListener = listener;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_image_management, container, false);

        mUploadImage = view.findViewById(R.id.upload_image_button);
        mSelectedImage = view.findViewById(R.id.selected_image);
        mProgressBar = view.findViewById(R.id.image_progress_bar);

        mToolbar = view.findViewById(R.id.nav_view);
        mToolbar.setNavigationItemSelectedListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.open_image:
                            openImage();
                            return true;
                        case R.id.change_image:
                            chooseImage();
                            return true;
                        case R.id.delete_image:
                            deleteImage();
                            return true;
                        default:
                            return false;
                    }
                }
        );
        if (getArguments() != null) {
            mImageID = getArguments().getString(AppConfig.ID_KEY);
        }

        initViewModel();
        observeLoadState();

        mUploadImage.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.upload_image_button) {
            if (mImageID != null) {
                uploadImage();
            } else {
                mOnImageSharingListener.onUploaded(mImageUri);
                dismiss();
            }
        }
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(ImageManagementViewModel.class);
    }

    private void observeLoadState() {
        mViewModel.getLoadState().observe(this, state -> {
            switch (state) {
                case DOWNLOAD_SUCCESS:
                    setEnabled(true);
                    setEnabledUpload(false);
                    mImageUri = mViewModel.getImageUri();
                    drawImage();
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case UPLOAD_SUCCESS:
                case DELETE_SUCCESS:
                    mProgressBar.setVisibility(View.GONE);
                    dismiss();
                    break;
                case FAIL:
                    mProgressBar.setVisibility(View.GONE);
                    setEnabled(true);

                    Toast.makeText(getContext(),
                            mViewModel.getErrorMessage().getValue(),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                case NONE:
                    mProgressBar.setVisibility(View.GONE);
                    setEnabled(true);
                    setEnabledUpload(false);
                    setEnabledUpload(mViewModel.getImageUri() != null);
                    break;
                case PROGRESS:
                    mProgressBar.setVisibility(View.VISIBLE);
                    setEnabled(false);
                    setEnabledUpload(false);
                    break;
            }
        });
    }

    private void setEnabledUpload(boolean bool) {
        mUploadImage.setEnabled(bool);
    }

    private void setEnabled(boolean bool) {
        mToolbar.setEnabled(bool);
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType(AppConfig.INTENT_IMAGE_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                AppConfig.INTENT_IMAGE_TITLE), AppConfig.PICK_IMAGE_FROM_GALLERY);
    }

    private void uploadImage() {
        mViewModel.uploadImage(mImageUri, mProgressBar, mImageID, getContext());
    }

    private void openImage() {
        if (mImageID != null) {
            mViewModel.downloadImage(mImageID, getContext());
        } else {
            Toast.makeText(getContext(),
                    ErrorsConfig.DEBUG_CHANGE_IMAGE,
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void deleteImage() {
        if (mImageID != null) {
            mViewModel.deleteImage(mImageID, getContext());
        } else {
            Toast.makeText(getContext(),
                    ErrorsConfig.DEBUG_CHANGE_IMAGE,
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConfig.PICK_IMAGE_FROM_GALLERY &&
                resultCode == RESULT_OK &&
                data != null && data.getData() != null) {

            mImageUri = data.getData();
            drawImage();
            setEnabledUpload(true);
        }
    }

    private void drawImage() {
        if (mImageUri != null) {
            Glide.with(Objects.requireNonNull(getContext()))
                    .load(mImageUri)
                    .override(mToolbar.getWidth(), mToolbar.getWidth())
                    .centerCrop()
                    .into(mSelectedImage);
        }
    }
}
