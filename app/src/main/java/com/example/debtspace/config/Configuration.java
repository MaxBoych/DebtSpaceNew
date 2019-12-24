package com.example.debtspace.config;

public class Configuration {

    public enum AuthStageState {
        SUCCESS,
        FAIL,
        PROGRESS,
        ERROR_FIRST_NAME,
        ERROR_LAST_NAME,
        ERROR_USERNAME,
        ERROR_EMAIL,
        ERROR_PASSWORD,
        NONE
    }

    public enum LoadStageState {
        SUCCESS,
        FAIL,
        PROGRESS,
        NONE
    }

    public final static String USERS_COLLECTION_NAME = "users";
    public final static String DEBTS_COLLECTION_NAME = "debts";
    public final static String FRIENDS_COLLECTION_NAME = "friends";
    public final static String GROUP_DEBTS_COLLECTION_NAME = "group_debts";
    public final static String NOTIFICATIONS_COLLECTION_NAME = "notifications";
    public final static String HISTORY_COLLECTION_NAME = "history";
    public final static String DATES_COLLECTION_NAME = "dates";

    public final static String GROUPS_FIELD_NAME = "groups";
    public final static String USERNAME_FIELD_NAME = "username";

    public final static String DEFAULT_DEBT_VALUE = "0";
    public final static String DEFAULT_IMAGE_VALUE = "default.jpg";
    public final static String DEFAULT_ERROR_VALUE = "";
    public final static int MINIMUM_GROUP_MEMBERS = 2;

    public final static int DEBT_TYPE = 0;
    public final static int GROUP_DEBT_TYPE = 1;
    public final static String NONE_ID = "-1";

    public final static int IMAGE_SIZE_64 = 64;
    public final static int IMAGE_SIZE_128 = 128;

    public final static String ID_KEY = "id";
    public final static String NAME_KEY = "name";
    public final static String FIRST_NAME_KEY = "firstName";
    public final static String LAST_NAME_KEY = "lastName";
    public final static String USERNAME_KEY = "username";
    public final static String DEBT_KEY = "debt";
    public final static String SCORE_KEY = "score";
    public final static String MEMBERS_KEY = "members";
    public final static String COMMENT_KEY = "comment";
    public final static String DATE_KEY = "date";

    public final static String INTENT_IMAGE_TYPE = "image/*";
    public final static String INTENT_IMAGE_TITLE = "Select image";
    public final static int PICK_IMAGE_FROM_GALLERY = 2;

    public final static String ENGLISH_LANGUAGE = "English";
    public final static String RUSSIAN_LANGUAGE = "Русский";
    public final static String GERMAN_LANGUAGE = "Deutsch";
    public final static String FRENCH_LANGUAGE = "Français";
    public final static String SPAIN_LANGUAGE = "Espanol";
}


