package com.makeandbuild.fixture;

import java.io.IOException;
import java.util.List;

import com.makeandbuild.persistence.BaseDao;

@SuppressWarnings("rawtypes")
public class FixtureImpl implements Fixture {
    protected List<EntityLoader> entityLoaders;
    protected List<EntityManager> entityManagers;
    
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

    @Override
    public void purge() {
        for (EntityManager entityManager : entityManagers){
            entityManager.deleteAll();
        }
    }
    private EntityManager getManager(Class entityClass){
        for (EntityManager entityManager : entityManagers){
            if (entityManager.getEntityClass().equals(entityClass)){
                return entityManager;
            }
        }
        throw new RuntimeException("no entity maanager for class "+entityClass);
    }
    @Override
    public void load() throws IOException {
        for (EntityLoader loader : entityLoaders){
            EntityManager manager = getManager(loader.getEntityClass());
            List<Object> entities = loader.load();
            for (Object entity : entities) {
                manager.save(entity);
            }
        }
        
    }

    @Override
    public void load(String resourceName) throws ClassNotFoundException, IOException {        
        EntityLoader loader = new ResourceEntityLoaderImpl(resourceName);
        EntityManager manager = getManager(loader.getEntityClass());
        List<Object> entities = loader.load();
        for (Object entity : entities) {
            manager.save(entity);
        }
    }

    @Override
    public void purge(Class entityClass) {
        EntityManager manager = getManager(entityClass);
        manager.deleteAll();
    }
    

}
