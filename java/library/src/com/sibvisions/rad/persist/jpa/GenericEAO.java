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

	private EntityManager entityManager;
	
	private Class<E> entityClass;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
		
        entityManager.getTransaction().begin();
    	
        entityManager.persist(entity);
                
        entityManager.getTransaction().commit();

        return entity;
	}
		
	/**
	 * {@inheritDoc}
	 */
	public void update(E entity) 
	{
		
        entityManager.getTransaction().begin();
    	
        entityManager.merge(entity);
        
        entityManager.getTransaction().commit();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void delete(E entity) 
	{
        entityManager.getTransaction().begin();
    	
        entityManager.remove(entity);
        
        entityManager.getTransaction().commit();	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public E findById(PK id) 
	{
		
        entityManager.getTransaction().begin();
    	
        E entity = entityManager.find(entityClass, id);

        entityManager.getTransaction().commit();
        
        return entity;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Collection<E> findAll() 
	{
        entityManager.getTransaction().begin();
        
		CriteriaQuery criteriaQuery = entityManager.getCriteriaBuilder().createQuery();
		From from = criteriaQuery.from(entityClass);
		criteriaQuery.select(from);
		
        Query query = entityManager.createQuery(criteriaQuery);
        List<E> objectList = (List<E>) query.getResultList();
              
//TODO remove debug code???        
//        Query  query = entityManager.createQuery("select "+entityClass.getSimpleName()+" from " + entityClass.getSimpleName()+" as "+entityClass.getSimpleName());       
//        List<E> objectList = (List<E>) query.getResultList();

        entityManager.getTransaction().commit();
        
        return objectList;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public void setEntityManager(EntityManager pEntityManager) 
	{
		entityManager = pEntityManager;
	}

	/**
	 * Sets the class of the entity the DAO.
	 * 
	 * @param entityClass
	 */
	public void setEntityClass(Class<E> pEntityClass) 
	{
		entityClass = pEntityClass;
	}
	
}	// GenericEAO
