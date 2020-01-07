package com.example.debtspace.main.interfaces;

public interface OnDownloadDataListener<T> {

    void onDownloadSuccessful(T object);

    void onFailure(String errorMessage);
}
