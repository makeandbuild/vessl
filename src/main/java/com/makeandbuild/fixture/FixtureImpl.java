package com.makeandbuild.fixture;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.makeandbuild.persistence.ObjectNotFoundException;

@SuppressWarnings("rawtypes")
public class FixtureImpl implements Fixture {
    protected List<EntityLoader> entityLoaders;
    protected List<EntityManager> entityManagers;
    Log logger = LogFactory.getLog(this.getClass());
    
    public List<EntityLoader> getEntityLoaders() {
        return entityLoaders;
    }

    public void setEntityLoaders(List<EntityLoader> entityLoaders) {
        this.entityLoaders = entityLoaders;
    }

    public List<EntityManager> getEntityManagers() {
        return entityManagers;
    }

    public void setEntityManagers(List<EntityManager> entityManagers) {
        this.entityManagers = entityManagers;
    }

    private EntityManager getManager(Class entityClass, String subtype){
        for (EntityManager entityManager : entityManagers){
            if (entityManager.getEntityClass().equals(entityClass) && compareTypes(subtype, entityManager.getSubtype())){
                return entityManager;
            }
        }
        throw new RuntimeException("no entity maanager for class "+entityClass);
    }
    private EntityLoader getEntityLoader(Class entityClass, String subtype){
        for (EntityLoader loader : entityLoaders){
            if (loader.getEntityClass().equals(entityClass) && compareTypes(subtype, loader.getSubtype())){
                return loader;
            }
        }
        throw new RuntimeException("no loader for class "+entityClass);
    }
    private boolean compareTypes(String subtype1, String subtype2){
        if (subtype1==null && subtype2==null){
            return true;
        } else {
            return subtype1!=null && subtype1.equals(subtype2);
        }
    }
    @Override
    public void load() throws IOException {
        for (EntityLoader loader : entityLoaders){
            EntityManager manager = getManager(loader.getEntityClass(), loader.getSubtype());
            List<Object> entities = loader.load();
            for (Object entity : entities) {
                manager.save(entity);
            }
        }
    }

    @Override
    public void load(String resourceName) throws ClassNotFoundException, IOException {        
        EntityLoader loader = new ResourceEntityLoaderImpl(resourceName);
        EntityManager manager = getManager(loader.getEntityClass(), loader.getSubtype());
        List<Object> entities = loader.load();
        for (Object entity : entities) {
            manager.save(entity);
        }
    }

    @Override
    public void purge() throws IOException {
        for (EntityManager entityManager : entityManagers){
            Class clazz = entityManager.getEntityClass();
            purge(clazz, entityManager.getSubtype());
        }
    }
    @Override
    public void purge(Class entityClass, String subtype) throws IOException {
        EntityManager manager = getManager(entityClass, subtype);
        EntityLoader loader = getEntityLoader(entityClass, subtype);
        List<Object> entities = loader.loadReverse();
        for (Object entity : entities) {
            try {
                manager.delete(entity);
            }catch (ObjectNotFoundException squash){
                logger.info("not found for "+entity);
            }
        }
    }

    @Override
    public void load(Class entityClass, String subtype) throws IOException {
        EntityLoader loader = getEntityLoader(entityClass, subtype);
        this.load(loader);
    }
    @Override
    public void load(EntityLoader loader) throws IOException {
        EntityManager manager = getManager(loader.getEntityClass(), loader.getSubtype());
        List<Object> entities = loader.load();
        for (Object entity : entities) {
            manager.save(entity);
        }
    }
}