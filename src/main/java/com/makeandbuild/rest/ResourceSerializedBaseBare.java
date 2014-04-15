package com.makeandbuild.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.springframework.beans.factory.annotation.Autowired;

import com.makeandbuild.persistence.AbstractPagedRequest;
import com.makeandbuild.persistence.Criteria;
import com.makeandbuild.persistence.Criteria.JoinLogic;
import com.makeandbuild.persistence.ObjectNotFoundException;
import com.makeandbuild.persistence.jdbc.BaseDao;
import com.makeandbuild.persistence.jdbc.PagedResponse;
import com.makeandbuild.persistence.jdbc.SortBy;
import com.makeandbuild.validation.ValidationProxyManager;

/**
 * User: telrod
 * Date: 3/4/14
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class ResourceSerializedBaseBare<T, ID> extends ResourceBase {

    @Context
    protected UriInfo uriInfo;
    @Autowired
    ValidationProxyManager validationProxyManager;

    
    protected abstract BaseDao<T, ID> getDao();
    protected Class resourceClass;

    public ResourceSerializedBaseBare(Class resourceClass) {
        super();
        this.resourceClass = resourceClass;
    }

    private BaseDao<T,ID> validationDao;

    protected BaseDao<T, ID> getValidationDao() {
        if (validationDao == null)
            validationDao = (BaseDao<T,ID>)validationProxyManager.newBeanValidatorProxy(getDao(), "data");
        return validationDao;
    }
    protected BaseDao<T, ID> getDao(boolean validate){
        if (validate){
            return getValidationDao();
        } else {
            return getDao();
        }
    }


    public Response getOne(@PathParam("id") final ID id) {
        try{
            T model = getDao().find(id);
            return Response.ok().entity(getObjectMapper().writeValueAsString(model)).build();
        }catch(Throwable e){
            return handleException(e, "resource with id "+id+" not found");
        }
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
                    JoinLogic joinLogic = JoinLogic.valueOf(queryParams.getFirst(key+"JoinLogic"));
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
    public Response getList(@Context UriInfo ui) {
        try {
            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            List<Criteria> criterias = getCriterias(queryParams);
            List<SortBy> sortBys = getSortBys(queryParams);
            AbstractPagedRequest request = getAbstractPagedRequest(queryParams);
            PagedResponse<T> response = getDao().find(request, criterias, sortBys);
            return Response.ok().entity(getObjectMapper().writeValueAsString(response)).build();
        }catch(Throwable e){
            logger.warn("error gettting list", e);
            return handleException(e, "error getting list");
        }
    }
    public Response create(String json, @QueryParam("validate") @DefaultValue("true") final boolean validate) {
        try{
            T model = (T) getObjectMapper().readValue(json, resourceClass);
            model = getDao(validate).create(model);
            
            ID id = getDao().getId(model);

            URI location = uriInfo.getAbsolutePathBuilder().path(id.toString()).build();
            return Response.created(location).entity(getObjectMapper().writeValueAsString(model)).build();
        }catch(Throwable e){
            logger.warn("could not create resource with payload "+json, e);
            return handleException(e, "error creating object");
        }
    }

    public Response update(String json, @QueryParam("validate") @DefaultValue("true") final boolean validate) {
        try{
            T model = (T) getObjectMapper().readValue(json, resourceClass);
            model = getDao(validate).update(model);
            return Response.ok().entity(getObjectMapper().writeValueAsString(model)).build();
        }catch(ObjectNotFoundException e){
            return Response.status(400).build();
        }catch(Exception e){
            logger.warn("could not update resource with payload "+json, e);
            return handleException(e, "error updating object");
        }
    }

    public Response delete(@PathParam("id") final ID id) {
        try{
            getDao().deleteById(id);
            return Response.ok().build();
        }catch(ObjectNotFoundException e){
            return Response.status(400).build();
        }catch(Exception e){
            logger.warn("could not delete resource with id "+id, e);
            return handleException(e, "error deleting object");
        }
    }
}