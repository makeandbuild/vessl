package com.makeandbuild.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class AccessDeniedHandlerImpl implements AccessDeniedHandler{
        static Log log = LogFactory.getLog(AccessDeniedHandlerImpl.class);

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
                throws IOException, ServletException {
            response.setStatus(Response.Status.FORBIDDEN.getStatusCode());
            JSONObject result = new JSONObject();
            try {
                result.put("message", "not authorized");
                response.getWriter().print(result.toString());
            } catch (JSONException e) {
                log.error("problem rendering error",e);
            }            
        }
}
