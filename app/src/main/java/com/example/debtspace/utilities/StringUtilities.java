package com.example.debtspace.utilities;

import android.util.Patterns;

import java.util.regex.Pattern;

public class StringUtilities {

    public static boolean isNotValidEmail(String email) {
        return !Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        final Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");

        return (password.length() >= 6 &&
                pattern.matcher(password).matches());
    }

    public static boolean isEmpty(String string) {
        return string.isEmpty();
    }
}
