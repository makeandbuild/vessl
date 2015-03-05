package com.makeandbuild.vessl.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

public abstract class ResourceSerializedBase<T, ID> extends ResourceSerializedBaseBare<T, ID> {

    @SuppressWarnings("rawtypes")
    public ResourceSerializedBase(Class resourceClass) {
        super(resourceClass);
    }

    @Path("/{id}")
    @GET
    public Response getOne(@PathParam("id") final ID id) {
        return super.getOne(id);
    }
    @GET
    public Response getList(@Context UriInfo ui) {
        return super.getList(ui);
    }
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response create(String json, @QueryParam("validate") @DefaultValue("true") final boolean validate) {
        return super.create(json, validate);
    }

    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response update(String json, @QueryParam("validate") @DefaultValue("true") final boolean validate) {
        return super.update(json, validate);
    }

    @Path("/{id}")
    @DELETE
    public Response delete(@PathParam("id") final ID id) {
        return super.delete(id);
    }
}