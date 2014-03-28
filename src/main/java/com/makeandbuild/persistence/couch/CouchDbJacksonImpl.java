package com.makeandbuild.persistence.couch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.makeandbuild.persistence.Criteria;
import com.makeandbuild.persistence.DaoException;
import com.makeandbuild.persistence.ObjectNotFoundException;
import com.makeandbuild.persistence.AbstractPagedRequest;
import com.makeandbuild.persistence.AbstractPagedResponse;
import com.makeandbuild.persistence.jdbc.SortBy;

public class CouchDbJacksonImpl extends CouchDBBaseImpl implements CouchDbJackson, CouchDao {
    private Log logger = LogFactory.getLog(getClass());
    private ObjectMapper mapper;
    private Resource designDocument;
    private String designDocumentLocation;
    

    public CouchDbJacksonImpl() {
        super();
        this.mapper = new ObjectMapper();
    }

    public CouchDbJacksonImpl(RestTemplate template) {
        super(template);
        this.mapper = new ObjectMapper();
    }
    public void init() {
        try {
            if (createDatabase())
                logger.info("successfully created database "+this.databaseName);
        } catch(Exception squash){
            logger.debug("problem creating db",squash);
        }
        if (designDocument !=null){
            try {
                ObjectNode existing = (ObjectNode)getDocument(baseUrl(designDocumentLocation)); 
                if (existing != null){
                    deleteDocument(existing);
                }
                ObjectNode node = (ObjectNode)mapper.readTree(designDocument.getInputStream());
                String json = mapper.writeValueAsString(node);
                putForObject(baseUrl(designDocumentLocation), json, String.class, new HashMap<String, Object>());
            } catch (Throwable e) {
                logger.warn("problem creating design document", e);
            }
        }
    }
    private void deleteDocument(ObjectNode existing){
        String url = baseUrl(designDocumentLocation+"?rev="+existing.get("_rev").asText());
        try {
            String response = execute(url, "", HttpMethod.DELETE, String.class, new HashMap<String, Object>());
            logger.debug(response);
        } catch (HttpClientErrorException e) {
            logger.error("Error deleting document: " + url, e);
            throw e;
        }
    }
    private JsonNode getDocument(String url) throws JsonProcessingException, IOException{
        try {
            String response = template.getForObject(url, String.class);
            return mapper.readTree(response);
        }catch(HttpClientErrorException e){
            if (e.getStatusCode()==HttpStatus.NOT_FOUND){
                return null;
            }
            throw e;
        }
    }
    @Override
    public ObjectNode save(ObjectNode params) throws DaoException {
        synchronized (getLock()) {
            try {
                String id = params.has("_id") ? params.get("_id").asText() : null;
                String request = params.toString();

                String response = null;
                if (id == null) {
                    response = template.postForObject(dbUrl(), request, String.class);
                } else {
                    try {
                        response = putForObject(dbUrl(id), request, String.class, new HashMap<String, Object>());
                    } catch (HttpClientErrorException restClientException) {
                        boolean rethrowException = true;
                        HttpStatus httpStatus = restClientException.getStatusCode();
                        if (httpStatus != null) {
                            int statusCode = httpStatus.value();
                            if (statusCode == 409) // indicating version
                                                   // conflict
                            {
                                // this can possibly happen due to the apache
                                // http client re-trying the PUT method call
                                // without
                                // interacting with the caller (me) causing
                                // there to be two PUTs. The second PUT gets a
                                // 409 since the revision
                                // of what is being sent is no longer correct
                                // due to being updated by the prior PUT.
                                // So... need to check to see if the current
                                // contents is the same. If so, no problem.
                                // Otherwise, fail.
                                // Jira issue: REN-273
                                ObjectNode currentJSON = find(new CouchId(id, null));
                                String currentRevision = currentJSON.get("_rev").asText();
                                currentJSON.remove("_rev");
                                params.remove("_rev");

                                JsonNode tree1 = mapper.readTree(currentJSON.toString());
                                JsonNode tree2 = mapper.readTree(params.toString());

                                boolean areTheyEqual = tree1.equals(tree2);
                                if (areTheyEqual) {
                                    // the content of document to save is the
                                    // same as the document just fetched
                                    // as the latest revision. This is the good
                                    // outcome, so no problem.
                                    // fix up the revision value in the returned
                                    // document so it reflects the
                                    // latest revision for this object that was
                                    // just fetched from couch.
                                    rethrowException = false;
                                    params.put("_rev", currentRevision);
                                    params.put("_id", currentJSON.get("_id"));
                                }
                            }
                        }
                        if (rethrowException) {
                            throw restClientException;
                        }
                    }
                }
                JsonNode obj = mapper.readTree(response);
                params.put("_id", obj.get("id").asText());
                params.put("_rev", obj.get("rev").asText());
                return params;
            } catch (JsonProcessingException e) {
                logger.error("problem saving " + params, e);
                throw new DaoException(e);
            } catch (IOException e) {
                logger.error("problem saving " + params, e);
                throw new DaoException(e);
            }
        }
    }
    private ObjectNode getAll() throws JsonProcessingException, IOException {
        String response = template.getForObject(dbUrl("/_all_docs"), String.class);
        return (ObjectNode) mapper.readTree(response);
    }
    @Override
    public ObjectNode find(CouchId couchid) throws DaoException {
        String url = dbUrl("/" + couchid.getId());
        try {
            String response = template.getForObject(url, String.class);
            return (ObjectNode) mapper.readTree(response);
        } catch (HttpClientErrorException e){
            if (e.getStatusCode() == HttpStatus.NOT_FOUND){
                throw new ObjectNotFoundException(couchid.getId());
            } else {
                throw e;
            }
        } catch (RuntimeException e){
            logger.debug("problem getting "+url,e);
            throw e;
        } catch (JsonProcessingException e) {
            logger.debug("problem getting "+url,e);
            throw new DaoException(e);
        } catch (IOException e) {
            logger.debug("problem getting "+url,e);
            throw new DaoException(e);
        }
    }

