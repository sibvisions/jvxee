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
 * The {@link JPAAccess} class encapsulates the EntityManager and the Metamodel.
 * {@link JPAAccess} manages the method invocation for the internal generic EAO
 * and external EAO.
 * 
 * @author Stefan Wurm
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
	private String insertMethodName;
	
	/** The name of the update Method. */
	private String updateMethodName;
	
	/** The name of the delete Method. */
	private String deleteMethodName;
	
	/** The name of the findById Method. */
	private String findByIdMethodName;
	
	/** The name of the findAll Method. */
	private String findAllMethodName;
	
	/** The insert Method. */
	private Method insertMethod;
	
	/** The update Method. */
	private Method updateMethod;
	
	/** The delete Method. */
	private Method deleteMethod;
	
	/** The findById Method. */
	private Method findByIdMethod;
	
	/** The findAll Method. */
	private Method findAllMethod;
	
	/** The Metamodel holds information about the structure of the entities. **/
	private Metamodel metamodel;
	
	/**
	 * The PersistenceUnitUtil afford methods to get useful information from
	 * entities.
	 **/
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
	 * Initializes the method names.
	 */
	private void initializeMethods()
	{
		if (externalEAO instanceof IGenericEAO)
		{
			insertMethodName = "insert";
			updateMethodName = "update";
			deleteMethodName = "delete";
			findByIdMethodName = "findById";
			findAllMethodName = "findAll";
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
							if ((EAO)annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.INSERT)
							{
								insertMethodName = method.getName();
							}
							else if ((EAO)annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.INSERT)
							{
								updateMethodName = method.getName();
							}
							else if ((EAO)annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.DELETE)
							{
								deleteMethodName = method.getName();
							}
							else if ((EAO)annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.FIND_BY_ID)
							{
								findByIdMethodName = method.getName();
							}
							else if ((EAO)annotation.getClass().getMethod("methodIdentifier").invoke(annotation) == EAO.FIND_ALL)
							{
								findAllMethodName = method.getName();
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
	 * Returns the Insert Method name.
	 * 
	 * @return The Name of the Insert Method
	 */
	public String getInsertMethod()
	{
		return insertMethodName;
	}
	
	/**
	 * Sets the Insert Method name.
	 * 
	 * @param pInsertMethod The name of the insert method
	 */
	public void setInsertMethod(String pInsertMethod)
	{
		insertMethodName = pInsertMethod;
	}
	
	/**
	 * Returns the Update Method name.
	 * 
	 * @return The Name of the update method
	 */
	public String getUpdateMethod()
	{
		return updateMethodName;
	}
	
	/**
	 * Sets the Update Method name.
	 * 
	 * @param pUpdateMethod The name of the update method
	 */
	public void setUpdateMethod(String pUpdateMethod)
	{
		updateMethodName = pUpdateMethod;
	}
	
	/**
	 * Returns the delete Method name.
	 * 
	 * @return the name of the delete method
	 */
	public String getDeleteMethod()
	{
		return deleteMethodName;
	}
	
	/**
	 * Sets the delete Method name.
	 * 
	 * @param pDeleteMethod the name of the delete method
	 */
	public void setDeleteMethod(String pDeleteMethod)
	{
		deleteMethodName = pDeleteMethod;
	}
	
	/**
	 * Returns the findById Method name.
	 * 
	 * @return the name of the find-by-id method
	 */
	public String getFindByIdMethod()
	{
		return findByIdMethodName;
	}
	
	/**
	 * Sets the findById Method name.
	 * 
	 * @param pFindByIdMethod the name of the find-by-id method
	 */
	public void setFindByIdMethod(String pFindByIdMethod)
	{
		findByIdMethodName = pFindByIdMethod;
	}
	
	/**
	 * Returns the findAll Method name.
	 * 
	 * @return the name of the find-all method
	 */
	public String getFindAllMethod()
	{
		return findAllMethodName;
	}
	
	/**
	 * Sets the findAll Method name.
	 * 
	 * @param pFindAllMethod the name of the find-all method
	 */
	public void setFindAllMethod(String pFindAllMethod)
	{
		findAllMethodName = pFindAllMethod;
	}
	
	/**
	 * If the external EAO is set and also the insert method is defined, the
	 * insert method from the external EAO is called. Otherwise the insert
	 * method from the genericEAO.
	 * 
	 * @param pEntity The entity to insert
	 * @param pEntityClass The entity Class of the inserted entity
	 * @return The inserted entity-object
	 * @throws DataSourceException if inserting the entity failed.
	 */
	public Object insert(Object pEntity, Class pEntityClass) throws DataSourceException
	{
		Object ret = null;
		
		if (pEntity != null)
		{
			if (externalEAO != null && insertMethodName != null)
			{
				try
				{
					if (insertMethod == null)
					{
						insertMethod = externalEAO.getClass().getMethod(insertMethodName, pEntityClass);
					}
					
					ret = insertMethod.invoke(externalEAO, pEntity);
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
	 * If the external EAO is set and also the update method is defined, the
	 * update method from the external EAO is called. Otherwise the update
	 * method from the genericEAO
	 * 
	 * @param pEntity the entity to Update
	 * @param pEntityClass the entity Class from the entity
	 * @throws DataSourceException if updating the entity failed.
	 */
	public void update(Object pEntity, Class pEntityClass) throws DataSourceException
	{
		if (pEntity != null)
		{
			if (externalEAO != null && updateMethodName != null)
			{
				try
				{
					if (updateMethod == null)
					{
						updateMethod = externalEAO.getClass().getMethod(updateMethodName, pEntityClass);
					}
					
					updateMethod.invoke(externalEAO, pEntity);
					
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
	 * If the external EAO is set and also the delete method is defined, the
	 * delete method from the external EAO is called. Otherwise the delete
	 * method from the genericEAO
	 * 
	 * @param pEntity the entity to delete
	 * @param pEntityClass the entity class from the entity
	 * @throws DataSourceException if deleting the entity failed.
	 */
	public void delete(Object pEntity, Class pEntityClass) throws DataSourceException
	{
		if (pEntity != null)
		{
			if (externalEAO != null && deleteMethodName != null)
			{
				try
				{
					if (deleteMethod == null)
					{
						deleteMethod = externalEAO.getClass().getMethod(deleteMethodName, pEntityClass);
					}
					
					deleteMethod.invoke(externalEAO, pEntity);
					
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
	 * If the external EAO is set and also the findById method is defined, the
	 * findById method from the external EAO is called. Otherwise the findById
	 * method from the genericEAO
	 * 
	 * @param pId the id from the entity
	 * @param pEntityClass the entity class to find
	 * @return the entity to the id
	 * @throws DataSourceException if finding failed.
	 */
	public Object findById(Object pId, Class pEntityClass) throws DataSourceException
	{
		Object ret = null;
		
		if (pId != null)
		{
			if (externalEAO != null && findByIdMethodName != null)
			{
				try
				{
					if (findByIdMethod == null)
					{
						findByIdMethod = externalEAO.getClass().getMethod(findByIdMethodName, pId.getClass());
					}
					
					ret = findByIdMethod.invoke(externalEAO, pId);
					
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
			ret = genericEAO.findById((Serializable)pId);
		}
		
		return ret;
	}
	
	/**
	 * If the external EAO is set and also the findAll method is defined, the
	 * findAll method from the external EAO is called. Otherwise the findAll
	 * method from the genericEAO
	 * 
	 * @param pEntityClass the entity Class
	 * @return the entities in a Collection
	 * @throws DataSourceException if finding failed.
	 */
	public Collection findAll(Class pEntityClass) throws DataSourceException
	{
		Object ret = null;
		
		if (externalEAO != null && findByIdMethodName != null)
		{
			try
			{
				if (findAllMethod == null)
				{
					findAllMethod = externalEAO.getClass().getMethod(findAllMethodName);
				}
				
				ret = findAllMethod.invoke(externalEAO);
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
		
		return (Collection)ret;
	}
	
	/**
	 * Reads and sets the values for the given entity from the db.
	 * 
	 * @param pEntity The entity-object
	 * @param pEntityClass the class of the entity
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
	 * @param pCountCriteriaQuery the CriteriaQuery
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
	 * @param pCriteriaQuery the CriteriaQuery
	 * @param pOffset the first Row Index
	 * @param pMax Maximum Number of Entities
	 * @return the entities in a collection
	 */
	public Collection findByCriteria(CriteriaQuery pCriteriaQuery, int pOffset, int pMax)
	{
		
		entityManager.getTransaction().begin();
		
		Query query = entityManager.createQuery(pCriteriaQuery);
		query.setFirstResult(pOffset);
		query.setMaxResults(pMax);
		List objectList = (List)query.getResultList();
		
		entityManager.getTransaction().commit();
		
		return objectList;
	}
	
	/**
	 * Returns the Entities for the given criteriaQuery.
	 * 
	 * @param pCriteriaQuery the CriteriaQuery
	 * @return the Entities in a Collection
	 */
	public Collection findByCriteria(CriteriaQuery pCriteriaQuery)
	{
		entityManager.getTransaction().begin();
		
		Query query = entityManager.createQuery(pCriteriaQuery);
		List objectList = (List)query.getResultList();
		
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
	 * @param pEntityManager the EntityManager
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
	 * @return The Metamodel
	 */
	public Metamodel getMetamodel()
	{
		return metamodel;
	}
	
	/**
	 * Returns the persistence unit util.
	 * 
	 * @return the PersistenceUnitUtil
	 */
	public PersistenceUnitUtil getPersistenceUnitUtil()
	{
		return persistenceUnitUtil;
	}
	
	/**
	 * Returns the PrimaryKey from the given entity.
	 * 
	 * @param pEntity the entity of which to get the primary key.
	 * @return The PrimaryKey from the given entity
	 */
	public Object getIdentifier(Object pEntity)
	{
		return persistenceUnitUtil.getIdentifier(pEntity);
	}
	
	/**
	 * Returns the <code>EntityType</code> for the given entityClass.
	 * 
	 * @param pEntityClass the class of the entity
	 * @return the <code>EntityType</code> for the given entityClass.
	 */
	public EntityType getEntityType(Class pEntityClass)
	{
		return metamodel.entity(pEntityClass);
	}
	
	/**
	 * Returns the <code>Attributes</code> for the given entityClass Attributes
	 * are the Fields from the entityClass.
	 * 
	 * @param pEntityClass the class of the entity
	 * @return <code>Attributes</code> for the given entityClass in a Collection
	 */
	public Set<Attribute> getAttributes(Class pEntityClass)
	{
		return metamodel.entity(pEntityClass).getAttributes();
	}
	
	/**
	 * Returns the <code>EmbeddableType</code> for the given embeddableClass.
	 * 
	 * @param pEmbeddableClass the class of the embedded class
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
