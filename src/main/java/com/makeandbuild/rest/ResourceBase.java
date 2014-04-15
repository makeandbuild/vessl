package com.makeandbuild.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.makeandbuild.persistence.ObjectNotFoundException;
import com.makeandbuild.rest.serializers.BeanValidationExceptionSerializer;
import com.makeandbuild.rest.serializers.ObjectNotFoundExceptionSerializer;
import com.makeandbuild.rest.serializers.RuntimeExceptionSerializer;
import com.makeandbuild.validation.exception.BeanValidationException;

/**
 * Base class for restful web service endpoint classes.  Will provide standard helper methods for configuration and response builders.
 *
 * User: telrod
 * Date: 1/25/14
 */
public class ResourceBase {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private ObjectMapper objectMapper = null;
    @SuppressWarnings("deprecation")
    protected final ObjectMapper getObjectMapper() {
        if (objectMapper == null){
            objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(Include.NON_NULL);
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            SimpleModule testModule = new SimpleModule("MyModule", new Version(1, 0, 0, null));
            testModule.addSerializer(BeanValidationException.class, new BeanValidationExceptionSerializer());
            testModule.addSerializer(ObjectNotFoundException.class, new ObjectNotFoundExceptionSerializer());
            testModule.addSerializer(RuntimeException.class, new RuntimeExceptionSerializer());
            addModuleSerializers(testModule);
            objectMapper.registerModule(testModule);
        }
        return objectMapper;        
    }
    protected void addModuleSerializers(SimpleModule testModule){}

    /**
     * To be used to build OK response with passed json as the response payload.  Will return response code of 200 unless
     * jsonObject is null, then will call buildExceptionResponse().
     * @param jsonObject
     * @return will return response code of 200 or 500 if json object is null.
     */
    protected Response buildOkResponse(JSONObject jsonObject) {
        if(jsonObject != null) {
            // the 2 parameter to toString() method causes pretty print
            return Response.status(Response.Status.OK).entity(jsonObject.toString(2)).build();
        }
        else {
            return buildExceptionResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    new NullPointerException("JSON object to build OK response is null."),
                    null,
                    "Can not build response with null JSON value.",
                    "Call to buildOkResponse contains null parameter.",
                    ErrorCode.INTERNAL_SERVER_ERROR_GENERAL);
        }
    }

    /**
     * To be used to build common response back to the client making restful request.  Will include the exception message along with the message passed.
     * The status to respond with can also be specified.
     * @param status response status to provide to caller
     * @param ex exception thrown when processing request
     * @param message end user message to be provided
     * @param devMessage message meant for developer
     * @param code application specific code
     * @return
     */
    protected Response buildExceptionResponse(Response.Status status, Throwable ex, String message, String devMessage, int code) {
        return buildExceptionResponse(status, ex, null, message, devMessage, code);
    }

    /**
     * To be used to build common response back to the client making restful request.  Will include the exception message along with the message passed.
     * The status to respond with can also be specified.
     * @param status response status to provide to caller
     * @param ex exception thrown when processing request
     * @param propertyName name of property that caused error or related to cause
     * @param message end user message to be provided
     * @param devMessage message meant for developer
     * @param code application specific code
     * @return
     */
    protected Response buildExceptionResponse(Response.Status status, Throwable ex, String propertyName, String message, String devMessage, int code) {
        try {
            return Response.status(status).entity(new ObjectMapper().writeValueAsString(failure(status.getStatusCode(), propertyName, message, ex, devMessage, code))).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    protected Response buildExceptionResponse(Throwable ex, String message) {
        return buildExceptionResponse(Response.Status.INTERNAL_SERVER_ERROR, ex, message);
    }

    protected Response buildExceptionResponse(Response.Status status, Throwable ex, String message) {
        try {
            return Response.status(status).entity(new ObjectMapper().writeValueAsString(failure(status.getStatusCode(), (String)null, message,ex, message, (Integer) null))).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }


    public static  String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public static Map<String,Object> failure(int statusCode, String propertyName, String reason, Throwable e, String devMessage, Integer code) {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("status", statusCode);
        if (code != null) {
            result.put("code", code);            
        }
        if(propertyName != null) {
            result.put("property", propertyName);
        }
        result.put("message", reason);
        result.put("developerMessage", devMessage);
        if (e!=null) {
            result.put("exception", e.getMessage());
            // TODO: Should we include stack trace in production responses?
            // Nice for dev but not sure we want in prod
            result.put("stacktrace", getStackTrace(e));
        }
        return result;
    }
}
