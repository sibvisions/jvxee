/*
 * Copyright 2009 SIB Visions GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 * History
 *
 * 09.05.2012 - [SW] - creation
 */
package com.sibvisions.rad.persist.jpa;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.rad.persist.DataSourceException;

import com.sibvisions.rad.persist.jpa.EAOMethod.EAO;

/**
 * The <code>JPAAccess</code> class encapsulates the EntityManager and the Metamodel.
 * <code>JPAAccess</code> manages the methode invocation for the internal generic EAO and
 * external EAO.
 * 
 * @author Stefan Wurm
 *
 */
public class JPAAccess {

	/** The EntityManager to manage the use of the entities **/
	private EntityManager entityManager;
	
	/** The inernal generic EAO Object */
	private GenericEAO genericEAO;
	
	/** The external EAO Object */
	private Object externalEAO;
	
	/** The name of the insert Methode */
	private String insertMethod;
	
	/** The name of the update Methode */
	private String updateMethod;
	
	/** The name of the delete Methode */
	private String deleteMethod;
	
	/** The name of the findById Methode */
	private String findByIdMethod;
	
	/** The name of the findAll Methode */
	private String findAllMethod;
	
	/** The Metamodel holds information about the structure of the entities **/
	private Metamodel metamodel;	
	
	/** The PersistenceUnitUtil afford methods to get useful information from entities **/
	private PersistenceUnitUtil persistenceUnitUtil;
	
	
	/** 
	 * Sets the external EAO Object
	 * 
	 * @param externalEAO
	 */
	public void setExternalEAO(Object externalEAO) {
		this.externalEAO = externalEAO;
		initializeMethods();
	}
	
	/**
	 * Initializes the methode Names
	 */
	private void initializeMethods() {

		if(this.externalEAO instanceof IGenericEAO) {
			
			insertMethod = "insert";
			updateMethod = "update";
			deleteMethod = "delete";
			findByIdMethod = "findById";
			findAllMethod = "findAll";
			
		} else {
			
			for(Method method : this.externalEAO.getClass().getMethods()) {
				
				for(Annotation annotation : method.getAnnotations()) {
					
					if(annotation.annotationType() == EAOMethod.class) {
						
						try {
						
							if((EAO) annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.INSERT) {
								insertMethod = method.getName();
							} else if((EAO) annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.INSERT) {
								updateMethod = method.getName();
							} else if((EAO) annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.DELETE) {
								deleteMethod = method.getName();
							} else if((EAO) annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.FIND_BY_ID) {
								findByIdMethod = method.getName();
							}  else if((EAO) annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.FIND_ALL) {
								findAllMethod = method.getName();
							}
						
						} catch(Exception e) {
							// Nothing to do.
						}
						
					}
					
				}	
				
			}
			
		}
		
	}		

	/**
	 * Returns the Insert Methode name
	 * 
	 * @return
	 */
	public String getInsertMethod() {
		return insertMethod;
	}

	/**
	 * Sets the Insert Methode name
	 * 
	 * @param insertMethod
	 */
	public void setInsertMethod(String insertMethod) {
		this.insertMethod = insertMethod;
	}

	/**
	 * Returns the Update Methode name
	 * 
	 * @return
	 */
	public String getUpdateMethod() {
		return updateMethod;
	}

	/**
	 * Sets the Update Methode name
	 * 
	 * @param updateMethod
	 */
	public void setUpdateMethod(String updateMethod) {
		this.updateMethod = updateMethod;
	}

	/**
	 * Returns the delete Methode name
	 * 
	 * @return
	 */
	public String getDeleteMethod() {
		return deleteMethod;
	}

	/**
	 * Sets the delete Methode name
	 * 
	 * @param deleteMethod
	 */
	public void setDeleteMethod(String deleteMethod) {
		this.deleteMethod = deleteMethod;
	}

	/**
	 * Returns the findById Methode name
	 * 
	 * @return
	 */
	public String getFindByIdMethod() {
		return findByIdMethod;
	}

	/**
	 * Sets the findById Methode name
	 * 
	 * @param findByIdMethod
	 */
	public void setFindByIdMethod(String findByIdMethod) {
		this.findByIdMethod = findByIdMethod;
	}
	
	/**
	 * Returns the findAll Methode name
	 * 
	 * @return
	 */
	public String getFindAllMethod() {
		return findAllMethod;
	}

	/**
	 * Sets the findAll Methode name
	 * 
	 * @param findAllMethod
	 */
	public void setFindAllMethod(String findAllMethod) {
		this.findAllMethod = findAllMethod;
	}

