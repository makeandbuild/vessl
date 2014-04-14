package com.makeandbuild.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makeandbuild.persistence.AbstractPagedRequest;
import com.makeandbuild.persistence.Criteria;
import com.makeandbuild.persistence.ObjectNotFoundException;
import com.makeandbuild.persistence.jdbc.BaseDao;
import com.makeandbuild.persistence.jdbc.PagedResponse;
import com.makeandbuild.persistence.jdbc.SortBy;

/**
 * User: telrod
 * Date: 3/4/14
 */
public abstract class ResourceSerializedBase<T, ID> extends ResourceBase {

    @Context
    protected UriInfo uriInfo;

    protected abstract BaseDao<T, ID> getDao();
    protected abstract ObjectMapper getObjectMapper();
    protected Class resourceClass;

    public ResourceSerializedBase(Class resourceClass) {
        super();
        this.resourceClass = resourceClass;
    }


    @Path("/{id}")
    @GET
    public Response getOne(@PathParam("id") final ID id) {
        try{
            T model = getDao().find(id);
            return Response.ok().entity(getObjectMapper().writeValueAsString(model)).build();
        }catch(ObjectNotFoundException e){
            return Response.status(400).build();
        }catch(Exception e){
            logger.warn("error with id "+id, e);
//            throw new RestClientException("license with id "+id+" not found");
            return buildExceptionResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    e,
                    "Error getting resource for " + resourceClass,
                    "Error getting resource for " + resourceClass,
                    ErrorCode.INTERNAL_SERVER_ERROR_GENERAL);
        }
    }
    @GET
    public Response getList(@Context UriInfo ui) {
        try {
            List<Criteria> criterias = new ArrayList<Criteria>();
            List<SortBy> sortBys = new ArrayList<SortBy>();
            AbstractPagedRequest request = new AbstractPagedRequest();

            MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                String key = entry.getKey();
                if ("sortBys".equals(key)){
                    String[] sortyBys = entry.getValue().get(0).split(",");
                    for (String sortyBy : sortyBys) {
                        String[] pair = sortyBy.split(":");
                        String attribute = pair[0];
                        boolean order = pair.length == 0 ? true : Boolean.parseBoolean(pair[1]);
                        sortBys.add(new SortBy(attribute, order));
                    }
                } else if ("pageSize".equals(key)){
                    int pageSize = Integer.parseInt(entry.getValue().get(0));
                    request.setPageSize(pageSize);
                } else if ("page".equals(key)){
                    int page = Integer.parseInt(entry.getValue().get(0));
                    request.setPage(page);
                } else {
                    Criteria criteria = new Criteria(key, entry.getValue().get(0));
                    criterias.add(criteria);
                }
            }
            PagedResponse<T> response = getDao().find(request, criterias, sortBys);
            return Response.ok().entity(getObjectMapper().writeValueAsString(response)).build();
        }catch(Exception e){
            logger.warn("error gettting list", e);
//            throw new RestClientException("error getting list");
            return buildExceptionResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    e,
                    "Error getting list of resources for " + resourceClass,
                    "Error getting list of resources for " + resourceClass,
                    ErrorCode.INTERNAL_SERVER_ERROR_GENERAL);
        }
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response create(String json) {
        try{
            T model = (T) getObjectMapper().readValue(json, resourceClass);
            model = getDao().create(model);
            
            ID id = getDao().getId(model);

            URI location = uriInfo.getAbsolutePathBuilder().path(id.toString()).build();
            return Response.created(location).entity(getObjectMapper().writeValueAsString(model)).build();

//            return Response.ok().entity(getObjectMapper().writeValueAsString(model)).build();
        }catch(ObjectNotFoundException e){
            return Response.status(400).build();
        }catch(Exception e){
            logger.warn("could not create resource with payload "+json, e);
//            throw new RestClientException("unable to create resource");
            return buildExceptionResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    e,
                    "Error creating resource for " + resourceClass,
                    "Error creating resource for " + resourceClass,
                    ErrorCode.INTERNAL_SERVER_ERROR_GENERAL);
        }
    }

    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response update(String json) {
        try{
            T model = (T) getObjectMapper().readValue(json, resourceClass);
            model = getDao().update(model);
            return Response.ok().entity(getObjectMapper().writeValueAsString(model)).build();
        }catch(ObjectNotFoundException e){
            return Response.status(400).build();
        }catch(Exception e){
            logger.warn("could not update resource with payload "+json, e);
//            throw new RestClientException("unable to create resource");
            return buildExceptionResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    e,
                    "Error updating resource for " + resourceClass,
                    "Error updating resource for" + resourceClass,
                    ErrorCode.INTERNAL_SERVER_ERROR_GENERAL);
        }
    }

    @Path("/{id}")
    @DELETE
    public Response delete(@PathParam("id") final ID id) {
        try{
            getDao().deleteById(id);
            return Response.ok().build();
        }catch(ObjectNotFoundException e){
            return Response.status(400).build();
        }catch(Exception e){
            logger.warn("could not delete resource with id "+id, e);
//            throw new RestClientException("could not delete resource with id "+id+" not found");
            return buildExceptionResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    e,
                    "Error deleting resource for " + resourceClass,
                    "Error deleting resource for" + resourceClass,
                    ErrorCode.INTERNAL_SERVER_ERROR_GENERAL);

        }
    }
}