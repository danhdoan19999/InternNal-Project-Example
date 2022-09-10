package com.nals.rw360.errors;

public final class ErrorCodes {
    private ErrorCodes() {
    }

    public static final String HANDLE_FILE_FAILED = "RW-0101";

    // Common
    public static final String VALIDATOR = "RW-0100";
    public static final String DATA_CONSTRAINT = "RW-0200";
    public static final String DUPLICATE_DATA = "RW-0300";
    public static final String BAD_REQUEST = "RW-0400";
    public static final String UNAUTHORIZED = "RW-0401";
    public static final String FORBIDDEN = "RW-0403";
    public static final String NOT_FOUND = "RW-0404";
    public static final String METHOD_NOT_ALLOWED = "RW-0405";
    public static final String NOT_ACCEPTABLE = "RW-0406";
    public static final String UNSUPPORTED_MEDIA_TYPE = "RW-0415";
    public static final String PRECONDITION_REQUIRED = "RW-0428";
    public static final String INTERNAL_SERVER = "RW-0500";
    public static final String NOT_IMPLEMENTED = "RW-0501";
    public static final String OBJECT_NOT_FOUND = "RW-0600";
    public static final String REPORT = "RW-0700";
    public static final String SORT_DATA = "RW-0800";
    public static final String SECURITY = "RW-0900";

    // Validator exception error code (Default: RW-0100)
    public static final String INVALID_KEY = "RW-1100";
    public static final String INVALID_USERNAME_OR_PASSWORD = "RW-1101";
    public static final String USER_WAS_LOCKED = "RW-1102";
    public static final String INVALID_REFRESH_TOKEN = "RW-1103";
    public static final String EMAIL_ALREADY_USED = "RW-1103";
    public static final String EMAIL_NOT_ACTIVATED = "RW-1104";
    public static final String INVALID_EMAIL = "RW-1105";
    public static final String INVALID_RESET_KEY = "RW-1106";
    public static final String INVALID_ROLE_TYPE = "RW-1107";
    public static final String EXPIRED_KEY = "RW-1108";
    public static final String LIMIT_NUMBER_OF_SEND_KEY = "RW-1109";
    public static final String INVALID_PASSWORD = "RW-1110";
    public static final String INVALID_AUTH_PROVIDER = "RW-1111";
    public static final String INVALID_OLD_PASSWORD = "RW-1112";
    public static final String INVALID_USER_SITE = "RW-1113";
    public static final String PHONE_ALREADY_USED = "RW-1114";
    public static final String INVALID_PHONE = "RW-1115";
    public static final String INVALID_POSTAL_CODE = "RW-1116";
    public static final String INVALID_NAME = "RW-1117";
    public static final String NAME_ALREADY_USED = "RW-1118";
    public static final String NAME_NOT_BLANK = "RW-1119";
    public static final String DESCRIPTION_NOT_BLANK = "RW-1120";
    public static final String INVALID_DESCRIPTION = "RW-1121";
    public static final String NOT_PERMISSION_UPDATE_GROUP = "RW-1122";
    public static final String GROUP_NAME_ALREADY_USED = "RW-1123";
    public static final String MANAGER_ID_NOT_NULL = "RW-1124";
    public static final String ROLE_NOT_LEADER = "RW-1125";
    public static final String SUB_GROUP_NAME_ALREADY_USED = "RW-1126";
    public static final String GROUP_TYPE_NAME_NOT_NULL = "RW-1127";
    public static final String GROUP_TYPE_NOT_FOUND = "RW-1128";
    public static final String MANAGER_ID_NOT_FOUND = "RW-1129";
    public static final String MEDIA_NOT_NULL = "RW-1130";
    public static final String LIST_MEMBER_ID_NOT_NULL = "RW-1131";
    public static final String USER_ALREADY_JOIN_THIS_SUB_GROUP = "RW-1132";
    public static final String NOT_PERMISSION_UPDATE_SUB_GROUP = "RW-1133";
    public static final String GROUP_TYPE_ID_NOT_NULL = "RW-1134";
    public static final String USER_NOT_EXISTS_IN_SUB_GROUP = "RW-1135";
    public static final String CAN_NOT_REMOVE_MANAGER_SUB_GROUP = "RW-1136";
    public static final String NOT_PERMISSION_REMOVE_MEMBER_OF_SUB_GROUP = "RW-1137";
    public static final String ROLE_ID_NOT_NULL = "RW-1138";
    public static final String FORM_TYPE_NOT_FOUND = "RW-1139";
    public static final String INVALID_EMAIL_COMPANY = "RW-1140";
    public static final String ASSESSMENT_NAME_ALREADY_USED = "RW-1141";
    public static final String INVALID_START_DATE = "RW-1142";
    public static final String INVALID_END_DATE = "RW-1143";
    // Data Constraint exception error (Default: RW-0200)

    // Duplicate Data exception error (Default: RW-0300)

    // Validator exception error (Default: RW-0400)

    // Internal server error (Default: RW-0500)
    public static final String CANNOT_VERIFY_SOCIAL_ACCESS_TOKEN = "RW-1500";
    public static final String CANNOT_GET_SOCIAL_USER_PROFILE = "RW-1501";

    // Object not found exception error (Default: RW-0600)
    public static final String ROLE_NOT_FOUND = "RW-1602";
    public static final String USER_NOT_FOUND = "RW-1603";
    public static final String GENDER_NOT_FOUND = "RW-1604";
    public static final String PERMISSION_TYPE_NOT_FOUND = "RW-1605";
    public static final String ROLE_TYPE_NOT_FOUND = "RW-1606";
    public static final String AUTH_PROVIDER_NOT_FOUND = "RW-1607";
    public static final String MEDIA_NOT_FOUND = "RW-1608";
    public static final String STATUS_NOT_FOUND = "RW-1609";
}
