package util;

/**
 * Protocol constants for client-server communication.
 */
public class Protocol {
    public static final String LOGIN                    = "LOGIN";
    public static final String LOGOUT                   = "LOGOUT";
    public static final String REGISTER                 = "REGISTER";
    public static final String GET_ALL_DISASTERS        = "GET_ALL_DISASTERS";
    public static final String GET_PRIORITIZED_DISASTERS= "GET_PRIORITIZED_DISASTERS";
    public static final String REPORT_DISASTER          = "REPORT_DISASTER";
    public static final String UPDATE_DISASTER          = "UPDATE_DISASTER";
    public static final String UPDATE_DISASTER_STATUS   = "UPDATE_DISASTER_STATUS";
    public static final String GET_ALL_TEAMS            = "GET_ALL_TEAMS";
    public static final String GET_AVAILABLE_TEAMS      = "GET_AVAILABLE_TEAMS";
    public static final String CREATE_TEAM              = "CREATE_TEAM";
    public static final String UPDATE_TEAM              = "UPDATE_TEAM";
    public static final String ASSIGN_TEAM              = "ASSIGN_TEAM";
    public static final String GET_TEAM_ASSIGNMENTS     = "GET_TEAM_ASSIGNMENTS";
    public static final String COMPLETE_ASSIGNMENT      = "COMPLETE_ASSIGNMENT";
    public static final String GET_ALL_RESOURCES        = "GET_ALL_RESOURCES";
    public static final String GET_AVAILABLE_RESOURCES  = "GET_AVAILABLE_RESOURCES";
    public static final String CREATE_RESOURCE          = "CREATE_RESOURCE";
    public static final String UPDATE_RESOURCE          = "UPDATE_RESOURCE";
    public static final String ALLOCATE_RESOURCE        = "ALLOCATE_RESOURCE";
    public static final String GET_RESOURCE_ALLOCATIONS = "GET_RESOURCE_ALLOCATIONS";
    public static final String RETURN_ALLOCATION        = "RETURN_ALLOCATION";
    public static final String GET_ALL_USERS            = "GET_ALL_USERS";
    public static final String UPDATE_USER              = "UPDATE_USER";
    public static final String GET_AUDIT_LOGS           = "GET_AUDIT_LOGS";
    public static final String SUCCESS                  = "SUCCESS";
    public static final String FAILURE                  = "FAILURE";
    public static final String DELIMITER                = "|";
    public static final String LIST_DELIMITER           = ";;";
    public static final String FIELD_DELIMITER          = "~~";
}
