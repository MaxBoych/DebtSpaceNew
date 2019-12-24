package com.example.debtspace.main.interfaces;

import java.util.List;

public interface OnDownloadDataListener<T> {

    void onDownloadSuccessful(List<T> list);

    void onFailure(String errorMessage);
}
