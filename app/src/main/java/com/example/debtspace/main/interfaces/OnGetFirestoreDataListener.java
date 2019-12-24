package com.example.debtspace.main.interfaces;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface OnGetFirestoreDataListener {

    void onGetSuccessful(List<Map<String, Map<String, Object>>> data) throws ExecutionException, InterruptedException;

    void onFailure(String errorMessage);
}
