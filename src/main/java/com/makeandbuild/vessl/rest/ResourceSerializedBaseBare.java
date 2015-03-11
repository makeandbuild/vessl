package com.makeandbuild.vessl.rest;

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

import com.makeandbuild.vessl.persistence.AbstractPagedRequest;
import com.makeandbuild.vessl.persistence.Criteria;
import com.makeandbuild.vessl.persistence.ObjectNotFoundException;
import com.makeandbuild.vessl.persistence.Criteria.JoinLogic;
import com.makeandbuild.vessl.persistence.jdbc.BaseDao;
import com.makeandbuild.vessl.persistence.jdbc.PagedResponse;
import com.makeandbuild.vessl.persistence.jdbc.SortBy;
import com.makeandbuild.vessl.validation.ValidationProxyManager;

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
        }catch(Throwable e){
            logger.warn("could not update resource with payload "+json, e);
            return handleException(e, "error updating object");
        }
    }

    public Response delete(@PathParam("id") final ID id) {
        try{
            getDao().deleteById(id);
            return Response.ok().build();
        }catch(Throwable e){
            logger.warn("could not delete resource with id "+id, e);
            return handleException(e, "error deleting object");
        }
    }
}