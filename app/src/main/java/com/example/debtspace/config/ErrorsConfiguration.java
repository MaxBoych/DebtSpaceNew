package com.example.debtspace.config;

public class ErrorsConfiguration {

    public final static String ERROR_SIGN_IN = "Cannot sign in";
    public final static String ERROR_SIGN_UP = "Cannot sign up";
    public final static String ERROR_CREATE_USER = "Cannot create user";
    public final static String ERROR_SAVE_USERNAME = "Cannot save username";

    public final static String ERROR_FIRST_NAME = "First name failed validation";
    public final static String ERROR_LAST_NAME = "Last name failed validation";
    public final static String ERROR_USERNAME = "Username failed validation or already exists";
    public final static String ERROR_EMAIL = "Email failed validation";
    public final static String ERROR_PASSWORD = "Password must contain at least 6 symbols: uppercase, lowercase, digits";

    public final static String ERROR_DATA_READING = "Some problems with data reading";
    public final static String ERROR_DATA_READING_DEBTS = "Some problems with data reading debts";
    public final static String ERROR_DATA_READING_NOTIFICATIONS = "Some problems with data reading notifications";
    public final static String ERROR_DATA_READING_HISTORY = "Some problems with data reading history";

    public final static String ERROR_DOWNLOAD_GROUP_IDS = "Cannot download group ids";
    public final static String ERROR_DOWNLOAD_GROUP_DEBTS = "Cannot download group debts";
    public final static String ERROR_DOWNLOAD_GROUP_IMAGE = "Cannot download group image. Group ID: ";
    public final static String ERROR_UPLOAD_GROUP_IMAGE = "Cannot upload group image. Group ID: ";
    public final static String ERROR_DOWNLOAD_DEFAULT_GROUP_IMAGE = "Cannot download default group image. Group ID: ";
    public final static String ERROR_UPLOAD_GROUP = "Cannot upload group. Group ID: ";
    public final static String ERROR_UPDATE_GROUP = "Cannot update group. Group ID: ";
    public final static String ERROR_UPLOAD_GROUP_DATA_TO_MEMBERS = "Cannot upload group data to members. Group ID: ";
    public final static String ERROR_MINIMUM_MEMBERS = "At least " + (Configuration.MINIMUM_GROUP_MEMBERS + 1) + " members of the group are required (including you)";

    public final static String ERROR_DOWNLOAD_SINGLE_DEBTS = "Cannot download single debts";
    public final static String ERROR_DOWNLOAD_USER_IMAGE = "Cannot download user image. Username: ";
    public final static String ERROR_DOWNLOAD_DEFAULT_USER_IMAGE = "Cannot download default user image. Username: ";
    public final static String ERROR_UPLOAD_DEBT_DATA = "Cannot upload debt data. Username: ";

    public final static String ERROR_FIND_USER = "Cannot find user. Username: ";
    public final static String WARNING_USER_DOES_NOT_EXIST = "User does not exist. Username: ";
    public final static String ERROR_SEND_FRIEND_REQUEST = "Cannot send friend request. Username: ";
    public final static String ERROR_USER_ALREADY_FRIEND = "This user is already a friend. Username: ";
    public final static String ERROR_REQUEST_ALREADY_SENT = "Friend request has already been sent to this user. Username: ";
    public final static String ERROR_DOWNLOAD_USER_DATA = "Cannot download user data. Username: ";

    public final static String ERROR_DOWNLOAD_HISTORY = "Cannot download history";

    public final static String ERROR_DELETE_REQUEST = "Cannot delete request. Username: ";
    public final static String ERROR_DOWNLOAD_REQUESTS = "Cannot download requests";
    public final static String ERROR_UPLOAD_REQUESTS = "Cannot upload requests. Username: ";

    public final static String ERROR_UPLOAD_IMAGE = "Cannot upload image";
    public final static String ERROR_DOWNLOAD_IMAGE = "Cannot download image";
    public final static String ERROR_DELETE_IMAGE = "Cannot delete image";
    public final static String DEBUG_CHANGE_IMAGE = "Image not yet added";

    public final static String ERROR_DO_STRIKE = "Cannot do strike";
    public final static String ERROR_UPDATE_SCORE = "Cannot update score. Username: ";
    public final static String ERROR_SET_NEW_SCORE = "Cannot set new score. Username: ";

    public final static String ERROR_DOWNLOAD_USERS_FOR_SEARCH = "Cannot download users for search";
    public final static String ERROR_CHECK_FRIENDS_IN_SEARCH = "Cannot check friends in user search";
}
