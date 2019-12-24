package com.example.debtspace.auth.repositories;

import android.util.Log;

import com.example.debtspace.auth.interfaces.OnAuthProgressListener;
import com.example.debtspace.config.Configuration;
import com.example.debtspace.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class AuthRepository {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mDatabase;

    public AuthRepository() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
    }

    public void signIn(String email, String password, OnAuthProgressListener progress) {
        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        progress.onSuccessful();
                    } else {
                        progress.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                    }
                }).addOnFailureListener(e -> progress.onFailure(e.getMessage()));
    }

    public void signUp(String firstName, String lastName,
                       String username, String email, String password,
                       OnAuthProgressListener progress) {

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        createUser(firstName, lastName, username, progress);
                    } else {
                        progress.onFailure(Objects.requireNonNull(task.getException()).getMessage());
                    }
                }).addOnFailureListener(e -> progress.onFailure(e.getMessage()));
    }

    private void createUser(String firstName, String lastName, String username,
                            OnAuthProgressListener progress) {

        User user = new User(firstName, lastName, username, "0");
        mDatabase.collection(Configuration.USERS_COLLECTION_NAME)
                .document(username)
                .set(user)
                .addOnSuccessListener(voidTask -> {
                    Log.d("#DS create user", "SUCCESS " + username + ". ");
                    setDisplayName(username, progress);
                })
                .addOnFailureListener(e -> {
                    Log.d("#DS create user", "FAIL " + username + ". " + e.getMessage());
                    progress.onFailure(e.getMessage());
                });
    }

    private void setDisplayName(String username,
                                OnAuthProgressListener progress) {

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest
                .Builder()
                .setDisplayName(username)
                .build();

        Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("#DS updUserProf", "SUCCESS.");
                        progress.onSuccessful();
                        //createDebts(username, progress);
                    } else {
                        String errorMsg = Objects.requireNonNull(task.getException()).getMessage();
                        Log.d("#DS updUserProf", "FAIL. " + errorMsg);
                        progress.onFailure(errorMsg);
                    }
                }).addOnFailureListener(e -> {
            Log.d("#DS updUserProf", "FAIL. " + e.getMessage());
            progress.onFailure(e.getMessage());
        });
    }

//    private void createDebts(String username,
//                             OnAuthProgressListener progress) {
//
//        //Log.d("#DS TEST", "createDebts START");
//
//        Map<String, Integer> debtBonds = new HashMap<>();
//        mDatabase.collection(Configuration.DEBTS_COLLECTION_NAME)
//                .document(username)
//                .set(debtBonds)
//                .addOnCompleteListener(voidTask -> {
//                    Log.d("#DS create debts", "SUCCESS " + username + ". ");
//                    createNotifications(username, progress);
//                }
//                )
//                .addOnFailureListener(e -> {
//                    Log.d("#DS create debts", "FAIL " + username + ". " + e.getMessage());
//                    progress.onFailure(e.getMessage());
//                });
//    }

    /*private void createGroupDebts(String groupName,
                                  OnAuthProgressListener progress) {

        mDatabase.collection(Configuration.GROUP_DEBTS_COLLECTION_NAME)
                .document(groupName)
    }*/

    /*private void createNotifications(String username,
                                     OnAuthProgressListener progress) {

        DocumentReference document = mDatabase.collection(Configuration.NOTIFICATIONS_COLLECTION_NAME)
                .document(username);

        createDebtRequests(document, progress);
    }*/

    /*private void createDebtRequests(DocumentReference document, OnAuthProgressListener progress) {
        Map<String, String> debtRequests = new HashMap<>();

        //Log.d("#DS TEST", "createDebtReq START");

        document.collection("debts")
                .document()
                .set(debtRequests)
                .addOnCompleteListener(voidTask -> {
                    Log.d("#DS create debtReq", "SUCCESS");
                    createFriendRequests(document, progress);
                })
                .addOnFailureListener(e -> {
                    Log.d("#DS create debtReq", "FAIL. " + e.getMessage());
                    progress.onFailure(e.getMessage());
                });
    }*/

    /*private void createFriendRequests(DocumentReference document, OnAuthProgressListener progress) {
        Map<String, String> friendRequests = new HashMap<>();

        //Log.d("#DS TEST", "createFriendReq START");

        document.collection("friends")
                .document()
                .set(friendRequests)
                .addOnCompleteListener(voidTask -> {
                    Log.d("#DS create friendReq", "SUCCESS");
                    progress.onSuccessful();
                })
                .addOnFailureListener(e -> {
                    Log.d("#DS create friendReq", "FAIL. " + e.getMessage());
                    progress.onFailure(e.getMessage());
                });
    }*/

    public FirebaseAuth getFirebaseAuth() {
        return mFirebaseAuth;
    }
}
