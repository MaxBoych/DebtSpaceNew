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
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.debtspace.R;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.main.interfaces.OnImageSharingListener;
import com.example.debtspace.main.viewmodels.ImageManagementViewModel;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class ImageManagementDialog extends DialogFragment implements View.OnClickListener {

    private OnImageSharingListener mOnImageSharingListener;

    private Button mUploadImage;

    private ImageView mSelectedImage;
    private Uri mImageUri;
    private String mImageID;

    private ImageManagementViewModel mViewModel;

    private ProgressBar mProgressBar;
    private Toolbar mToolbar;

    public ImageManagementDialog newInstance(String id) {
        Bundle args = new Bundle();
        args.putString(Configuration.ID_KEY, id);
        ImageManagementDialog fragment = new ImageManagementDialog();
        fragment.setArguments(args);

        return fragment;
    }

    public void setImageSharingListener(OnImageSharingListener listener) {
        mOnImageSharingListener = listener;
    }

    /*@Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mOnImageSharingListener = (OnImageSharingListener) context;
    }*/

    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_image_management, container, false);

        mUploadImage = view.findViewById(R.id.upload_image_button);
        mSelectedImage = view.findViewById(R.id.selected_image);
        mProgressBar = view.findViewById(R.id.image_progress_bar);

        mToolbar = view.findViewById(R.id.image_toolbar);
        mToolbar.inflateMenu(R.menu.image_menu);
        mToolbar.setOnMenuItemClickListener(item -> {
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
            mImageID = getArguments().getString(Configuration.ID_KEY);
        }

        initViewModel();
        observeState();

        mUploadImage.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.upload_image_button) {
            if (mImageID != null) {
                Log.d("#DS", "ID != null");
                uploadImage();
            } else {
                Log.d("#DS", "ID == null");
                mOnImageSharingListener.onUploaded(mImageUri);
                dismiss();
            }
        }
    }

    private void initViewModel() {
        mViewModel = ViewModelProviders.of(this).get(ImageManagementViewModel.class);
    }

    private void observeState() {
        mViewModel.getState().observe(this, imageStageState -> {
            switch (imageStageState) {
                case DOWNLOAD_SUCCESS:
                    Log.d("#DS", "DOWNLOAD_SUCCESS");
                    mProgressBar.setVisibility(View.GONE);
                    setEnabled(true);
                    setEnabledUpload(false);
                    mImageUri = mViewModel.getImageUri();
                    drawImage();
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
        intent.setType(Configuration.INTENT_IMAGE_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                Configuration.INTENT_IMAGE_TITLE), Configuration.PICK_IMAGE_FROM_GALLERY);
    }

    private void uploadImage() {
        mViewModel.uploadImage(mImageUri, mProgressBar, mImageID);
    }

    private void openImage() {
        mViewModel.downloadImage(mImageID);
    }

    private void deleteImage() {
        mViewModel.deleteImage(mImageID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Configuration.PICK_IMAGE_FROM_GALLERY &&
                resultCode == RESULT_OK &&
                data != null && data.getData() != null) {

            mImageUri = data.getData();
            drawImage();
            setEnabledUpload(true);
        }
    }

    private void drawImage() {
        if (mImageUri != null) {
            Picasso.get().load(mImageUri)
                    .into(mSelectedImage);
        }
    }
}
