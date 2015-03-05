package com.makeandbuild.vessl.rest;

/**
 * Class containing constants for application specific error codes within the rest api.
 * Codes should be 5 digit number where the first 3 digits match the general http status code that
 * would be supplied with the error. *
 *
 * User: telrod
 * Date: 3/7/14
 */
public class ErrorCode {

    public static final int UNAUTHORIZED_GENERAL = 40101;

    public static final int INTERNAL_SERVER_ERROR_GENERAL = 50001;
}