    @Override
    public void deleteById(CouchId couchid) {
        synchronized (getLock()) {
            if (couchid.getRevision()==null){
                ObjectNode node = find(couchid);
                couchid = getId(node);
            }
            String url = dbUrl(couchid.getId() + "?rev=" + couchid.getRevision());
            try {
                String response = execute(url, "", HttpMethod.DELETE, String.class, new HashMap<String, Object>());
                logger.debug(response);
            } catch (HttpClientErrorException e) {
                logger.error("Error deleting document: " + url, e);
                throw e;
            }
        }
    }

    @Override
    public void deleteAll() throws DaoException {
        try {
            ObjectNode all = getAll();
            ArrayNode rows = (ArrayNode) all.get("rows");
            for (int i=0;i<rows.size();i++) {
                JsonNode row = rows.get(i);
                String id = row.get("id").asText();
                String rev = row.get("value").get("rev").asText();
                this.deleteById(new CouchId(id, rev));
            }
        } catch (JsonProcessingException e) {
            logger.debug("problem getting all ",e);
            throw new DaoException(e);
        } catch (IOException e) {
            logger.debug("problem getting all ",e);
            throw new DaoException(e);
        }
    }

    @Override
    public ArrayNode listDatabases() throws JsonProcessingException, IOException {
        String url = baseUrl("/_all_dbs");
        try {
            String response = template.getForObject(url, String.class);
            return (ArrayNode) mapper.readTree(response);
        } catch (RuntimeException e){
            logger.error("problem gettin "+url,e);
            throw e;
        }
    }

    @Override
    public boolean createDatabase() throws JsonProcessingException, IOException {
        String response = putForObject(dbUrl(), "", String.class, new HashMap<String, Object>());
        JsonNode obj = mapper.readTree(response);
        return obj.get("ok").asBoolean();
    }

    @Override
    public boolean deleteDatabase() throws JsonProcessingException, IOException {
        String response = execute(dbUrl(), "", HttpMethod.DELETE, String.class, new HashMap<String, Object>());
        JsonNode obj = mapper.readTree(response);
        return obj.get("ok").asBoolean();
    }

    @Override
    public Class getEntityClass() {
        return ObjectNode.class;
    }

    @Override
    public Class getIdClass() {
        return CouchId.class;
    }

    @Override
    public boolean exists(CouchId id) throws DaoException {
        return this.find(id) == null;
    }

    @Override
    public boolean exists(List<Criteria> criterias) throws DaoException {
        AbstractPagedResponse<ObjectNode, ArrayNode> response = find(new AbstractPagedRequest(), criterias);
        return response.getTotalItems()>0;
    }

    @Override
    public ObjectNode update(ObjectNode item) throws DaoException {
        // TODO check if exists beforehand
        return save(item);
    }

    @Override
    public ObjectNode create(ObjectNode item) throws DaoException {
        // TODO check doesn't exists beforehand
        return save(item);
    }

