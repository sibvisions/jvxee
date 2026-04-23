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

import apps.jvxee.entity.Customer;

import com.sibvisions.rad.persist.jpa.EAOMethod;
import com.sibvisions.rad.persist.jpa.EAOMethod.EAO;

/**
 * The customer EAO.
 * 
 * @author Stefan Wurm
 */
public class CustomerEAO
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
	 * Deletes a customer.
	 * 
	 * @param pCustomer the customer
	 */
	@EAOMethod(methodIdentifier = EAO.DELETE)
	public void deleteCustomer(Customer pCustomer) 
	{
        entityManager.getTransaction().begin();
    	
        if (pCustomer.getEducations().size() > 0) 
        {
        	pCustomer.getEducations().clear();
        }
        
        if (pCustomer.getAddresses().size() > 0) 
        {
        	pCustomer.getEducations().clear();
        }
        
        entityManager.remove(pCustomer);
                
        entityManager.getTransaction().commit();
	}
	
}	// CustomerEAO
