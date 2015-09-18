/*
 * Copyright 2015 SIB Visions GmbH
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
 * 15.09.2015 - [RZ] - creation
 */
package com.sibvisions.rad.persist.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;
import javax.rad.model.SortDefinition;
import javax.rad.model.condition.Equals;
import javax.rad.model.condition.Not;
import javax.rad.model.condition.Or;
import javax.rad.persist.DataSourceException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sibvisions.rad.persist.jpa.entity.Customer;

/**
 * Tests the {@link TestConditionCriteriaMapper}.
 * 
 * @author Robert Zenz
 */
public class TestConditionCriteriaMapper
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** The EntityManagerFactory. **/
	private EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpatest");
	
	/** The EntityManager. **/
	private EntityManager entityManager = null;
	
	/** The jpa storage. */
	private JPAStorage jpaStorage;
	
	/** The condition criteria mapper. */
	private ConditionCriteriaMapper conditionCriteriaMapper;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Initializes the storage.
	 *
	 * @throws DataSourceException if that failed.
	 */
	@Before
	public void setUp() throws DataSourceException
	{
		entityManager = emf.createEntityManager();
		
		jpaStorage = new JPAStorage(Customer.class);
		jpaStorage.setEntityManager(entityManager);
		jpaStorage.open();
		
		conditionCriteriaMapper = new ConditionCriteriaMapper(jpaStorage.getJPAServerMetaData(), entityManager.getCriteriaBuilder());
	}
	
	/**
	 * Destroys the entity manager.
	 */
	@After
	public void tearDown()
	{
		entityManager.close();
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Test methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Tests the
	 * {@link ConditionCriteriaMapper#getCriteriaQuery(javax.rad.model.condition.ICondition, SortDefinition, Class, String)}
	 * method.
	 * 
	 * @throws DataSourceException if the test fails.
	 */
	@Test
	public void testGetCriteriaQuery() throws DataSourceException
	{
		CriteriaQuery criteriaQuery = conditionCriteriaMapper.getCriteriaQuery(
				null,
				null,
				Customer.class,
				null);
		Assert.assertNull(criteriaQuery.getGroupList());
		Assert.assertNull(criteriaQuery.getGroupRestriction());
		Assert.assertNull(criteriaQuery.getOrderList());
		Assert.assertTrue(criteriaQuery.getParameters().isEmpty());
		Assert.assertNull(criteriaQuery.getRestriction());
		Assert.assertNull(criteriaQuery.getSelection());
		
		criteriaQuery = conditionCriteriaMapper.getCriteriaQuery(
				new Equals("FIRSTNAME", "Test"),
				null,
				Customer.class,
				null);
		Assert.assertNull(criteriaQuery.getGroupList());
		Assert.assertNull(criteriaQuery.getGroupRestriction());
		Assert.assertNull(criteriaQuery.getOrderList());
		Assert.assertTrue(criteriaQuery.getParameters().isEmpty());
		Assert.assertNotNull(criteriaQuery.getRestriction());
		Assert.assertNotNull(criteriaQuery.getSelection());
		
		criteriaQuery = conditionCriteriaMapper.getCriteriaQuery(
				new Not(new Equals("FIRSTNAME", "Test")),
				null,
				Customer.class,
				null);
		Assert.assertNull(criteriaQuery.getGroupList());
		Assert.assertNull(criteriaQuery.getGroupRestriction());
		Assert.assertNull(criteriaQuery.getOrderList());
		Assert.assertTrue(criteriaQuery.getParameters().isEmpty());
		Assert.assertNotNull(criteriaQuery.getRestriction());
		Assert.assertNotNull(criteriaQuery.getSelection());
		
		criteriaQuery = conditionCriteriaMapper.getCriteriaQuery(
				new Or(new Equals("FIRSTNAME", "Test"), new Equals("FIRSTNAME", "Something")),
				null,
				Customer.class,
				null);
		Assert.assertNull(criteriaQuery.getGroupList());
		Assert.assertNull(criteriaQuery.getGroupRestriction());
		Assert.assertNull(criteriaQuery.getOrderList());
		Assert.assertTrue(criteriaQuery.getParameters().isEmpty());
		Assert.assertNotNull(criteriaQuery.getRestriction());
		Assert.assertNotNull(criteriaQuery.getSelection());
		
		criteriaQuery = conditionCriteriaMapper.getCriteriaQuery(
				null,
				new SortDefinition("FIRSTNAME", "LASTNAME"),
				Customer.class,
				null);
		Assert.assertNull(criteriaQuery.getGroupList());
		Assert.assertNull(criteriaQuery.getGroupRestriction());
		Assert.assertNotNull(criteriaQuery.getOrderList());
		Assert.assertTrue(criteriaQuery.getParameters().isEmpty());
		Assert.assertNull(criteriaQuery.getRestriction());
		Assert.assertNotNull(criteriaQuery.getSelection());
		
		criteriaQuery = conditionCriteriaMapper.getCriteriaQuery(
				new Equals("FIRSTNAME", "Test"),
				new SortDefinition("FIRSTNAME", "LASTNAME"),
				Customer.class,
				null);
		Assert.assertNull(criteriaQuery.getGroupList());
		Assert.assertNull(criteriaQuery.getGroupRestriction());
		Assert.assertNotNull(criteriaQuery.getOrderList());
		Assert.assertTrue(criteriaQuery.getParameters().isEmpty());
		Assert.assertNotNull(criteriaQuery.getRestriction());
		Assert.assertNotNull(criteriaQuery.getSelection());
		
		criteriaQuery = conditionCriteriaMapper.getCriteriaQuery(
				null,
				null,
				Customer.class,
				"LASTNAME");
		Assert.assertNull(criteriaQuery.getGroupList());
		Assert.assertNull(criteriaQuery.getGroupRestriction());
		Assert.assertNull(criteriaQuery.getOrderList());
		Assert.assertTrue(criteriaQuery.getParameters().isEmpty());
		Assert.assertNull(criteriaQuery.getRestriction());
		Assert.assertNull(criteriaQuery.getSelection());
	}
	
	/**
	 * Tests the
	 * {@link ConditionCriteriaMapper#getCountCriteriaQuery(javax.rad.model.condition.ICondition, Class, String)}
	 * method.
	 * 
	 * @throws DataSourceException if the test fails.
	 */
	@Test
	public void testGetCountCriteriaQuery() throws DataSourceException
	{
		CriteriaQuery criteriaQuery = conditionCriteriaMapper.getCountCriteriaQuery(
				null,
				Customer.class,
				null);
		Assert.assertNull(criteriaQuery.getGroupList());
		Assert.assertNull(criteriaQuery.getGroupRestriction());
		Assert.assertNull(criteriaQuery.getOrderList());
		Assert.assertTrue(criteriaQuery.getParameters().isEmpty());
		Assert.assertNull(criteriaQuery.getRestriction());
		Assert.assertNotNull(criteriaQuery.getSelection());
		
		criteriaQuery = conditionCriteriaMapper.getCountCriteriaQuery(
				new Equals("FIRSTNAME", "Test"),
				Customer.class,
				null);
		Assert.assertNull(criteriaQuery.getGroupList());
		Assert.assertNull(criteriaQuery.getGroupRestriction());
		Assert.assertNull(criteriaQuery.getOrderList());
		Assert.assertTrue(criteriaQuery.getParameters().isEmpty());
		Assert.assertNotNull(criteriaQuery.getRestriction());
		Assert.assertNotNull(criteriaQuery.getSelection());
		
		criteriaQuery = conditionCriteriaMapper.getCountCriteriaQuery(
				new Not(new Equals("FIRSTNAME", "Test")),
				Customer.class,
				null);
		Assert.assertNull(criteriaQuery.getGroupList());
		Assert.assertNull(criteriaQuery.getGroupRestriction());
		Assert.assertNull(criteriaQuery.getOrderList());
		Assert.assertTrue(criteriaQuery.getParameters().isEmpty());
		Assert.assertNotNull(criteriaQuery.getRestriction());
		Assert.assertNotNull(criteriaQuery.getSelection());
		
		criteriaQuery = conditionCriteriaMapper.getCountCriteriaQuery(
				new Or(new Equals("FIRSTNAME", "Test"), new Equals("FIRSTNAME", "Something")),
				Customer.class,
				null);
		Assert.assertNull(criteriaQuery.getGroupList());
		Assert.assertNull(criteriaQuery.getGroupRestriction());
		Assert.assertNull(criteriaQuery.getOrderList());
		Assert.assertTrue(criteriaQuery.getParameters().isEmpty());
		Assert.assertNotNull(criteriaQuery.getRestriction());
		Assert.assertNotNull(criteriaQuery.getSelection());
	}
	
}	// TestConditionCriteriaMapper
