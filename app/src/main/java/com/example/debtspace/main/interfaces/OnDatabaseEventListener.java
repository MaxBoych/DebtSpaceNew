package com.example.debtspace.main.interfaces;

public interface OnDatabaseEventListener<T> {

    void onAdded(T object);

    void onModified(T object);

    void onRemoved(T object);

    void onFailure(String errorMessage);
}
