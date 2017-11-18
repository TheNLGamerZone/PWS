package nl.hetbaarnschlyceum.pws.server.tc;

public class OperationResult {
    public static final int SUCCESS = 5;
    public static final int UNIQUE_USER = 1;
    public static final int FAILED_DUPLICATE_NAME = 0;
    public static final int FAILED_DUPLICATE_NUMBER = -1;
    public static final int FAILED_NAMECHECK = -2;
    public static final int FAILED_INCORRECT_HASH = -3;
    public static final int FAILED_UNKNOWN_USER = -4;
    public static final int FAILED_SYS_ERROR = -5;
}
