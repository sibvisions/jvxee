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
package com.sibvisions.rad.persist.jpa.eao;

import javax.persistence.EntityManager;

import com.sibvisions.rad.persist.jpa.EAOMethod;
import com.sibvisions.rad.persist.jpa.EAOMethod.EAO;
import com.sibvisions.rad.persist.jpa.entity.Customer;

/**
 * An Entity Access Object for the Customer Entity.
 * 
 * @author Stefan Wurm
 */
public class CustomerEAO
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** The Entity Manager. */
	private EntityManager entityManager;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Setter Method for the Entity Manager.
	 * 
	 * @param pEntityManager The Entity Manager
	 */
	public void setEntityManager(EntityManager pEntityManager)
	{
		entityManager = pEntityManager;
	}
	
	/**
	 * Deletes the given customer from the DB.
	 * 
	 * @param customer The Customer
	 */
	@EAOMethod(methodIdentifier = EAO.DELETE)
	public void deleteCustomer(Customer customer)
	{
		entityManager.getTransaction().begin();
		
		if (customer.getEducations().size() > 0)
		{
			customer.getEducations().clear();
		}
		
		if (customer.getAddresses().size() > 0)
		{
			customer.getEducations().clear();
		}
		
		entityManager.remove(customer);
		entityManager.getTransaction().commit();
	}
	
}	// CustomerEAO
