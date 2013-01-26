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
package apps.jvxee.frames;

import javax.persistence.EntityManager;
import javax.rad.persist.IStorage;

import apps.jvxee.Session;
import apps.jvxee.eao.AddressEAO;
import apps.jvxee.eao.CustomerEAO;
import apps.jvxee.entity.Address;
import apps.jvxee.entity.Customer;
import apps.jvxee.entity.Education;

import com.sibvisions.rad.persist.jpa.JPAStorage;

/**
 * The LCO for the CustomerEdit WorkScreen.
 * <p/>
 * @author Stefan Wurm
 */
public class CustomerEdit extends Session 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Gets the customer storage.
	 * 
	 * @return the customers storage.
	 * @throws Exception if data access fails
	 */
	public IStorage getCustomer() throws Exception
	{
		JPAStorage jpaCustomer = (JPAStorage)get("customer");

		if (jpaCustomer == null) 
		{
			EntityManager em = getEntityManager();
			
			jpaCustomer = new JPAStorage(Customer.class);
			jpaCustomer.setEntityManager(em);
			
			CustomerEAO customerEAO = new CustomerEAO();
			customerEAO.setEntityManager(em);
			
			jpaCustomer.getJPAAccess().setExternalEAO(customerEAO);
		    jpaCustomer.open();
			
			put("customer", jpaCustomer);
		}
		
		return jpaCustomer;
	}
	
	/**
	 * Gets the address storage.
	 * 
	 * @return the address storage
	 * @throws Exception if data access fails
	 */
	public IStorage getAddress() throws Exception
	{
		JPAStorage jpaAddress = (JPAStorage)get("address");
		
		if (jpaAddress == null) 
		{
			EntityManager em = getEntityManager();
			
			jpaAddress = new JPAStorage(Address.class);
			jpaAddress.setEntityManager(em);
			
			AddressEAO addressEAO = new AddressEAO();
			addressEAO.setEntityManager(em);
			
			jpaAddress.getJPAAccess().setExternalEAO(addressEAO);
		    jpaAddress.open();
			
			put("address", jpaAddress);
		}
		
		return jpaAddress;
	}
	
	/**
	 * Gets the customer education storage.
	 * 
	 * @return the customer education storage
	 * @throws Exception if data access fails
	 */
	public IStorage getCustomerEducation() throws Exception
	{
		JPAStorage jpaCustomerEducation = (JPAStorage)get("customerEducation");
		
		if (jpaCustomerEducation == null) 
		{
			jpaCustomerEducation = new JPAStorage(Customer.class);
			jpaCustomerEducation.setDetailEntity(Education.class);
			jpaCustomerEducation.setEntityManager(getEntityManager());
		    jpaCustomerEducation.open();
			
			put("customerEducation", jpaCustomerEducation);
		}
		
		return jpaCustomerEducation;
	}	
	
}	// CustomerEdit