    private Criteria getCriteria(List<Criteria> criterias, String attributeName){
        for (Criteria criteria : criterias){
            if (criteria.getAttribute().equals(attributeName)){
                return criteria;
            }
        }
        return null;
    }
    @Override
    public AbstractPagedResponse<ObjectNode, ArrayNode> find(AbstractPagedRequest request, List<Criteria> criterias) throws DaoException {
        int skip = request.getPage() * request.getPageSize();
        int limit = request.getPageSize();
        String view = null;
        String snippet = "?skip="+skip+"&limit="+limit;

        for (Criteria criteria : criterias){
            if (criteria.getAttribute().equals("view")){
                view = (String)criteria.getValue();
            } else if (criteria.getAttribute().equals("key")){
                snippet += "&"+toQueryParam(criteria);
            } else if (criteria.getAttribute().equals("startkey")){
                snippet += "&"+toQueryParam(criteria);
            } else if (criteria.getAttribute().equals("endkey")){
                snippet += "&"+toQueryParam(criteria);
            } else {
                snippet += "&"+toQueryParam(criteria);
            }
        }
        String url = dbUrl(view+snippet);

        
        Criteria method = getCriteria(criterias, "method");
        try {
            ObjectNode response = null;
            if (method != null && method.getValue().equals("post")){
                ObjectNode payload = mapper.createObjectNode();
                ArrayNode keys = mapper.createArrayNode();
                payload.put("keys",keys);
                for (String key : (List<String>)getCriteria(criterias, "keys").getValue()){
                    keys.add(key);
                }
                String json = mapper.writeValueAsString(payload);
                response = (ObjectNode) mapper.readTree(template.postForObject(url, json, String.class));
    
            } else {
                    response = (ObjectNode) mapper.readTree(template.getForObject(url, String.class));
            }
            AbstractPagedResponse<ObjectNode, ArrayNode> pagedResponse = new AbstractPagedResponse<ObjectNode, ArrayNode>();
            ArrayNode items = mapper.createArrayNode();
            ArrayNode rows = (ArrayNode) response.get("rows");
            for (int i=0;i<rows.size();i++){
                items.add(rows.get(i).get("value"));
            }
            pagedResponse.setItems(items);
            long totalItems = response.get("total_rows").asLong();
            pagedResponse.setTotalItems(totalItems);
            int totalPages = (int)Math.ceil(totalItems/request.getPageSize());
            pagedResponse.setTotalPages(totalPages);
            return pagedResponse;
        } catch (RestClientException e) {
            logger.error("problem fetching "+url, e);
            throw new DaoException(e);
        } catch (JsonProcessingException e) {
            logger.error("problem fetching "+url, e);
            throw new DaoException(e);
        } catch (IOException e) {
            logger.error("problem fetching "+url, e);
            throw new DaoException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    private String toQueryParam(Criteria criteria) {
        String queryParam = criteria.getAttribute()+"=";
        Object value = criteria.getValue();
        if (value instanceof List){
            List values = (List)value;
            queryParam = queryParam+"[";
            for (int i=0;i<values.size();i++){
                if (i >0)
                    queryParam = queryParam+",";
                queryParam = queryParam + toValue(values.get(i));
            }
            queryParam = queryParam+"]";
        } else {
            queryParam = queryParam+toValue(value);
        }
        return queryParam;
    }
    private String toValue(Object value){
        if (value instanceof String){
            return "\""+value+"\"";
        } else {
            return ""+value;
        }
    }

    private String dbUrl(String snippet){
        return this.baseUrl + "/"+this.databaseName+"/"+snippet;
    }
    private String dbUrl(){
        return this.baseUrl + "/"+this.databaseName;
    }
    private String baseUrl(String snippet){
        return this.baseUrl+"/"+snippet;
    }
    private List<Criteria> toList(Criteria[] params) {
        List<Criteria> criterias = new ArrayList<Criteria>();
        for (Criteria c : params){
            criterias.add(c);
        }
        return criterias;
    }
    @Override
    public AbstractPagedResponse<ObjectNode, ArrayNode> find(AbstractPagedRequest request, Criteria... criteriaParams) throws DaoException {
        return find(request, toList(criteriaParams));
    }

    @Override
    public void delete(List<Criteria> criterias) throws DaoException {
        AbstractPagedRequest request = new AbstractPagedRequest();
        AbstractPagedResponse<ObjectNode, ArrayNode> response = find(request, criterias);
        while(response.getTotalItems() > 0){
            ArrayNode items = response.getItems();
            for (int i=0;i<items.size();i++){
                ObjectNode item = (ObjectNode)items.get(i);
                CouchId id = getId(item);
                this.deleteById(id);
            }
            response = find(request, criterias);
        }
    }
    public CouchId getId(ObjectNode node){
        CouchId id =  new CouchId(node.get("_id").asText());
        if (node.has("_rev")){
            id.setRevision(node.get("_rev").asText());
        }
        return id;
    }
    @Override
    public void delete(Criteria criteria) {
        List<Criteria> criterias = new ArrayList<Criteria>();
        criterias.add(criteria);
        delete(criterias);
    }

    public Resource getDesignDocument() {
        return designDocument;
    }

    public void setDesignDocument(Resource designDocument) {
        this.designDocument = designDocument;
    }

    public String getDesignDocumentLocation() {
        return designDocumentLocation;
    }

    public void setDesignDocumentLocation(String designDocumentLocation) {
        this.designDocumentLocation = designDocumentLocation;
    }

    @Override
    public boolean exists(Criteria... criterias) throws DaoException {
        return exists(toList(criterias));
    }

    @Override
    public void delete(Criteria... criterias) throws DaoException {
        delete(toList(criterias));
    }

}
