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
import javax.rad.persist.DataSourceException;

import com.sibvisions.rad.persist.jpa.EAOMethod;
import com.sibvisions.rad.persist.jpa.EAOMethod.EAO;
import com.sibvisions.rad.persist.jpa.entity.Address;

/**
 * An Entity Access Object for the Address Entity.
 * 
 * @author Stefan Wurm
 */
public class AddressEAO
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
	 * Saves the given address in the database.
	 * 
	 * @param address the address
	 * @return the address with the id
	 * @throws DataSourceException if the zip is not 4 characters long.
	 */
	@EAOMethod(methodIdentifier = EAO.INSERT)
	public Address insertAddress(Address address) throws DataSourceException
	{
		if (address.getZip().length() <= 3)
		{
			throw new DataSourceException("ZIP have to be 4 characters long");
		}
		
		entityManager.getTransaction().begin();
		entityManager.persist(address);
		entityManager.getTransaction().commit();
		
		return address;
	}
	
	/**
	 * Make an update to the given address.
	 * 
	 * @param address the address
	 * @throws DataSourceException if the zip is not 4 characters long.
	 */
	@EAOMethod(methodIdentifier = EAO.UPDATE)
	public void updateAddress(Address address) throws DataSourceException
	{
		if (address.getZip().length() <= 3)
		{
			throw new DataSourceException("ZIP have to be 4 characters long");
		}
		
		entityManager.getTransaction().begin();
		entityManager.merge(address);
		entityManager.getTransaction().commit();
	}
	
}	// AddressEAO
