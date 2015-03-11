package com.makeandbuild.vessl.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.makeandbuild.vessl.persistence.AbstractPagedRequest;
import com.makeandbuild.vessl.persistence.Criteria;
import com.makeandbuild.vessl.persistence.ObjectNotFoundException;
import com.makeandbuild.vessl.persistence.jdbc.SortBy;
import com.makeandbuild.vessl.rest.serializers.BeanValidationExceptionSerializer;
import com.makeandbuild.vessl.rest.serializers.ObjectNotFoundExceptionSerializer;
import com.makeandbuild.vessl.rest.serializers.RuntimeExceptionSerializer;
import com.makeandbuild.vessl.validation.exception.BeanValidationException;


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

    protected Response buildExceptionResponse(Response.Status status, Throwable ex, String message, String devMessage, int code) {
        return buildExceptionResponse(status, ex, null, message, devMessage, code);
    }


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
            result.put("stacktrace", getStackTrace(e));
        }
        return result;
    }

	protected Response handleException(Throwable e, String message){
		try {
			return Response.status(400).entity(getObjectMapper().writeValueAsString(e)).build();
		}catch (Throwable e2){
			return buildExceptionResponse(e, message);
		}

	}
	protected List<SortBy> getSortBys(MultivaluedMap<String, String> queryParams){
		List<SortBy> sortBys = new ArrayList<SortBy>();
		if (queryParams.containsKey("sortBys")){
			List<String> value = queryParams.get("sortBys");
			String[] sortyBys = value.get(0).split(",");
			for (String sortyBy : sortyBys) {
				String[] pair = sortyBy.split(":");
				String attribute = pair[0];
				boolean order = pair.length == 0 ? true : Boolean.parseBoolean(pair[1]);
				sortBys.add(new SortBy(attribute, order));
			}
		}
		return sortBys;
	}
	protected Integer getPageSize(MultivaluedMap<String, String> queryParams){
		if (queryParams.containsKey("pageSize")){
			List<String> value = queryParams.get("pageSize");
			return Integer.parseInt(value.get(0));
		}
		return null;
	}
	protected Integer getPage(MultivaluedMap<String, String> queryParams){
		if (queryParams.containsKey("page")){
			List<String> value = queryParams.get("page");
			return Integer.parseInt(value.get(0));
		}
		return null;
	}
	protected List<Criteria> getCriterias(MultivaluedMap<String, String> queryParams){
		List<Criteria> criterias = new ArrayList<Criteria>();
		for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
			String key = entry.getKey();
			if (!"sortBys".equals(key) && !"pageSize".equals(key) && !"page".equals(key) && !key.endsWith("Operation") && !key.endsWith("JoinLogic")){
				Criteria criteria = new Criteria(key, entry.getValue().get(0));
				criterias.add(criteria);
				if (queryParams.containsKey(key+"Operation")){
					String operation = queryParams.getFirst(key+"Operation");
					criteria.setOperation(operation);
				}
				if (queryParams.containsKey(key+"JoinLogic")){
					Criteria.JoinLogic joinLogic = Criteria.JoinLogic.valueOf(queryParams.getFirst(key + "JoinLogic"));
					criteria.setJoinLogic(joinLogic);
				}
			}
		}
		return criterias;
	}
	protected AbstractPagedRequest getAbstractPagedRequest(MultivaluedMap<String, String> queryParams){
		AbstractPagedRequest request = new AbstractPagedRequest();
		Integer pageSize = getPageSize(queryParams);
		if (pageSize != null){
			request.setPageSize(pageSize);
		}
		Integer page = getPage(queryParams);
		if (page != null){
			request.setPage(page);
		}
		return request;
	}
}