	/**
	 * If the external EAO is set and also the insert methode is defined, the insert methode
	 * from the external EAO is called. Otherwise the insert methode from the genericEAO.
	 * 
	 * @param entity The entity to insert
	 * @param entityClass The entity Class of the inserted entity
	 * @return
	 * @throws DataSourceException
	 */
    public Object insert(Object entity, Class entityClass) throws DataSourceException {

    	Object ret = null;
    	
		if(entity != null) {
		
	    	if(externalEAO != null && insertMethod != null) {
	    		
	    		try {

	    			Method  method = externalEAO.getClass().getMethod(insertMethod, entityClass);
	    			ret = method.invoke(externalEAO, entity);
	    				
	    		} catch (InvocationTargetException ite) { // If the exception  is from Type DataSourceException throw it
	    		     
	    			try { 
	    		     
	    		    	 throw ite.getCause();
	    		     
	    		     } catch (DataSourceException dse) {
	    		           throw dse;
	    		     } catch (Throwable e) {}
	    		     
	    		} catch (Exception e) { 
	    			// If there is an error, ignore it and try it with the genericDAO
	    		}    		

	    	}
	    	
	    	if(ret == null || ret.getClass() != entityClass) {
	    				
		    	genericEAO.setEntityClass(entityClass);
		    	ret = genericEAO.insert(entity);	
	    	
	    	}
		}
		
		return ret;

    }
    
    /**
	 * If the external EAO is set and also the update methode is defined, the update methode
	 * from the external EAO is called. Otherwise the update methode from the genericEAO
	 * 
     * @param entity the entity to Update
     * @param entityClass the entity Class from the entity
     * @throws DataSourceException
     */
    public void update(Object entity, Class entityClass) throws DataSourceException {

		if(entity != null) {
		
	    	if(externalEAO != null && updateMethod != null) {
	    		
	    		try {

	    			Method  method = externalEAO.getClass().getMethod(updateMethod, entityClass);
	    			method.invoke(externalEAO, entity);
	    	
	    			return;
	    			
	    		} catch (InvocationTargetException ite) { // If the exception  is from Type DataSourceException throw it
	    		     
	    			try { 
	    		     
	    		    	 throw ite.getCause();
	    		     
	    		     } catch (DataSourceException dse) {
	    		           throw dse;
	    		     } catch (Throwable e) {}
	    		     
	    		} catch (Exception e) { 
	    			// If there is an error, ignore it and try it with the genericDAO
	    		}
	    		 
	    	}
	    	
	    	genericEAO.setEntityClass(entityClass);
	    	genericEAO.update(entity);	   	    	
	    	
		}    	
    }
    
    /**
	 * If the external EAO is set and also the delete methode is defined, the delete methode
	 * from the external EAO is called. Otherwise the delete methode from the genericEAO
	 * 
     * @param entity the entity to delete
     * @param entityClass the entity class from the entity
     * @throws DataSourceException
     */
    public void delete(Object entity, Class entityClass) throws DataSourceException {
    	
		if(entity != null) {
			
	    	if(externalEAO != null && deleteMethod != null) {
	    		
	    		try {

	    			Method  method = externalEAO.getClass().getMethod(deleteMethod, entityClass);
	    			method.invoke(externalEAO, entity);
	    	
	    			return;
	    			
	    		} catch (InvocationTargetException ite) { // If the exception  is from Type DataSourceException throw it
	    		     
	    			try { 
	    		     
	    		    	 throw ite.getCause();
	    		     
	    		     } catch (DataSourceException dse) {
	    		           throw dse;
	    		     } catch (Throwable e) {}
	    		     
	    		} catch (Exception e) { 
	    			// If there is an error, ignore it and try it with the genericDAO
	    		}
	    		
	    	}
	    	
	    	genericEAO.setEntityClass(entityClass);
	    	genericEAO.delete(entity);	    	    	
	    	
		} 
    }
        
    
    /**
     * If the external EAO is set and also the findById methode is defined, the findById methode
	 * from the external EAO is called. Otherwise the findById methode from the genericEAO
     * 
     * @param id the id from the entity
     * @param entityClass the entity class to find
     * @return the entity to the id
     * @throws DataSourceException
     */
    public Object findById(Object id, Class entityClass) throws DataSourceException {

    	Object ret = null;
    	
		if(id != null) {
			
	    	if(externalEAO != null && findByIdMethod != null) {
	    		
	    		try {

	    			Method  method = externalEAO.getClass().getMethod(findByIdMethod, id.getClass());
	    			ret = method.invoke(externalEAO, id);

	    			return ret;
	    			
	    		} catch (InvocationTargetException ite) { // If the exception  is from Type DataSourceException throw it
	    		     
	    			try { 
	    		     
	    		    	 throw ite.getCause();
	    		     
	    		     } catch (DataSourceException dse) {
	    		           throw dse;
	    		     } catch (Throwable e) {}
	    		     
	    		} catch (Exception e) { 
	    			// If there is an error, ignore it and try it with the genericDAO
	    		}
	    			    	
	    	}
	    	
	    	genericEAO.setEntityClass(entityClass);
	    	ret = genericEAO.findById((Serializable) id);
	    	
		}     	
    	
    	return ret;
    
    }
    
