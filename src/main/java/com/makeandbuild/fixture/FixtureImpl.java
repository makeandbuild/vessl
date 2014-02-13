package com.makeandbuild.fixture;

import java.io.IOException;
import java.util.List;

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
    @SuppressWarnings("rawtypes")
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

}
