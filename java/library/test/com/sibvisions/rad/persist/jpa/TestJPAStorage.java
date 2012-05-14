package com.sibvisions.rad.persist.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
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

public class TestJPAStorage {
	
	EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpatest"); 
	EntityManager entityManager = null;
	CriteriaBuilder criteriaBuilder;
	
	JPAStorage jpaStorageCustomer;
	JPAStorage jpaStorageAddress;
	JPAStorage jpaStorageEducation;
	
	
	@Before
	public void open() throws Exception {
		
	      entityManager = emf.createEntityManager();
	      criteriaBuilder = emf.getCriteriaBuilder();
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
	
	@After
	public void close() throws Exception {
		entityManager.close();
	}	
		
	public void initializeDB() {
		
	      entityManager.getTransaction().begin();
	      
	      entityManager.createNativeQuery("insert into salutation (ID, SALUTATION) values (1, 'Mister')").executeUpdate();	      
	      entityManager.createNativeQuery("insert into salutation (ID, SALUTATION) values (2, 'Mrs')").executeUpdate();

	      entityManager.createNativeQuery("insert into healthinsurance (ID, NR, HEALTH_INSURANCE, STREET, ZIP, CITY) values (1, 10, 'Healthinsurance 1', 'Street 1', 1111, 'City 1')").executeUpdate();	      
	      entityManager.createNativeQuery("insert into healthinsurance (ID, NR, HEALTH_INSURANCE, STREET, ZIP, CITY) values (2, 20, 'Healthinsurance 2', 'Street 2', 2222, 'City 2')").executeUpdate();     
	      entityManager.createNativeQuery("insert into healthinsurance (ID, NR, HEALTH_INSURANCE, STREET, ZIP, CITY) values (3, 30, 'Healthinsurance 3', 'Street 3', 3333, 'City 3')").executeUpdate();	      
	      
	      entityManager.createNativeQuery("insert into customer (ID, FIRSTNAME, LASTNAME, BIRTHDAY, PRIVATECUSTOMER, TELEPHONE_PRIVATE, TELEPHONE_OFFICE, EMAIL, SALUTATION_ID, SALUTATION_SALUTATION, HEALTHINSURANCE_ID, HEALTHINSURANCE_NR) values (1, 'Firstname 1', 'Lastname 1', null, true, '1111111111', '1111111111', 'first1.last1@gmx.at', 1, 'Mister',  1, 10)").executeUpdate();	      
	      entityManager.createNativeQuery("insert into customer (ID, FIRSTNAME, LASTNAME, BIRTHDAY, PRIVATECUSTOMER, TELEPHONE_PRIVATE, TELEPHONE_OFFICE, EMAIL, SALUTATION_ID, SALUTATION_SALUTATION, HEALTHINSURANCE_ID, HEALTHINSURANCE_NR) values (2, 'Firstname 2', 'Lastname 2', null, true, '2222222222', '2222222222', 'first2.last2@gmx.at', 1, 'Mister',  1, 10)").executeUpdate();	
	      entityManager.createNativeQuery("insert into customer (ID, FIRSTNAME, LASTNAME, BIRTHDAY, PRIVATECUSTOMER, TELEPHONE_PRIVATE, TELEPHONE_OFFICE, EMAIL, SALUTATION_ID, SALUTATION_SALUTATION, HEALTHINSURANCE_ID, HEALTHINSURANCE_NR) values (3, 'Firstname 3', 'Lastname 3', null, false, '3333333333', '3333333333', 'first3.last3@gmx.at', 2, 'Mrs',  3, 30)").executeUpdate();	
	      entityManager.createNativeQuery("insert into customer (ID, FIRSTNAME, LASTNAME, BIRTHDAY, PRIVATECUSTOMER, TELEPHONE_PRIVATE, TELEPHONE_OFFICE, EMAIL, SALUTATION_ID, SALUTATION_SALUTATION, HEALTHINSURANCE_ID, HEALTHINSURANCE_NR) values (4, 'Firstname 4', 'Lastname 4', null, false, '4444444444', '4444444444', 'first4.last4@gmx.at', 2, 'Mrs',  3, 30)").executeUpdate();	
	         
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
	
	public void clearDB() {
	      entityManager.getTransaction().begin();
	      
	      entityManager.createQuery("delete from Address").executeUpdate();
	      entityManager.createQuery("delete from Customer").executeUpdate();
	      entityManager.createQuery("delete from Salutation").executeUpdate();
	      entityManager.createQuery("delete from Healthinsurance").executeUpdate();
	      entityManager.createQuery("delete from Education").executeUpdate();

	      entityManager.flush();
	      
	      entityManager.getTransaction().commit();		
	}	
	
	/****************************** Test MetaData *************************************************/
		
	@Test
	public void testMetaDataEducation() throws Throwable {
		
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
				
		Assert.assertArrayEquals(new String[] {"CUSTOMER_ID", "CUSTOMER_LASTNAME", "EDUCATION_ID", "EDUCATION_EDUCATION"}, rdb.getRowDefinition().getColumnNames());
	}	
	
	@Test
	public void testMetaDataAddress() throws Throwable {
		
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
		
		Assert.assertArrayEquals(new String[] {"ID", "ZIP", "STREET", "CUSTOMER_ID", "CUSTOMER_LASTNAME", "CITY"}, rdb.getRowDefinition().getColumnNames());
	}
	
	/****************************** Test Fetch *************************************************/
	
	@Test
	public void testFetch() throws Exception {
		
		List<Object []> objects = jpaStorageCustomer.executeFetch(null, null, 0, 10);
		
		Assert.assertTrue(objects.size() == 5);
		
	}
	
	/****************************** Test Insert *************************************************/
	
	@Test
	public void testInsertCustomer() throws Exception {
	
		/* ID, BIRTHDAY, EMAIL, TELEPHONE_PRIVATE, TELEPHONE_OFFICE, LASTNAME, 
		 *  HEALTHINSURANCE_ID, HEALTHINSURANCE_NR, HEALTHINSURANCE_HEALTH_INSURANCE,
		 *  FIRSTNAME, SALUTATION_ID, SALUTATION, PRIVATECUSTOMER
		 */
		Object [] dataRow = {null, null, "New Mail", "12345", "12345", "New Lastname", null, null, null, "New Firstname", 1, "Mister", true};
		
		dataRow = jpaStorageCustomer.executeInsert(dataRow);
		
		Assert.assertTrue(dataRow[0] != null);

        entityManager.getTransaction().begin();
        
        Customer entity = entityManager.find(Customer.class, dataRow[0]);

        Assert.assertTrue(entity != null);
        Assert.assertTrue(entity.getId() == 5);
        Assert.assertTrue(entity.getBirthday() == null);
        Assert.assertTrue(dataRow[2].equals(entity.getCustomerContact().getEmail()));
        Assert.assertTrue(dataRow[3].equals(entity.getCustomerContact().getTelephone_private()));
        Assert.assertTrue(dataRow[4].equals(entity.getCustomerContact().getTelephone_office()));
        Assert.assertTrue(dataRow[5].equals(entity.getLastname()));
        Assert.assertTrue(dataRow[9].equals(entity.getFirstname()));
        Assert.assertTrue(dataRow[10].equals(new Integer(entity.getSalutation().getSalutationPK().getId())));
        Assert.assertTrue(dataRow[11].equals(entity.getSalutation().getSalutationPK().getSalutation()));
        Assert.assertTrue(entity.isPrivateCustomer());
        
        entityManager.getTransaction().commit();		
		
	}
	
	@Test
	public void testInsertAddress() throws Exception {
	
		/* "ID", "ZIP", "STREET", "CUSTOMER_ID", "CUSTOMER_LASTNAME", "CITY" **/
		Object [] dataRow = {null, 1234, "New street", "2", "Lastname 2", "New city"};
		
		dataRow = jpaStorageAddress.executeInsert(dataRow);
		
		Assert.assertTrue(dataRow[0] != null);

        entityManager.getTransaction().begin();
        
        Address entity = entityManager.find(Address.class, dataRow[0]);

        Assert.assertTrue(entity != null);
        Assert.assertTrue(entity.getId() == 3);
        Assert.assertTrue(dataRow[1].equals(entity.getZip()));
        Assert.assertTrue(dataRow[2].equals(entity.getStreet()));
        Assert.assertTrue(dataRow[3].equals(entity.getCustomer().getId()));
        Assert.assertTrue(dataRow[4].equals(entity.getCustomer().getLastname()));
        Assert.assertTrue(dataRow[5].equals(entity.getCity()));
        
        entityManager.getTransaction().commit();		
        
        entityManager.getTransaction().begin();
        
        Customer entityCust = entityManager.find(Customer.class, 2);

        Assert.assertTrue(entityCust != null);
        Assert.assertTrue(entityCust.getAddresses().size() == 1);   
        
        entityManager.getTransaction().commit();	        
		
	}	
	
	@Test
	public void testInsertEducation() throws Exception {
	
		/* "CUSTOMER_ID", "CUSTOMER_LASTNAME", "EDUCATION_ID", "EDUCATION_EDUCATION" */
		Object [] dataRow = {3, "Lastname 3", 2, "Education 2"};
		
		dataRow = jpaStorageEducation.executeInsert(dataRow);
		
		Assert.assertTrue(dataRow[0] != null);

        entityManager.getTransaction().begin();
        
        Customer entity = entityManager.find(Customer.class, dataRow[0]);

        Assert.assertTrue(entity != null);
        Assert.assertTrue(entity.getId() == 3);
        Assert.assertTrue(entity.getEducations().size() == 1);
        
        entityManager.getTransaction().commit();		
	}		
	
	/****************************** Test Update *************************************************/
	
	@Test
	public void testUpdateCustomer() throws Exception {
	
		ICondition condition = new Equals("ID", 3);
		
		List<Object []> list = jpaStorageCustomer.executeFetch(condition, null, 0, 10);
		
		Assert.assertTrue(list.size() == 2); // is 2 because of null
		
		Object [] dataRowOld = list.get(0);
		
		Object [] dataRowNew = dataRowOld;

		/* ID, BIRTHDAY, EMAIL, TELEPHONE_PRIVATE, TELEPHONE_OFFICE, LASTNAME, 
		 *  HEALTHINSURANCE_ID, HEALTHINSURANCE_NR, HEALTHINSURANCE_HEALTH_INSURANCE,
		 *  FIRSTNAME, SALUTATION_ID, SALUTATION, PRIVATECUSTOMER
		 */		
		dataRowNew[2] = "updatedMail";
		dataRowNew[10] = new BigDecimal(1);
		dataRowNew[11] = "Mister";
		
		dataRowNew = jpaStorageCustomer.executeUpdate(dataRowOld, dataRowNew);
		
		Assert.assertTrue(dataRowNew != null);

        entityManager.getTransaction().begin();
        
        Customer entity = entityManager.find(Customer.class, 3);

        Assert.assertTrue(entity != null);
        Assert.assertTrue(entity.getId() == 3);
        Assert.assertTrue(entity.getBirthday() == null);
        Assert.assertTrue(dataRowNew[2].equals(entity.getCustomerContact().getEmail()));
        Assert.assertTrue(dataRowNew[3].equals(entity.getCustomerContact().getTelephone_private()));
        Assert.assertTrue(dataRowNew[4].equals(entity.getCustomerContact().getTelephone_office()));
        Assert.assertTrue(dataRowNew[5].equals(entity.getLastname()));
        Assert.assertTrue(dataRowNew[9].equals(entity.getFirstname()));
        Assert.assertTrue(dataRowNew[10].equals(new Integer(entity.getSalutation().getSalutationPK().getId())));
        Assert.assertTrue(dataRowNew[11].equals(entity.getSalutation().getSalutationPK().getSalutation()));
        Assert.assertTrue(!entity.isPrivateCustomer());
        
        entityManager.getTransaction().commit();		
		
	}	
	
	@Test
	public void testUpdateAddress() throws Exception {
	
		ICondition condition = new Equals("ID", 2);
		
		List<Object []> list = jpaStorageAddress.executeFetch(condition, null, 0, 10);
		
		Assert.assertTrue(list.size() == 2); // is 2 because of null
		
		Object [] dataRowOld = list.get(0);
		
		Object [] dataRowNew = dataRowOld;

		/* "ID", "ZIP", "STREET", "CUSTOMER_ID", "CUSTOMER_LASTNAME", "CITY" **/
		dataRowNew[1] = 8888;
		dataRowNew[2] = "Updated Street";
		dataRowNew[5] = "Updated City";
		
		dataRowNew = jpaStorageAddress.executeUpdate(dataRowOld, dataRowNew);
		
		Assert.assertTrue(dataRowNew != null);

        entityManager.getTransaction().begin();
        
        Address entity = entityManager.find(Address.class, 2);

        Assert.assertTrue(entity != null);
        Assert.assertTrue(entity.getId() == 2);
        Assert.assertTrue(dataRowNew[1].equals(entity.getZip()));
        Assert.assertTrue(dataRowNew[2].equals(entity.getStreet()));
        Assert.assertTrue(dataRowNew[3].equals(entity.getCustomer().getId()));
        Assert.assertTrue(dataRowNew[4].equals(entity.getCustomer().getLastname()));
        Assert.assertTrue(dataRowNew[5].equals(entity.getCity()));
        
        entityManager.getTransaction().commit();		      
		
	}		
	
	@Test
	public void testUpdateEducation() throws Exception {

		/* "CUSTOMER_ID", "CUSTOMER_LASTNAME", "EDUCATION_ID", "EDUCATION_EDUCATION" */
		Object [] dataRowOld = {1, "Lastname 1", 2, "Education 2"};
		
		/* "CUSTOMER_ID", "CUSTOMER_LASTNAME", "EDUCATION_ID", "EDUCATION_EDUCATION" */
		Object [] dataRowNew = {1, "Lastname 1", 3, "Education 3"};

		dataRowNew = jpaStorageEducation.executeUpdate(dataRowOld, dataRowNew);		
		
		Assert.assertTrue(dataRowNew != null);

        entityManager.getTransaction().begin();
        
        Customer entity = entityManager.find(Customer.class, 1);

        Assert.assertTrue(entity != null);
        Assert.assertTrue(entity.getId() == 1);
        Assert.assertTrue(entity.getEducations().size() == 2);
        
        Education education = new ArrayList<Education>(entity.getEducations()).get(1); 
        
        Assert.assertTrue(education.getId() == 3);
        Assert.assertTrue(education.getEducation().equals("Education 3"));
        
        entityManager.getTransaction().commit();		
	}		
	
	/****************************** Test Delete *************************************************/
	
	@Test
	public void testDeleteCustomer() throws Exception {
	
		ICondition condition = new Equals("ID", 3);
		
		List<Object []> list = jpaStorageCustomer.executeFetch(condition, null, 0, 10);
		
		Assert.assertTrue(list.size() == 2); // is 2 because of null
		
		Object [] dataRowDelete = list.get(0);
		
		jpaStorageCustomer.executeDelete(dataRowDelete);
	
        entityManager.getTransaction().begin();
        
        Customer entity = entityManager.find(Customer.class, 3);
        
        Assert.assertNull(entity);

        entityManager.getTransaction().commit();		
		
	}
	
	@Test
	public void testDeleteAddress() throws Exception {
	
		ICondition condition = new Equals("ID", 2);
		
		List<Object []> list = jpaStorageAddress.executeFetch(condition, null, 0, 10);
		
		Assert.assertTrue(list.size() == 2); // is 2 because of null
		
		Object [] dataRowDelete = list.get(0);

		jpaStorageAddress.executeDelete(dataRowDelete);

        entityManager.getTransaction().begin();
        
        Address entity = entityManager.find(Address.class, 2);

        Assert.assertNull(entity);
        
        entityManager.getTransaction().commit();		      
	}
	
	@Test
	public void testDeleteEducation() throws Exception {

		/* "CUSTOMER_ID", "CUSTOMER_LASTNAME", "EDUCATION_ID", "EDUCATION_EDUCATION" */
		Object [] dataRowDelete = {1, "Lastname 1", 2, "Education 2"};
		
		jpaStorageEducation.executeDelete(dataRowDelete);	
	
        entityManager.getTransaction().begin();
        
        Customer entity = entityManager.find(Customer.class, 1);

        Assert.assertTrue(entity != null);
        Assert.assertTrue(entity.getId() == 1);
        Assert.assertTrue(entity.getEducations().size() == 1);

        entityManager.getTransaction().commit();		
	}		
	
	/****************************** Test Refetch *************************************************/	
	
	@Test
	public void testRefetchCustomer() throws Exception {
	
		ICondition condition = new Equals("ID", 3);
		
		List<Object []> list = jpaStorageCustomer.executeFetch(condition, null, 0, 10);
		
		Assert.assertTrue(list.size() == 2); // is 2 because of null
		
		Object [] dataRowRefetch = list.get(0);

		/* ID, BIRTHDAY, EMAIL, TELEPHONE_PRIVATE, TELEPHONE_OFFICE, LASTNAME, 
		 *  HEALTHINSURANCE_ID, HEALTHINSURANCE_NR, HEALTHINSURANCE_HEALTH_INSURANCE,
		 *  FIRSTNAME, SALUTATION_ID, SALUTATION, PRIVATECUSTOMER
		 */
		dataRowRefetch[2] = "updatedMail";
		dataRowRefetch[10] = new BigDecimal(1);
		dataRowRefetch[11] = "Mister";
		
		Object [] dataRowRefetch2 = jpaStorageCustomer.executeRefetchRow(dataRowRefetch);
	
		Assert.assertTrue(dataRowRefetch2[2].equals("first3.last3@gmx.at"));
		Assert.assertTrue(dataRowRefetch2[10].equals(new Integer(2)));
		Assert.assertTrue(dataRowRefetch2[11].equals("Mrs"));
	}		
	
}
