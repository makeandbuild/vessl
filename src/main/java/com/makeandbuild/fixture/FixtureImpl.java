package com.makeandbuild.fixture;

import java.io.IOException;
import java.util.List;

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

    private EntityManager getManager(Class entityClass){
        for (EntityManager entityManager : entityManagers){
            if (entityManager.getEntityClass().equals(entityClass)){
                return entityManager;
            }
        }
        throw new RuntimeException("no entity maanager for class "+entityClass);
    }
    private EntityLoader getEntityLoader(Class entityClass){
        for (EntityLoader loader : entityLoaders){
            if (loader.getEntityClass().equals(entityClass)){
                return loader;
            }
        }
        throw new RuntimeException("no loader for class "+entityClass);
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
    public void purge() throws IOException {
        for (EntityManager entityManager : entityManagers){
            Class clazz = entityManager.getEntityClass();
            purge(clazz);
        }
    }
    @Override
    public void purge(Class entityClass) throws IOException {
        EntityManager manager = getManager(entityClass);
        EntityLoader loader = getEntityLoader(entityClass);
        List<Object> entities = loader.load();
        for (Object entity : entities) {
            manager.delete(entity);
        }
    }

    @Override
    public void load(Class entityClass) throws IOException {
        EntityLoader loader = getEntityLoader(entityClass);
        this.load(loader);
    }
    @Override
    public void load(EntityLoader loader) throws IOException {
        EntityManager manager = getManager(loader.getEntityClass());
        List<Object> entities = loader.load();
        for (Object entity : entities) {
            manager.save(entity);
        }
    }
}