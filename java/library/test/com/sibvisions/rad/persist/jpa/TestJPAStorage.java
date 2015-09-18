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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.rad.model.condition.Equals;
import javax.rad.model.condition.ICondition;
import javax.rad.model.condition.Like;
import javax.rad.persist.DataSourceException;
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
import com.sibvisions.rad.persist.jpa.entity.flight.Flight;
import com.sibvisions.rad.util.DirectObjectConnection;
import com.sibvisions.util.type.FileUtil;
import com.sibvisions.util.type.ResourceUtil;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

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
	
	/** The storage for the customer entities. **/
	private JPAStorage jpaStorageCustomer;
	
	/** The storage for the address entities. **/
	private JPAStorage jpaStorageAddress;
	
	/** The storage for the education entities. **/
	private JPAStorage jpaStorageEducation;
	
	/** The storage for the flight entities. **/
	private JPAStorage jpaStorageFlight;
	
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
		
		jpaStorageFlight = new JPAStorage(Flight.class);
		jpaStorageFlight.setEntityManager(entityManager);
		jpaStorageFlight.open();
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
		
		// Flights
		entityManager.createNativeQuery("insert into airport"
				+ "(CODE, NAME, COUNTRY, LOCATION)"
				+ "values ('JFK', 'John F. Kennedy International Airport', 'USA', 'New York')").executeUpdate();
		entityManager.createNativeQuery("insert into airport"
				+ "(CODE, NAME, COUNTRY, LOCATION)"
				+ "values ('SFO', 'San Francisco International Airport', 'USA', 'San Francisco')").executeUpdate();
		entityManager.createNativeQuery("insert into airport"
				+ "(CODE, NAME, COUNTRY, LOCATION)"
				+ "values ('BOS', 'Logan International Airport', 'USA', 'Boston')").executeUpdate();
		entityManager.createNativeQuery("insert into airport"
				+ "(CODE, NAME, COUNTRY, LOCATION)"
				+ "values ('IAD', 'Washington Dulles International Airport', 'USA', 'Washington')").executeUpdate();
		entityManager.createNativeQuery("insert into airport"
				+ "(CODE, NAME, COUNTRY, LOCATION)"
				+ "values ('VIE', 'Vienna International Airport', 'Austria', 'Vienna/Schwechat')").executeUpdate();
		entityManager.createNativeQuery("insert into airport"
				+ "(CODE, NAME, COUNTRY, LOCATION)"
				+ "values ('LHR', 'London Heathrow Airport', 'United Kingdom', 'London')").executeUpdate();
		entityManager.createNativeQuery("insert into airport"
				+ "(CODE, NAME, COUNTRY, LOCATION)"
				+ "values ('BER', 'Berlin Brandenburg Airport', 'Germany', 'Berlin')").executeUpdate();
				
		entityManager.createNativeQuery("insert into aircraft"
				+ "(REGISTRATIONNUMBER, COUNTRY, DESCRIPTION)"
				+ "values ('LX-VCH', 'LX', 'Boeing 747')").executeUpdate();
		entityManager.createNativeQuery("insert into aircraft"
				+ "(REGISTRATIONNUMBER, COUNTRY, DESCRIPTION)"
				+ "values ('G-EUPH', 'G', 'Airbus A319')").executeUpdate();
		entityManager.createNativeQuery("insert into aircraft"
				+ "(REGISTRATIONNUMBER, COUNTRY, DESCRIPTION)"
				+ "values ('HB-FVD', 'HB', 'Pilatus PC12')").executeUpdate();
		entityManager.createNativeQuery("insert into aircraft"
				+ "(REGISTRATIONNUMBER, COUNTRY, DESCRIPTION)"
				+ "values ('HL7414', 'HL', 'Boeing 747')").executeUpdate();
		entityManager.createNativeQuery("insert into aircraft"
				+ "(REGISTRATIONNUMBER, COUNTRY, DESCRIPTION)"
				+ "values ('PH-MCY', 'PH', 'McDonnell Douglas MD-11F')").executeUpdate();
		entityManager.createNativeQuery("insert into aircraft"
				+ "(REGISTRATIONNUMBER, COUNTRY, DESCRIPTION)"
				+ "values ('EI-IMC', 'EI', 'Airbus A319')").executeUpdate();
		entityManager.createNativeQuery("insert into aircraft"
				+ "(REGISTRATIONNUMBER, COUNTRY, DESCRIPTION)"
				+ "values ('D-EPHH', 'D', 'Piper PA46')").executeUpdate();
		entityManager.createNativeQuery("insert into aircraft"
				+ "(REGISTRATIONNUMBER, COUNTRY, DESCRIPTION)"
				+ "values ('PH-KZU', 'PH', 'Fokker 70')").executeUpdate();
				
		entityManager.createNativeQuery("delete from flight").executeUpdate();
		entityManager.createNativeQuery("insert into flight"
				+ "(AIRLINE, FLIGHTNUMBER, AIRCRAFT_REGISTRATIONNUMBER, AIRPORTORIGIN_CODE, AIRPORTDESTINATION_CODE)"
				+ "values ('British Airways', '180', 'HB-FVD', 'JFK', 'LHR')").executeUpdate();
		entityManager.createNativeQuery("insert into flight"
				+ "(AIRLINE, FLIGHTNUMBER, AIRCRAFT_REGISTRATIONNUMBER, AIRPORTORIGIN_CODE, AIRPORTDESTINATION_CODE)"
				+ "values ('United', '415', 'PH-MCY', 'JFK', 'SFO')").executeUpdate();
		entityManager.createNativeQuery("insert into flight"
				+ "(AIRLINE, FLIGHTNUMBER, AIRCRAFT_REGISTRATIONNUMBER, AIRPORTORIGIN_CODE, AIRPORTDESTINATION_CODE)"
				+ "values ('JetBlue', '157', 'D-EPHH', 'BOS', 'IAD')").executeUpdate();
		entityManager.createNativeQuery("insert into flight"
				+ "(AIRLINE, FLIGHTNUMBER, AIRCRAFT_REGISTRATIONNUMBER, AIRPORTORIGIN_CODE, AIRPORTDESTINATION_CODE)"
				+ "values ('Virgin Atlantic', '20', 'G-EUPH', 'SFO', 'LHR')").executeUpdate();
		entityManager.createNativeQuery("insert into flight"
				+ "(AIRLINE, FLIGHTNUMBER, AIRCRAFT_REGISTRATIONNUMBER, AIRPORTORIGIN_CODE, AIRPORTDESTINATION_CODE)"
				+ "values ('Germanwings', '8461', 'LX-VCH', 'LHR', 'BER')").executeUpdate();
		entityManager.createNativeQuery("insert into flight"
				+ "(AIRLINE, FLIGHTNUMBER, AIRCRAFT_REGISTRATIONNUMBER, AIRPORTORIGIN_CODE, AIRPORTDESTINATION_CODE)"
				+ "values ('Airberlin', '7248', 'PH-KZU', 'BER', 'JFK')").executeUpdate();
				
		entityManager.flush();
		
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Deletes all values from the database.
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
		entityManager.createQuery("delete from Flight").executeUpdate();
		entityManager.createQuery("delete from Aircraft").executeUpdate();
		entityManager.createQuery("delete from Airport").executeUpdate();
		
		entityManager.flush();
		
		entityManager.getTransaction().commit();
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Test methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Test method to delete an address.
	 * 
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testDeleteAddress() throws Exception
	{
		ICondition condition = new Equals("ID", BigDecimal.valueOf(2));
		
		List<Object[]> list = jpaStorageAddress.executeFetch(condition, null, 0, 10);
		
		// is 2 because of null
		Assert.assertEquals(2, list.size());
		
		Object[] dataRowDelete = list.get(0);
		
		jpaStorageAddress.executeDelete(dataRowDelete);
		
		entityManager.getTransaction().begin();
		
		Address entity = entityManager.find(Address.class, Long.valueOf(2));
		
		Assert.assertNull(entity);
		
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Test method to delete a customer.
	 * 
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testDeleteCustomer() throws Exception
	{
		ICondition condition = new Equals("ID", BigDecimal.valueOf(3));
		
		List<Object[]> list = jpaStorageCustomer.executeFetch(condition, null, 0, 10);
		
		// is 2 because of null
		Assert.assertEquals(2, list.size());
		
		Object[] dataRowDelete = list.get(0);
		
		jpaStorageCustomer.executeDelete(dataRowDelete);
		
		entityManager.getTransaction().begin();
		
		Customer entity = entityManager.find(Customer.class, Long.valueOf(3));
		
		Assert.assertNull(entity);
		
		entityManager.getTransaction().commit();
	}
	
	/**
	 * Test method to delete an education.
	 * 
	 * @throws Exception if the test fails.
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
	
	/**
	 * Tests if an exception occurs if the master entity is {@code null}.
	 * 
	 * @throws DataSourceException if the test fails.
	 */
	@Test
	public void testExceptionOnNullMasterEntity() throws DataSourceException
	{
		JPAStorage storage = null;
		
		try
		{
			storage = new JPAStorage(null);
			
			Assert.fail("JPAStorage does accept a null master entity in it's constructor, this shouldn't be allowed!");
		}
		catch (IllegalArgumentException e)
		{
			// Everything is fine, carry on.
		}
		
		storage = new JPAStorage(Customer.class);
		
		try
		{
			storage.setMasterEntity(null);
			
			Assert.fail("JPAStorage does accept a null master entity, this shouldn't be allowed!");
		}
		catch (IllegalArgumentException e)
		{
			// Everything is fine, carry on.
		}
	}
	
	/**
	 * Tests the fetch method from JPAStorageCustomer.
	 * 
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testFetch() throws Exception
	{
		List<Object[]> objects = jpaStorageCustomer.executeFetch(null, null, 0, 10);
		
		Assert.assertEquals(5, objects.size());
	}
	
	/**
	 * Tests the {@link JPAStorage#getEstimatedRowCount(ICondition)} function.
	 * 
	 * @throws DataSourceException if the test fails.
	 */
	@Test
	public void testGetEstimatedRowCount() throws DataSourceException
	{
		Assert.assertEquals(4, jpaStorageCustomer.getEstimatedRowCount(null));
		Assert.assertEquals(4, jpaStorageCustomer.getEstimatedRowCount(new Like("FIRSTNAME", "Firstname *")));
		Assert.assertEquals(1, jpaStorageCustomer.getEstimatedRowCount(new Equals("FIRSTNAME", "Firstname 3")));
		Assert.assertEquals(0, jpaStorageCustomer.getEstimatedRowCount(new Equals("FIRSTNAME", "Non Existent")));
		
		Assert.assertEquals(6, jpaStorageFlight.getEstimatedRowCount(null));
	}
	
	/**
	 * Tests the {@link JPAStorage#getName()} function.
	 * 
	 * @throws DataSourceException if the test fails.
	 */
	@Test
	public void testGetName() throws DataSourceException
	{
		JPAStorage storage = new JPAStorage(Customer.class);
		
		Assert.assertEquals("customer", storage.getName());
		
		storage.setDetailEntity(Address.class);
		
		Assert.assertEquals("customeraddress", storage.getName());
	}
	
	/**
	 * Test method to insert a customer.
	 * 
	 * @throws Exception if the test fails.
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
	 * @throws Exception if the test fails.
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
	 * @throws Exception if the test fails.
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
	
	/**
	 * Tests the initialization of the MetaData for the education entity.
	 * 
	 * @throws Throwable if the test fails.
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
	 * @throws Throwable if the test fails.
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
	
	/**
	 * Tests the multiple foreign keys are producing correctly named columns.
	 * 
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testMultipleForeignKeysColumnNames() throws Exception
	{
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("flight", jpaStorageFlight);
		
		// We will be testing the naming of the columns and if they are
		// correctly mapped. For that we will create a RemoteDataBook.
		
		RemoteDataSource dataSource = new RemoteDataSource(new MasterConnection(new DirectObjectConnection(map)));
		dataSource.open();
		
		RemoteDataBook dataBook = new RemoteDataBook();
		dataBook.setDataSource(dataSource);
		dataBook.setName("flight");
		dataBook.open();
		
		dataBook.insert(false);
		dataBook.setValue("AIRLINE", "Personal Aviation Stuffy Inc.");
		dataBook.setValue("FLIGHTNUMBER", "2634");
		dataBook.setValue("AIRCRAFT_REGISTRATIONNUMBER", "PH-MCY");
		dataBook.setValue("AIRPORTORIGIN_CODE", "VIE");
		dataBook.setValue("AIRPORTDESTINATION_CODE", "SFO");
		dataBook.saveAllRows();
		dataBook.reload();
		
		dataBook.setSelectedRow(dataBook.searchNext(new Equals("FLIGHTNUMBER", "2634")));
		
		// Now check that everything was correctly inserted.
		Assert.assertEquals("Personal Aviation Stuffy Inc.", dataBook.getValue("AIRLINE"));
		Assert.assertEquals("2634", dataBook.getValue("FLIGHTNUMBER"));
		Assert.assertEquals("PH-MCY", dataBook.getValue("AIRCRAFT_REGISTRATIONNUMBER"));
		Assert.assertEquals("VIE", dataBook.getValue("AIRPORTORIGIN_CODE"));
		Assert.assertEquals("Vienna/Schwechat", dataBook.getValue("AIRPORTORIGIN_LOCATION"));
		Assert.assertEquals("SFO", dataBook.getValue("AIRPORTDESTINATION_CODE"));
		Assert.assertEquals("San Francisco", dataBook.getValue("AIRPORTDESTINATION_LOCATION"));
	}
	
	/**
	 * Test method to update a customer.
	 * 
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testUpdateCustomer() throws Exception
	{
		ICondition condition = new Equals("ID", BigDecimal.valueOf(3));
		
		List<Object[]> list = jpaStorageCustomer.executeFetch(condition, null, 0, 10);
		
		// is 2 because of null
		Assert.assertEquals(2, list.size());
		
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
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testUpdateAddress() throws Exception
	{
		ICondition condition = new Equals("ID", BigDecimal.valueOf(2));
		
		List<Object[]> list = jpaStorageAddress.executeFetch(condition, null, 0, 10);
		
		// is 2 because of null
		Assert.assertEquals(2, list.size());
		
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
	 * @throws Exception if the test fails.
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
	
	/**
	 * Test method to refetch a customer.
	 * 
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testRefetchCustomer() throws Exception
	{
		ICondition condition = new Equals("ID", BigDecimal.valueOf(3));
		
		List<Object[]> list = jpaStorageCustomer.executeFetch(condition, null, 0, 10);
		
		// is 2 because of null
		Assert.assertEquals(2, list.size());
		
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
	
	/**
	 * Tests the
	 * {@link JPAStorage#writeCSV(java.io.OutputStream, String[], String[], ICondition, javax.rad.model.SortDefinition, String)}
	 * method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testWriteCSV() throws Exception
	{
		// First get the column names to make sure that they are always in
		// the correct order.
		String[] columnNames = jpaStorageCustomer.getMetaData().getColumnNames();
		columnNames = Arrays.copyOf(columnNames, columnNames.length);
		Arrays.sort(columnNames);
		
		ByteOutputStream stream = new ByteOutputStream();
		
		jpaStorageCustomer.writeCSV(stream, columnNames, null, null, null, ",");
		
		String expected = new String(FileUtil.getContent(ResourceUtil.getResourceAsStream("/com/sibvisions/rad/persist/jpa/resource/customers.csv")));
		String actual = new String(stream.getBytes(), 0, stream.size());
		System.out.println(actual);
		Assert.assertEquals(expected, actual);
	}
	
}	// TestJPAStorage
