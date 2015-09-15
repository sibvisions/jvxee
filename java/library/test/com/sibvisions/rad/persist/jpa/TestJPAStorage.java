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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.rad.model.condition.Equals;
import javax.rad.model.condition.ICondition;
import javax.rad.remote.MasterConnection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sibvisions.rad.model.remote.RemoteDataBook;
import com.sibvisions.rad.model.remote.RemoteDataSource;
import com.sibvisions.rad.persist.jpa.entity.Address;
import com.sibvisions.rad.persist.jpa.entity.Customer;
import com.sibvisions.rad.persist.jpa.entity.Education;
import com.sibvisions.rad.util.DirectObjectConnection;

/**
 * The class to test the JPAStorage.
 * 
 * @author Stefan Wurm
 * 		
 */
public class TestJPAStorage
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** The EntityManagerFactory. **/
	private EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpatest");
	
	/** The EntityManager. **/
	private EntityManager entityManager = null;
	
	/** The CriteriaBuilder. **/
	//	private CriteriaBuilder criteriaBuilder;
	
	/** The storage for the customer entities. **/
	private JPAStorage jpaStorageCustomer;
	
	/** The storage for the address entities. **/
	private JPAStorage jpaStorageAddress;
	
	/** The storage for the education entities. **/
	private JPAStorage jpaStorageEducation;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Initializes the storage objects.
	 * 
	 * @throws Exception if there is an exception
	 */
	@Before
	public void open() throws Exception
	{
		entityManager = emf.createEntityManager();
		//	      criteriaBuilder = emf.getCriteriaBuilder();
		clearDB();
		initializeDB();
		
		jpaStorageCustomer = new JPAStorage(Customer.class);
		jpaStorageCustomer.setEntityManager(entityManager);
		jpaStorageCustomer.open();
		
		jpaStorageAddress = new JPAStorage(Address.class);
		jpaStorageAddress.setEntityManager(entityManager);
		jpaStorageAddress.open();
		
		jpaStorageEducation = new JPAStorage(Customer.class);
		jpaStorageEducation.setEntityManager(entityManager);
		jpaStorageEducation.setDetailEntity(Education.class);
		jpaStorageEducation.open();
	}
	
	/**
	 * Close the EntityManager.
	 * 
	 * @throws Exception if there is an exception.
	 */
	@After
	public void close() throws Exception
	{
		entityManager.close();
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Initializes the database with default values.
	 * 
	 */
	public void initializeDB()
	{
		entityManager.getTransaction().begin();
		
		entityManager.createNativeQuery("insert into salutation (ID, SALUTATION) values (1, 'Mister')").executeUpdate();
		entityManager.createNativeQuery("insert into salutation (ID, SALUTATION) values (2, 'Mrs')").executeUpdate();
		
		entityManager.createNativeQuery("insert into healthinsurance (ID, NR, HEALTHINSURANCE, STREET, ZIP, CITY) " +
				"values (1, 10, 'Healthinsurance 1', 'Street 1', 1111, 'City 1')").executeUpdate();
		entityManager.createNativeQuery("insert into healthinsurance (ID, NR, HEALTHINSURANCE, STREET, ZIP, CITY) " +
				"values (2, 20, 'Healthinsurance 2', 'Street 2', 2222, 'City 2')").executeUpdate();
		entityManager.createNativeQuery("insert into healthinsurance (ID, NR, HEALTHINSURANCE, STREET, ZIP, CITY) " +
				"values (3, 30, 'Healthinsurance 3', 'Street 3', 3333, 'City 3')").executeUpdate();
				
		entityManager.createNativeQuery("insert into customer (ID, FIRSTNAME, LASTNAME, BIRTHDAY, PRIVATECUSTOMER, TELEPHONEPRIVATE, " +
				"TELEPHONEOFFICE, EMAIL, SALUTATION_ID, SALUTATION_SALUTATION, HEALTHINSURANCE_ID, HEALTHINSURANCE_NR) " +
				"values (1, 'Firstname 1', 'Lastname 1', null, true, '1111111111', '1111111111', 'first1.last1@gmx.at', " +
				"1, 'Mister',  1, 10)").executeUpdate();
		entityManager.createNativeQuery("insert into customer (ID, FIRSTNAME, LASTNAME, BIRTHDAY, PRIVATECUSTOMER, TELEPHONEPRIVATE, " +
				"TELEPHONEOFFICE, EMAIL, SALUTATION_ID, SALUTATION_SALUTATION, HEALTHINSURANCE_ID, HEALTHINSURANCE_NR) " +
				"values (2, 'Firstname 2', 'Lastname 2', null, true, '2222222222', '2222222222', 'first2.last2@gmx.at', " +
				"1, 'Mister',  1, 10)").executeUpdate();
		entityManager.createNativeQuery("insert into customer (ID, FIRSTNAME, LASTNAME, BIRTHDAY, PRIVATECUSTOMER, TELEPHONEPRIVATE, " +
				"TELEPHONEOFFICE, EMAIL, SALUTATION_ID, SALUTATION_SALUTATION, HEALTHINSURANCE_ID, HEALTHINSURANCE_NR) " +
				"values (3, 'Firstname 3', 'Lastname 3', null, false, '3333333333', '3333333333', 'first3.last3@gmx.at', " +
				"2, 'Mrs',  3, 30)").executeUpdate();
		entityManager.createNativeQuery("insert into customer (ID, FIRSTNAME, LASTNAME, BIRTHDAY, PRIVATECUSTOMER, TELEPHONEPRIVATE, " +
				"TELEPHONEOFFICE, EMAIL, SALUTATION_ID, SALUTATION_SALUTATION, HEALTHINSURANCE_ID, HEALTHINSURANCE_NR) " +
				"values (4, 'Firstname 4', 'Lastname 4', null, false, '4444444444', '4444444444', 'first4.last4@gmx.at', " +
				"2, 'Mrs',  3, 30)").executeUpdate();
				
		entityManager.createNativeQuery("insert into address values (1, 'City 1', 'Street 1',  1, 1)").executeUpdate();
		entityManager.createNativeQuery("insert into address values (2, 'City 2', 'Street 2',  2, 1)").executeUpdate();
		
		entityManager.createNativeQuery("insert into education values (1, 'Education 1')").executeUpdate();
		entityManager.createNativeQuery("insert into education values (2, 'Education 2')").executeUpdate();
		entityManager.createNativeQuery("insert into education values (3, 'Education 3')").executeUpdate();
		entityManager.createNativeQuery("insert into education values (4, 'Education 4')").executeUpdate();
		entityManager.createNativeQuery("insert into education values (5, 'Education 5')").executeUpdate();
		
		entityManager.createNativeQuery("insert into customer_education values (1, 1)").executeUpdate();
		entityManager.createNativeQuery("insert into customer_education values (1, 2)").executeUpdate();
		
		entityManager.flush();
		
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Delets all values from the database.
	 * 
	 */
	public void clearDB()
	{
		entityManager.getTransaction().begin();
		
		entityManager.createQuery("delete from Address").executeUpdate();
		entityManager.createQuery("delete from Customer").executeUpdate();
		entityManager.createQuery("delete from Salutation").executeUpdate();
		entityManager.createQuery("delete from Healthinsurance").executeUpdate();
		entityManager.createQuery("delete from Education").executeUpdate();
		
		entityManager.flush();
		
		entityManager.getTransaction().commit();
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Test methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	// MetaData
	//-----------------------------------
	
	/**
	 * Tests the initialization of the MetaData for the education entity.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testMetaDataEducation() throws Throwable
	{
		
		DirectObjectConnection con = new DirectObjectConnection();
		con.put("jpa", jpaStorageEducation);
		
		MasterConnection macon = new MasterConnection(con);
		macon.open();
		
		RemoteDataSource rds = new RemoteDataSource(macon);
		rds.open();
		
		RemoteDataBook rdb = new RemoteDataBook();
		rdb.setDataSource(rds);
		rdb.setName("jpa");
		rdb.open();
		
		Assert.assertArrayEquals(new String[] { "CUSTOMER_ID", "CUSTOMER_LASTNAME", "EDUCATION_ID", "EDUCATION_EDUCATION" }, rdb.getRowDefinition().getColumnNames());
	}
	
	/**
	 * Tests the initialization of the MetaData for the address entity.
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testMetaDataAddress() throws Throwable
	{
		DirectObjectConnection con = new DirectObjectConnection();
		con.put("jpa", jpaStorageAddress);
		
		MasterConnection macon = new MasterConnection(con);
		macon.open();
		
		RemoteDataSource rds = new RemoteDataSource(macon);
		rds.open();
		
		RemoteDataBook rdb = new RemoteDataBook();
		rdb.setDataSource(rds);
		rdb.setName("jpa");
		rdb.open();
		
		Assert.assertArrayEquals(new String[] { "ID", "ZIP", "STREET", "CUSTOMER_ID", "CUSTOMER_LASTNAME", "CITY" }, rdb.getRowDefinition().getColumnNames());
	}
	
	// Fetch
	//-----------------------------------
	
	/**
	 * Tests the fetch method from JPAStorageCustomer.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testFetch() throws Exception
	{
		List<Object[]> objects = jpaStorageCustomer.executeFetch(null, null, 0, 10);
		
		Assert.assertEquals(5, objects.size());
	}
	
	// Insert
	//-----------------------------------
	
	/**
	 * Test method to insert a customer.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInsertCustomer() throws Exception
	{
		/*
		 * ID, BIRTHDAY, EMAIL, TELEPHONE_PRIVATE, TELEPHONE_OFFICE, LASTNAME,
		 * HEALTHINSURANCE_ID, HEALTHINSURANCE_NR,
		 * HEALTHINSURANCE_HEALTH_INSURANCE, FIRSTNAME, SALUTATION_ID,
		 * SALUTATION, PRIVATECUSTOMER
		 */
		Object[] dataRow = { null, null, "New Mail", "12345", "12345", "New Lastname", null, null, null, "New Firstname", BigDecimal.valueOf(1), "Mister", Boolean.TRUE };
		
		dataRow = jpaStorageCustomer.executeInsert(dataRow);
		
		Assert.assertNotNull(dataRow[0]);
		
		entityManager.getTransaction().begin();
		
		Customer entity = entityManager.find(Customer.class, dataRow[0]);
		
		Assert.assertNotNull(entity);
		Assert.assertEquals(5, entity.getId());
		Assert.assertNull(entity.getBirthday());
		Assert.assertEquals(dataRow[2], entity.getCustomerContact().getEmail());
		Assert.assertEquals(dataRow[3], entity.getCustomerContact().getTelephonePrivate());
		Assert.assertEquals(dataRow[4], entity.getCustomerContact().getTelephoneOffice());
		Assert.assertEquals(dataRow[5], entity.getLastname());
		Assert.assertEquals(dataRow[9], entity.getFirstname());
		Assert.assertEquals(dataRow[10], new Long(entity.getSalutation().getSalutationPK().getId()));
		Assert.assertEquals(dataRow[11], entity.getSalutation().getSalutationPK().getSalutation());
		Assert.assertTrue(entity.isPrivateCustomer());
		
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Test method to insert an address.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInsertAddress() throws Exception
	{
		/* "ID", "ZIP", "STREET", "CUSTOMER_ID", "CUSTOMER_LASTNAME", "CITY" **/
		Object[] dataRow = { null, new BigDecimal(1234), "New street", "2", "Lastname 2", "New city" };
		
		dataRow = jpaStorageAddress.executeInsert(dataRow);
		
		Assert.assertNotNull(dataRow[0]);
		
		entityManager.getTransaction().begin();
		
		Address entity = entityManager.find(Address.class, dataRow[0]);
		
		Assert.assertNotNull(entity);
		Assert.assertEquals(3, entity.getId());
		Assert.assertEquals(dataRow[1], entity.getZip());
		Assert.assertEquals(dataRow[2], entity.getStreet());
		Assert.assertEquals(dataRow[3], Long.valueOf(entity.getCustomer().getId()));
		Assert.assertEquals(dataRow[4], entity.getCustomer().getLastname());
		Assert.assertEquals(dataRow[5], entity.getCity());
		
		entityManager.getTransaction().commit();
		
		entityManager.getTransaction().begin();
		
		Customer entityCust = entityManager.find(Customer.class, Long.valueOf(2));
		
		Assert.assertNotNull(entityCust);
		Assert.assertEquals(1, entityCust.getAddresses().size());
		
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Test method to insert an education.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInsertEducation() throws Exception
	{
		/*
		 * "CUSTOMER_ID", "CUSTOMER_LASTNAME", "EDUCATION_ID",
		 * "EDUCATION_EDUCATION"
		 */
		Object[] dataRow = { BigDecimal.valueOf(3), "Lastname 3", BigDecimal.valueOf(2), "Education 2" };
		
		dataRow = jpaStorageEducation.executeInsert(dataRow);
		
		Assert.assertNotNull(dataRow[0]);
		
		entityManager.getTransaction().begin();
		
		Customer entity = entityManager.find(Customer.class, dataRow[0]);
		
		Assert.assertNotNull(entity);
		Assert.assertEquals(3, entity.getId());
		Assert.assertEquals(1, entity.getEducations().size());
		
		entityManager.getTransaction().commit();
	}
	
	// Update
	//-----------------------------------
	
	/**
	 * Test method to update a customer.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdateCustomer() throws Exception
	{
		ICondition condition = new Equals("ID", BigDecimal.valueOf(3));
		
		List<Object[]> list = jpaStorageCustomer.executeFetch(condition, null, 0, 10);
		
		Assert.assertEquals(2, list.size()); // is 2 because of null
		
		Object[] dataRowOld = list.get(0);
		
		Object[] dataRowNew = dataRowOld;
		
		/*
		 * ID, BIRTHDAY, EMAIL, TELEPHONE_PRIVATE, TELEPHONE_OFFICE, LASTNAME,
		 * HEALTHINSURANCE_ID, HEALTHINSURANCE_NR,
		 * HEALTHINSURANCE_HEALTH_INSURANCE, FIRSTNAME, SALUTATION_ID,
		 * SALUTATION, PRIVATECUSTOMER
		 */
		dataRowNew[2] = "updatedMail";
		dataRowNew[10] = new BigDecimal(1);
		dataRowNew[11] = "Mister";
		
		dataRowNew = jpaStorageCustomer.executeUpdate(dataRowOld, dataRowNew);
		
		Assert.assertNotNull(dataRowNew);
		
		entityManager.getTransaction().begin();
		
		Customer entity = entityManager.find(Customer.class, Long.valueOf(3));
		
		Assert.assertNotNull(entity);
		Assert.assertEquals(3, entity.getId());
		Assert.assertNull(entity.getBirthday());
		Assert.assertEquals(dataRowNew[2], entity.getCustomerContact().getEmail());
		Assert.assertEquals(dataRowNew[3], entity.getCustomerContact().getTelephonePrivate());
		Assert.assertEquals(dataRowNew[4], entity.getCustomerContact().getTelephoneOffice());
		Assert.assertEquals(dataRowNew[5], entity.getLastname());
		Assert.assertEquals(dataRowNew[9], entity.getFirstname());
		Assert.assertEquals(dataRowNew[10], new Long(entity.getSalutation().getSalutationPK().getId()));
		Assert.assertEquals(dataRowNew[11], entity.getSalutation().getSalutationPK().getSalutation());
		Assert.assertTrue(!entity.isPrivateCustomer());
		
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Test method to update an address.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdateAddress() throws Exception
	{
		ICondition condition = new Equals("ID", BigDecimal.valueOf(2));
		
		List<Object[]> list = jpaStorageAddress.executeFetch(condition, null, 0, 10);
		
		Assert.assertEquals(2, list.size()); // is 2 because of null
		
		Object[] dataRowOld = list.get(0);
		
		Object[] dataRowNew = dataRowOld;
		
		/* "ID", "ZIP", "STREET", "CUSTOMER_ID", "CUSTOMER_LASTNAME", "CITY" **/
		dataRowNew[1] = new BigDecimal(8888);
		dataRowNew[2] = "Updated Street";
		dataRowNew[5] = "Updated City";
		
		dataRowNew = jpaStorageAddress.executeUpdate(dataRowOld, dataRowNew);
		
		Assert.assertNotNull(dataRowNew);
		
		entityManager.getTransaction().begin();
		
		Address entity = entityManager.find(Address.class, Long.valueOf(2));
		
		Assert.assertNotNull(entity);
		Assert.assertEquals(2, entity.getId());
		Assert.assertEquals(dataRowNew[1], entity.getZip());
		Assert.assertEquals(dataRowNew[2], entity.getStreet());
		Assert.assertEquals(dataRowNew[3], Long.valueOf(entity.getCustomer().getId()));
		Assert.assertEquals(dataRowNew[4], entity.getCustomer().getLastname());
		Assert.assertEquals(dataRowNew[5], entity.getCity());
		
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Test method to update an education.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdateEducation() throws Exception
	{
		/*
		 * "CUSTOMER_ID", "CUSTOMER_LASTNAME", "EDUCATION_ID",
		 * "EDUCATION_EDUCATION"
		 */
		Object[] dataRowOld = { BigDecimal.valueOf(1), "Lastname 1", BigDecimal.valueOf(2), "Education 2" };
		
		/*
		 * "CUSTOMER_ID", "CUSTOMER_LASTNAME", "EDUCATION_ID",
		 * "EDUCATION_EDUCATION"
		 */
		Object[] dataRowNew = { BigDecimal.valueOf(1), "Lastname 1", BigDecimal.valueOf(3), "Education 3" };
		
		dataRowNew = jpaStorageEducation.executeUpdate(dataRowOld, dataRowNew);
		
		Assert.assertNotNull(dataRowNew);
		
		entityManager.getTransaction().begin();
		
		Customer entity = entityManager.find(Customer.class, Long.valueOf(1));
		
		Assert.assertNotNull(entity);
		Assert.assertEquals(1, entity.getId());
		Assert.assertEquals(2, entity.getEducations().size());
		
		Education education = new ArrayList<Education>(entity.getEducations()).get(1);
		
		Assert.assertEquals(3, education.getId());
		Assert.assertEquals("Education 3", education.getEducation());
		
		entityManager.getTransaction().commit();
	}
	
	// Delete
	//-----------------------------------
	
	/**
	 * Test method to delete a customer.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDeleteCustomer() throws Exception
	{
		ICondition condition = new Equals("ID", BigDecimal.valueOf(3));
		
		List<Object[]> list = jpaStorageCustomer.executeFetch(condition, null, 0, 10);
		
		Assert.assertEquals(2, list.size()); // is 2 because of null
		
		Object[] dataRowDelete = list.get(0);
		
		jpaStorageCustomer.executeDelete(dataRowDelete);
		
		entityManager.getTransaction().begin();
		
		Customer entity = entityManager.find(Customer.class, Long.valueOf(3));
		
		Assert.assertNull(entity);
		
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Test method to delete an address.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDeleteAddress() throws Exception
	{
		ICondition condition = new Equals("ID", BigDecimal.valueOf(2));
		
		List<Object[]> list = jpaStorageAddress.executeFetch(condition, null, 0, 10);
		
		Assert.assertEquals(2, list.size()); // is 2 because of null
		
		Object[] dataRowDelete = list.get(0);
		
		jpaStorageAddress.executeDelete(dataRowDelete);
		
		entityManager.getTransaction().begin();
		
		Address entity = entityManager.find(Address.class, Long.valueOf(2));
		
		Assert.assertNull(entity);
		
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Test method to delete an education.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDeleteEducation() throws Exception
	{
		/*
		 * "CUSTOMER_ID", "CUSTOMER_LASTNAME", "EDUCATION_ID",
		 * "EDUCATION_EDUCATION"
		 */
		Object[] dataRowDelete = { BigDecimal.valueOf(1), "Lastname 1", BigDecimal.valueOf(2), "Education 2" };
		
		jpaStorageEducation.executeDelete(dataRowDelete);
		
		entityManager.getTransaction().begin();
		
		Customer entity = entityManager.find(Customer.class, Long.valueOf(1));
		
		Assert.assertNotNull(entity);
		Assert.assertEquals(1, entity.getId());
		Assert.assertEquals(1, entity.getEducations().size());
		
		entityManager.getTransaction().commit();
	}
	
	// Refetch
	//-----------------------------------
	
	/**
	 * Test method to refetch a customer.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRefetchCustomer() throws Exception
	{
		ICondition condition = new Equals("ID", BigDecimal.valueOf(3));
		
		List<Object[]> list = jpaStorageCustomer.executeFetch(condition, null, 0, 10);
		
		Assert.assertEquals(2, list.size()); // is 2 because of null
		
		Object[] dataRowRefetch = list.get(0);
		
		/*
		 * ID, BIRTHDAY, EMAIL, TELEPHONE_PRIVATE, TELEPHONE_OFFICE, LASTNAME,
		 * HEALTHINSURANCE_ID, HEALTHINSURANCE_NR,
		 * HEALTHINSURANCE_HEALTH_INSURANCE, FIRSTNAME, SALUTATION_ID,
		 * SALUTATION, PRIVATECUSTOMER
		 */
		dataRowRefetch[2] = "updatedMail";
		dataRowRefetch[10] = BigDecimal.valueOf(1);
		dataRowRefetch[11] = "Mister";
		
		Object[] dataRowRefetch2 = jpaStorageCustomer.executeRefetchRow(dataRowRefetch);
		
		Assert.assertEquals("first3.last3@gmx.at", dataRowRefetch2[2]);
		Assert.assertEquals(new Long(2), dataRowRefetch2[10]);
		Assert.assertEquals("Mrs", dataRowRefetch2[11]);
	}
	
}	// TestJPAStorage
