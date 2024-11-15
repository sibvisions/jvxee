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
package apps.jvxee.eao;

import javax.persistence.EntityManager;
import jvx.rad.persist.DataSourceException;

import apps.jvxee.entity.Address;

import com.sibvisions.rad.persist.jpa.EAOMethod;
import com.sibvisions.rad.persist.jpa.EAOMethod.EAO;

/**
 * The address EAO.
 * 
 * @author Stefan Wurm
 */
public class AddressEAO
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** the entity manager. */
	private EntityManager entityManager;

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Sets the entity manager.
	 * 
	 * @param pEntityManager the entity manager
	 */
	public void setEntityManager(EntityManager pEntityManager)
	{
		this.entityManager = pEntityManager;
	}

	/**
	 * Inserts an address.
	 * 
	 * @param pAddress the address
	 * @return the inserted address
	 * @throws DataSourceException if insert or address validation fails
	 */
	@EAOMethod(methodIdentifier = EAO.INSERT)
	public Address insertAddress(Address pAddress) throws DataSourceException
	{
		if (pAddress.getZip().length() <= 3)
		{
			throw new DataSourceException("ZIP have to be 4 characters long");
		}

		entityManager.getTransaction().begin();

		entityManager.persist(pAddress);

		entityManager.getTransaction().commit();

		return pAddress;
	}

	/**
	 * Updates an address.
	 * 
	 * @param pAddress the address
	 * @throws DataSourceException if update or address validation fails
	 */
	@EAOMethod(methodIdentifier = EAO.UPDATE)
	public void updateAddress(Address pAddress) throws DataSourceException
	{

		if (pAddress.getZip().length() <= 3)
		{
			throw new DataSourceException("ZIP have to be 4 characters long");
		}

		entityManager.getTransaction().begin();

		entityManager.merge(pAddress);

		entityManager.getTransaction().commit();
	}

}	// AddressEAO
