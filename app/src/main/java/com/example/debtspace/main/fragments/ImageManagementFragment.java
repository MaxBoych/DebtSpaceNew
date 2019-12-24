package com.example.debtspace.main.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.debtspace.R;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.viewmodels.ImageManagementViewModel;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class ImageManagementFragment extends Fragment implements View.OnClickListener {

    private Button mUploadImage;
    private Button mOpenImage;
    private Button mChangeImage;
    private Button mDeleteImage;
    private ImageView mSelectedImage;
    private Uri mImageUri;
    private String mImageKey;

    private ImageManagementViewModel mViewModel;

    private ProgressBar mProgressBar;

    public ImageManagementFragment getInstance(String id) {
        Bundle args = new Bundle();
        args.putString(Configuration.ID_KEY, id);
        ImageManagementFragment fragment = new ImageManagementFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_image_management, container, false);

        mUploadImage = view.findViewById(R.id.upload_image_button);
        mOpenImage = view.findViewById(R.id.open_image_button);
        mChangeImage = view.findViewById(R.id.change_image_button);
        mDeleteImage = view.findViewById(R.id.delete_image_button);
        mSelectedImage = view.findViewById(R.id.selected_image);
        mProgressBar = view.findViewById(R.id.image_progress_bar);

        mImageKey = Objects.requireNonNull(getArguments()).getString(Configuration.ID_KEY);

        initViewModel();
        observeState();

        mUploadImage.setOnClickListener(this);
        mOpenImage.setOnClickListener(this);
        mChangeImage.setOnClickListener(this);
        mDeleteImage.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.upload_image_button) {
            uploadImage();
        } else if (v.getId() == R.id.open_image_button) {
            openImage();
        } else if (v.getId() == R.id.change_image_button) {
            chooseImage();
        } else if (v.getId() == R.id.delete_image_button) {
            deleteImage();
        }
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(ImageManagementViewModel.class);
    }

    private void observeState() {
        mViewModel.getState().observe(this, listStageState -> {
            switch (listStageState) {
                case SUCCESS:
                    mProgressBar.setVisibility(View.GONE);
                    setEnabled(true);
                    setEnabledUpload(false);

                    Uri uri = mViewModel.getImageUri();
                    if (uri != null) {
                        mImageUri = uri;
                        setEnabledUpload(true);
                        drawImage();
                    }
                    break;
                case FAIL:
                    mProgressBar.setVisibility(View.GONE);
                    setEnabled(true);
                    setEnabledUpload(mViewModel.getImageUri() != null);

                    Toast.makeText(getContext(),
                            mViewModel.getErrorMessage().getValue(),
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                case NONE:
                    mProgressBar.setVisibility(View.GONE);
                    setEnabled(true);
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
        mOpenImage.setEnabled(bool);
        mChangeImage.setEnabled(bool);
        mDeleteImage.setEnabled(bool);
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType(Configuration.INTENT_IMAGE_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, Configuration.INTENT_IMAGE_TITLE), Configuration.PICK_IMAGE_FROM_GALLERY);
        setEnabledUpload(true);
    }

    private void uploadImage() {
        mViewModel.uploadImage(mImageUri, mProgressBar, mImageKey);
    }

    private void openImage() {
        mViewModel.downloadImage(mImageKey);
    }

    private void deleteImage() {
        mViewModel.deleteImage(mImageKey);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Configuration.PICK_IMAGE_FROM_GALLERY &&
                resultCode == RESULT_OK &&
                data != null && data.getData() != null) {

            mImageUri = data.getData();
            drawImage();
        }
    }

    private void drawImage() {
        Picasso.get().load(mImageUri)
                .into(mSelectedImage);
    }
}