    /**
	 * If the external EAO is set and also the findAll methode is defined, the findAll methode
	 * from the external EAO is called. Otherwise the findAll methode from the genericEAO
	 * 
     * @param entityClass the entity Class
     * @return the entities in a Collection
     * @throws DataSourceException
     */
    public Collection findAll(Class entityClass) throws DataSourceException {

    	Object ret = null;

    	if(externalEAO != null && findByIdMethod != null) {
    		
    		try {

    			Method  method = externalEAO.getClass().getMethod(findAllMethod);
    			ret = method.invoke(externalEAO);   

    		} catch (InvocationTargetException ite) { // If the exception  is from Type DataSourceException throw it
    		     
    			try { 
    		     
    		    	 throw ite.getCause();
    		     
    		     } catch (DataSourceException dse) {
    		           throw dse;
    		     } catch (Throwable e) {}
    		     
    		} catch (Exception e) { 
    			// If there is an error, ignore it and try it with the genericDAO
    		}
	    	
    	}
    	
    	if(ret == null || !(ret instanceof Collection)) {

	    	genericEAO.setEntityClass(entityClass);
	    	ret = genericEAO.findAll();
    	}    	
	    
    	return (Collection) ret;    	
    }    
    
    /**
     * Reads and sets the values for the given entity from the db
     * 
     * @param entity
     * @param entityClass
     * @throws DataSourceException
     */
    public void refresh(Object entity, Class entityClass) {

		if(entity != null) {
				
	        entityManager.getTransaction().begin();
	    	
	        entityManager.refresh(entity);
	        
	        entityManager.getTransaction().commit();	
    
		}

    }    

    /**
     * Return the number of Entities for the given criteriaQuery
     * 
     * @param countCriteriaQuery
     * @return the number of Entities
     */
    public Long countByCriteria(CriteriaQuery<Long> countCriteriaQuery) { 	
		
    	if(countCriteriaQuery != null) {
    	
			entityManager.getTransaction().begin();
	        
			Long count = entityManager.createQuery(countCriteriaQuery).getSingleResult();
	        
			entityManager.getTransaction().commit(); 
			
			return count;
		
    	}
    	
    	return null;
		
    }

    /**
	 * Returns the Entities for the given criteriaQuery.
	 * 
	 * @param criteriaQuery
	 * @param offset the first Row Index
	 * @param max Maximum Number of Entities
	 * @return the entities in a collection
	 * 
	 * throws DataSourceException
     */
    public Collection findByCriteria(CriteriaQuery criteriaQuery, int offset, int max) {
    	 
        entityManager.getTransaction().begin();
    	
        Query query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult(offset);
        query.setMaxResults(max);
        List objectList = (List) query.getResultList();
        
        entityManager.getTransaction().commit();
        
		return objectList;
    }
    
	/**
	 * Returns the Entities for the given criteriaQuery.
	 * 
	 * @param criteriaQuery
	 * @return the Entities in a Collection
	 */    
    public Collection findByCriteria(CriteriaQuery criteriaQuery) {

        entityManager.getTransaction().begin();
    	
        Query query = entityManager.createQuery(criteriaQuery);
        List objectList = (List) query.getResultList();
        
        entityManager.getTransaction().commit();
        
        return objectList;
        
    }
	
	/**
	 * Returns the <code>EntityManager</code>
	 * 
	 * @return The <code>EntityManager</code>
	 */
	public EntityManager getEntityManager() {
		return entityManager;
	}

	/**
	 * Sets the <code>EntityManager</code>
	 * 
	 * @param entityManager
	 */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
		this.genericEAO = new GenericEAO(entityManager);
		this.metamodel = entityManager.getMetamodel();
		this.persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
	}

	/**
	 * Returns the Metamodel from the entityManager
	 * 
	 * @return
	 */
	public Metamodel getMetamodel() {
		return metamodel;
	}

	/**
	 * Returns the persistenceUnitUtil
	 * 
	 * @return
	 */
	public PersistenceUnitUtil getPersistenceUnitUtil() {
		return persistenceUnitUtil;
	}

	/**
	 * Returns the PrimaryKey from the given entity
	 * 
	 * @param entity 
	 * @return The PrimaryKey from the given entity
	 */
	public Object getIdentifier(Object entity) {
		return persistenceUnitUtil.getIdentifier(entity);
	}

	/**
	 * Returns the <code>EntityType</code> for the given entityClass.
	 * 
	 * @param entityClass
	 * @return the <code>EntityType</code> for the given entityClass.
	 */
	public EntityType getEntityType(Class entityClass) {
		return metamodel.entity(entityClass);
	}
	
	/**
	 * Returns the <code>Attributes</code> for the given entityClass
	 * Attributes are the Fields from the entityClass.
	 * 
	 * @param entityClass
	 * @return <code>Attributes</code> for the given entityClass in a Collection
	 */
	public Set<Attribute> getAttributes(Class entityClass) {
		return metamodel.entity(entityClass).getAttributes();
	}	

	/**
	 * Returns the <code>EmbeddableType</code> for the given embeddableClass
	 * 
	 * @param embeddableClass
	 * @return the <code>EmbeddableType</code> for the given embeddableClass 
	 */
	public EmbeddableType getEmbeddableType(Class embeddableClass) {
		return metamodel.embeddable(embeddableClass);
	}
	
	/**
	 * Returns the CriteriaBuilder from the EntityManager
	 * 
	 * @return CriteriaBuilder from the EntityManager
	 */
	public CriteriaBuilder getCriteriaBuilder() {
		
		return entityManager.getCriteriaBuilder();
		
	}
				
}
