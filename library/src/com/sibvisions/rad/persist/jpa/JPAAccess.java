/*
 * Copyright 2012 SIB Visions GmbH
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
import com.sibvisions.util.log.ILogger;
import com.sibvisions.util.log.LoggerFactory;

/**
 * The <code>JPAAccess</code> class encapsulates the EntityManager and the Metamodel.
 * <code>JPAAccess</code> manages the methode invocation for the internal generic EAO and
 * external EAO.
 * 
 * @author Stefan Wurm
 *
 */
public class JPAAccess 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** The logger. */
	protected static ILogger logger = LoggerFactory.getInstance(JPAAccess.class);
	
	/** The EntityManager to manage the use of the entities. **/
	private EntityManager entityManager;
	
	/** The inernal generic EAO Object. */
	private GenericEAO genericEAO;
	
	/** The external EAO Object. */
	private Object externalEAO;
	
	/** The name of the insert Method. */
	private String insertMethod;
	
	/** The name of the update Method. */
	private String updateMethod;
	
	/** The name of the delete Method. */
	private String deleteMethod;
	
	/** The name of the findById Method. */
	private String findByIdMethod;
	
	/** The name of the findAll Method. */
	private String findAllMethod;
	
	/** The Metamodel holds information about the structure of the entities. **/
	private Metamodel metamodel;	
	
	/** The PersistenceUnitUtil afford methods to get useful information from entities. **/
	private PersistenceUnitUtil persistenceUnitUtil;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** 
	 * Sets the external EAO Object.
	 * 
	 * @param pExternalEAO the external EAO
	 */
	public void setExternalEAO(Object pExternalEAO) 
	{
		externalEAO = pExternalEAO;
		
		initializeMethods();
	}
	
	/**
	 * Initializes the methode Names.
	 */
	private void initializeMethods() 
	{
		if (externalEAO instanceof IGenericEAO) 
		{
			insertMethod = "insert";
			updateMethod = "update";
			deleteMethod = "delete";
			findByIdMethod = "findById";
			findAllMethod = "findAll";
		} 
		else 
		{
			for (Method method : externalEAO.getClass().getMethods()) 
			{
				for (Annotation annotation : method.getAnnotations()) 
				{
					if (annotation.annotationType() == EAOMethod.class) 
					{
						try 
						{
							if ((EAO) annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.INSERT) 
							{
								insertMethod = method.getName();
							} 
							else if ((EAO) annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.INSERT) 
							{
								updateMethod = method.getName();
							} 
							else if ((EAO) annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.DELETE) 
							{
								deleteMethod = method.getName();
							} 
							else if ((EAO) annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.FIND_BY_ID) 
							{
								findByIdMethod = method.getName();
							} 
							else if ((EAO) annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.FIND_ALL) 
							{
								findAllMethod = method.getName();
							}
						} 
						catch (Exception e)
						{
							// Nothing to be done
						}
					}
				}	
			}
		}
	}		

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Returns the Insert Methode name.
	 * 
	 * @return
	 */
	public String getInsertMethod() 
	{
		return insertMethod;
	}

	/**
	 * Sets the Insert Methode name.
	 * 
	 * @param pInsertMethod
	 */
	public void setInsertMethod(String pInsertMethod) 
	{
		insertMethod = pInsertMethod;
	}

	/**
	 * Returns the Update Methode name.
	 * 
	 * @return
	 */
	public String getUpdateMethod() 
	{
		return updateMethod;
	}

	/**
	 * Sets the Update Methode name.
	 * 
	 * @param pUpdateMethod
	 */
	public void setUpdateMethod(String pUpdateMethod) 
	{
		updateMethod = pUpdateMethod;
	}

	/**
	 * Returns the delete Methode name.
	 * 
	 * @return
	 */
	public String getDeleteMethod() 
	{
		return deleteMethod;
	}

	/**
	 * Sets the delete Methode name.
	 * 
	 * @param pDeleteMethod
	 */
	public void setDeleteMethod(String pDeleteMethod)
	{
		deleteMethod = pDeleteMethod;
	}

	/**
	 * Returns the findById Methode name.
	 * 
	 * @return
	 */
	public String getFindByIdMethod() 
	{
		return findByIdMethod;
	}

	/**
	 * Sets the findById Methode name.
	 * 
	 * @param pFindByIdMethod
	 */
	public void setFindByIdMethod(String pFindByIdMethod) 
	{
		findByIdMethod = pFindByIdMethod;
	}
	
	/**
	 * Returns the findAll Methode name.
	 * 
	 * @return
	 */
	public String getFindAllMethod() 
	{
		return findAllMethod;
	}

	/**
	 * Sets the findAll Methode name.
	 * 
	 * @param pFindAllMethod
	 */
	public void setFindAllMethod(String pFindAllMethod)
	{
		findAllMethod = pFindAllMethod;
	}

	/**
	 * If the external EAO is set and also the insert methode is defined, the insert methode
	 * from the external EAO is called. Otherwise the insert methode from the genericEAO.
	 * 
	 * @param pEntity The entity to insert
	 * @param pEntityClass The entity Class of the inserted entity
	 * @return
	 * @throws DataSourceException
	 */
    public Object insert(Object pEntity, Class pEntityClass) throws DataSourceException 
    {
    	Object ret = null;
    	
		if (pEntity != null) 
		{
	    	if (externalEAO != null && insertMethod != null) 
	    	{
	    		try 
	    		{
	    			Method  method = externalEAO.getClass().getMethod(insertMethod, pEntityClass);
	    			ret = method.invoke(externalEAO, pEntity);
	    		} 
	    		catch (InvocationTargetException ite) 
	    		{ 
	    			// If the exception is from Type DataSourceException throw it

	    			Throwable thr = ite.getCause();
	    			
	    			if (thr instanceof DataSourceException)
	    			{
	    				throw (DataSourceException)ite.getCause();
	    			}
	    			else if (thr instanceof RuntimeException) 
	    			{
	    				throw (RuntimeException)thr;
	    			}
	    			else
	    			{
	    				logger.error(thr);
	    			}
	    		} 
	    		catch (Exception e) 
	    		{ 
	    			// If there is an error, ignore it and try it with the genericDAO
    				logger.debug(e);
	    		}    		
	    	}
	    	
	    	if (ret == null || ret.getClass() != pEntityClass) 
	    	{
		    	genericEAO.setEntityClass(pEntityClass);
		    	ret = genericEAO.insert(pEntity);	
	    	}
		}
		
		return ret;
    }
    
    /**
	 * If the external EAO is set and also the update methode is defined, the update methode
	 * from the external EAO is called. Otherwise the update methode from the genericEAO
	 * 
     * @param pEntity the entity to Update
     * @param pEntityClass the entity Class from the entity
     * @throws DataSourceException
     */
    public void update(Object pEntity, Class pEntityClass) throws DataSourceException 
    {
		if (pEntity != null) 
		{
	    	if (externalEAO != null && updateMethod != null) 
	    	{
	    		try 
	    		{
	    			Method  method = externalEAO.getClass().getMethod(updateMethod, pEntityClass);
	    			method.invoke(externalEAO, pEntity);
	    	
	    			return;
	    		} 
	    		catch (InvocationTargetException ite) 
	    		{ 
	    			// If the exception is from Type DataSourceException throw it

	    			Throwable thr = ite.getCause();
	    			
	    			if (thr instanceof DataSourceException)
	    			{
	    				throw (DataSourceException)ite.getCause();
	    			}
	    			else if (thr instanceof RuntimeException) 
	    			{
	    				throw (RuntimeException)thr;
	    			}
	    			else
	    			{
	    				logger.error(thr);
	    			}
	    		} 
	    		catch (Exception e) 
	    		{ 
	    			// If there is an error, ignore it and try it with the genericDAO
	    			logger.debug(e);
	    		}
	    	}
	    	
	    	genericEAO.setEntityClass(pEntityClass);
	    	genericEAO.update(pEntity);	   	    	
		}    	
    }
    
    /**
	 * If the external EAO is set and also the delete methode is defined, the delete methode
	 * from the external EAO is called. Otherwise the delete methode from the genericEAO
	 * 
     * @param pEntity the entity to delete
     * @param pEntityClass the entity class from the entity
     * @throws DataSourceException
     */
    public void delete(Object pEntity, Class pEntityClass) throws DataSourceException 
    {
		if (pEntity != null) 
		{
	    	if (externalEAO != null && deleteMethod != null) 
	    	{
	    		try 
	    		{
	    			Method  method = externalEAO.getClass().getMethod(deleteMethod, pEntityClass);
	    			method.invoke(externalEAO, pEntity);
	    	
	    			return;
	    		} 
	    		catch (InvocationTargetException ite) 
	    		{ 
	    			// If the exception  is from Type DataSourceException throw it
	    		     
	    			Throwable thr = ite.getCause();
	    			
	    			if (thr instanceof DataSourceException)
	    			{
	    				throw (DataSourceException)ite.getCause();
	    			}
	    			else if (thr instanceof RuntimeException) 
	    			{
	    				throw (RuntimeException)thr;
	    			}
	    			else
	    			{
	    				logger.error(thr);
	    			}
	    		} 
	    		catch (Exception e) 
	    		{ 
	    			// If there is an error, ignore it and try it with the genericDAO
	    			logger.debug(e);
	    		}
	    	}
	    	
	    	genericEAO.setEntityClass(pEntityClass);
	    	genericEAO.delete(pEntity);	    	    	
		} 
    }
    
    /**
     * If the external EAO is set and also the findById methode is defined, the findById methode
	 * from the external EAO is called. Otherwise the findById methode from the genericEAO
     * 
     * @param pId the id from the entity
     * @param pEntityClass the entity class to find
     * @return the entity to the id
     * @throws DataSourceException
     */
    public Object findById(Object pId, Class pEntityClass) throws DataSourceException 
    {
    	Object ret = null;
    	
		if (pId != null) 
		{
	    	if (externalEAO != null && findByIdMethod != null) 
	    	{
	    		try 
	    		{
	    			Method  method = externalEAO.getClass().getMethod(findByIdMethod, pId.getClass());
	    			ret = method.invoke(externalEAO, pId);

	    			return ret;
	    		} 
	    		catch (InvocationTargetException ite) 
	    		{ 
	    			// If the exception  is from Type DataSourceException throw it
	    		     
	    			Throwable thr = ite.getCause();
	    			
	    			if (thr instanceof DataSourceException)
	    			{
	    				throw (DataSourceException)ite.getCause();
	    			}
	    			else if (thr instanceof RuntimeException) 
	    			{
	    				throw (RuntimeException)thr;
	    			}
	    			else
	    			{
	    				logger.error(thr);
	    			}
	    		} 
	    		catch (Exception e) 
	    		{ 
	    			// If there is an error, ignore it and try it with the genericDAO
	    			logger.debug(e);
	    		}
	    	}
	    	
	    	genericEAO.setEntityClass(pEntityClass);
	    	ret = genericEAO.findById((Serializable) pId);
		}     	
    	
    	return ret;
    }
    
    /**
	 * If the external EAO is set and also the findAll methode is defined, the findAll methode
	 * from the external EAO is called. Otherwise the findAll methode from the genericEAO
	 * 
     * @param pEntityClass the entity Class
     * @return the entities in a Collection
     * @throws DataSourceException
     */
    public Collection findAll(Class pEntityClass) throws DataSourceException 
    {
    	Object ret = null;

    	if (externalEAO != null && findByIdMethod != null) 
    	{
    		try 
    		{
    			Method  method = externalEAO.getClass().getMethod(findAllMethod);
    			ret = method.invoke(externalEAO);   
    		} 
    		catch (InvocationTargetException ite) 
    		{ 
    			// If the exception  is from Type DataSourceException throw it
    		     
    			Throwable thr = ite.getCause();
    			
    			if (thr instanceof DataSourceException)
    			{
    				throw (DataSourceException)ite.getCause();
    			}
    			else if (thr instanceof RuntimeException) 
    			{
    				throw (RuntimeException)thr;
    			}
    			else
    			{
    				logger.error(thr);
    			}
    		} 
    		catch (Exception e) 
    		{ 
    			// If there is an error, ignore it and try it with the genericDAO
    			logger.error(e);
    		}
    	}
    	
    	if (ret == null || !(ret instanceof Collection)) 
    	{
	    	genericEAO.setEntityClass(pEntityClass);
	    	ret = genericEAO.findAll();
    	}    	
	    
    	return (Collection) ret;    	
    }    
    
    /**
     * Reads and sets the values for the given entity from the db.
     * 
     * @param pEntity
     * @param pEntityClass
     * @throws DataSourceException
     */
    public void refresh(Object pEntity, Class pEntityClass) 
    {
		if (pEntity != null) 
		{
	        entityManager.getTransaction().begin();
	    	
	        entityManager.refresh(pEntity);
	        
	        entityManager.getTransaction().commit();	
		}
    }    

    /**
     * Return the number of Entities for the given criteriaQuery.
     * 
     * @param pCountCriteriaQuery
     * @return the number of Entities
     */
    public Long countByCriteria(CriteriaQuery<Long> pCountCriteriaQuery) 
    { 	
    	if (pCountCriteriaQuery != null) 
    	{
			entityManager.getTransaction().begin();
	        
			Long count = entityManager.createQuery(pCountCriteriaQuery).getSingleResult();
	        
			entityManager.getTransaction().commit(); 
			
			return count;
    	}
    	else
    	{
    		return null;
    	}
    }

    /**
	 * Returns the Entities for the given criteriaQuery.
	 * 
	 * @param pCriteriaQuery
	 * @param pOffset the first Row Index
	 * @param pMax Maximum Number of Entities
	 * @return the entities in a collection
	 * 
	 * throws DataSourceException
     */
    public Collection findByCriteria(CriteriaQuery pCriteriaQuery, int pOffset, int pMax) 
    {
    	 
        entityManager.getTransaction().begin();
    	
        Query query = entityManager.createQuery(pCriteriaQuery);
        query.setFirstResult(pOffset);
        query.setMaxResults(pMax);
        List objectList = (List) query.getResultList();
        
        entityManager.getTransaction().commit();
        
		return objectList;
    }
    
	/**
	 * Returns the Entities for the given criteriaQuery.
	 * 
	 * @param pCriteriaQuery
	 * @return the Entities in a Collection
	 */    
    public Collection findByCriteria(CriteriaQuery pCriteriaQuery) 
    {
        entityManager.getTransaction().begin();
    	
        Query query = entityManager.createQuery(pCriteriaQuery);
        List objectList = (List) query.getResultList();
        
        entityManager.getTransaction().commit();
        
        return objectList;
    }
	
	/**
	 * Returns the <code>EntityManager</code>.
	 * 
	 * @return The <code>EntityManager</code>
	 */
	public EntityManager getEntityManager() 
	{
		return entityManager;
	}

	/**
	 * Sets the <code>EntityManager</code>.
	 * 
	 * @param pEntityManager
	 */
	public void setEntityManager(EntityManager pEntityManager) 
	{
		entityManager = pEntityManager;
		genericEAO = new GenericEAO(pEntityManager);
		metamodel = pEntityManager.getMetamodel();
		persistenceUnitUtil = pEntityManager.getEntityManagerFactory().getPersistenceUnitUtil();
	}

	/**
	 * Returns the Metamodel from the entityManager.
	 * 
	 * @return
	 */
	public Metamodel getMetamodel() 
	{
		return metamodel;
	}

	/**
	 * Returns the persistence unit util.
	 * 
	 * @return
	 */
	public PersistenceUnitUtil getPersistenceUnitUtil() 
	{
		return persistenceUnitUtil;
	}

	/**
	 * Returns the PrimaryKey from the given entity.
	 * 
	 * @param pEntity 
	 * @return The PrimaryKey from the given entity
	 */
	public Object getIdentifier(Object pEntity) 
	{
		return persistenceUnitUtil.getIdentifier(pEntity);
	}

	/**
	 * Returns the <code>EntityType</code> for the given entityClass.
	 * 
	 * @param pEntityClass
	 * @return the <code>EntityType</code> for the given entityClass.
	 */
	public EntityType getEntityType(Class pEntityClass)
	{
		return metamodel.entity(pEntityClass);
	}
	
	/**
	 * Returns the <code>Attributes</code> for the given entityClass
	 * Attributes are the Fields from the entityClass.
	 * 
	 * @param pEntityClass
	 * @return <code>Attributes</code> for the given entityClass in a Collection
	 */
	public Set<Attribute> getAttributes(Class pEntityClass) 
	{
		return metamodel.entity(pEntityClass).getAttributes();
	}	

	/**
	 * Returns the <code>EmbeddableType</code> for the given embeddableClass.
	 * 
	 * @param pEmbeddableClass
	 * @return the <code>EmbeddableType</code> for the given embeddableClass 
	 */
	public EmbeddableType getEmbeddableType(Class pEmbeddableClass) 
	{
		return metamodel.embeddable(pEmbeddableClass);
	}
	
	/**
	 * Returns the CriteriaBuilder from the EntityManager.
	 * 
	 * @return CriteriaBuilder from the EntityManager
	 */
	public CriteriaBuilder getCriteriaBuilder() 
	{
		return entityManager.getCriteriaBuilder();
	}
				
}	// JPAAccess
