package com.example.debtspace.utilities;

import android.annotation.SuppressLint;
import android.util.Patterns;

import com.example.debtspace.config.AppConfig;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    public static String getCurrentDateAndTime() {
        @SuppressLint("SimpleDateFormat")
        String date = new SimpleDateFormat(AppConfig.PATTERN_DATE).format(Calendar.getInstance().getTime());
        return date;
    }

    public static String getRandomString(int n) {
        String AlphaNumericString = AppConfig.LETTERS_AND_DIGITS;
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index = (int) (AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }
}
