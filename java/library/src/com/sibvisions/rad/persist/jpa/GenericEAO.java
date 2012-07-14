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
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;

/**
 * Implemented Methods to access the database by entities and primary keys.
 * 
 * @author Stefan Wurm
 *
 * @param <E> The Type of the Entity
 * @param <PK> The Type of the Primary Key
 */
public class GenericEAO<E, PK  extends Serializable> implements IGenericEAO<E, PK> 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/** The Entity Manager. */
	private EntityManager entityManager;
	
	/** The Class for the entity. */
	private Class<E> entityClass;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * To set the Entity Manager.
	 * 
	 * @param pEntityManager The Entity Manager
	 */
	public GenericEAO(EntityManager pEntityManager) 
	{	
		setEntityManager(pEntityManager);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Interface implementation
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * {@inheritDoc}
	 */
	public E insert(E entity) 
	{
		try 
		{
			
	        entityManager.getTransaction().begin();
	
	        entityManager.persist(entity);
	                
	        entityManager.getTransaction().commit();
	        
		}  
		catch (IllegalStateException ise) 
		{
			// getTransaction throws an IllegalStateException if the entityManager is container managed
			entityManager.persist(entity);
		}

        return entity;
	}
		
	/**
	 * {@inheritDoc}
	 */
	public void update(E entity) 
	{
		
		try 
		{
			
	        entityManager.getTransaction().begin();
	    	
	        entityManager.merge(entity);
	        
	        entityManager.getTransaction().commit();
        
		} 
		catch (IllegalStateException ise) 
		{
			// getTransaction throws an IllegalStateException if the entityManager is container managed
			entityManager.merge(entity);
		}        
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void delete(E entity) 
	{
		try 
		{
			
        entityManager.getTransaction().begin();
    	
        entityManager.remove(entity);
        
        entityManager.getTransaction().commit();	
        
		} 
		catch (IllegalStateException ise) 
		{
			// getTransaction throws an IllegalStateException if the entityManager is container managed
			entityManager.remove(entity);
		}        
	}
	
	/**
	 * {@inheritDoc}
	 */
	public E findById(PK id) 
	{
		
		E entity = null;
		
		try 
		{
			
	        entityManager.getTransaction().begin();
	    	
	        entity = entityManager.find(entityClass, id);
	
	        entityManager.getTransaction().commit();
        
		}
		catch (IllegalStateException ise) 
		{
			// getTransaction throws an IllegalStateException if the entityManager is container managed
			entity = entityManager.find(entityClass, id);
		}   
        
        return entity;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Collection<E> findAll() 
	{
	
		CriteriaQuery criteriaQuery = entityManager.getCriteriaBuilder().createQuery();
		From from = criteriaQuery.from(entityClass);
		criteriaQuery.select(from);
		
        Query query = entityManager.createQuery(criteriaQuery);
        
        List<E> objectList = null;
		
		try 
		{
			
	        entityManager.getTransaction().begin();
	        
	        objectList = (List<E>) query.getResultList();
	              
	        entityManager.getTransaction().commit();
        
		}
		catch (IllegalStateException ise) 
		{
			// getTransaction throws an IllegalStateException if the entityManager is container managed
			objectList = (List<E>) query.getResultList();
		}           
        
        return objectList;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Setter Methode for the Entity Manager.
	 * 
	 * @param pEntityManager The Entity Manager for the entities
	 */
	public void setEntityManager(EntityManager pEntityManager) 
	{
		entityManager = pEntityManager;
	}

	/**
	 * Sets the class of the entity the DAO.
	 * 
	 * @param pEntityClass The Class for the entity
	 */
	public void setEntityClass(Class<E> pEntityClass) 
	{
		entityClass = pEntityClass;
	}
	
}	// GenericEAO
