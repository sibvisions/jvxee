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
 * 17.09.2015 - [RZ] - creation
 */
package com.sibvisions.rad.persist.jpa.entity.flight;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Aircraft
{
	private String country;
	
	private String description;
	
	@Id
	@OneToMany
	private String registrationNumber;
	
	public Aircraft()
	{
		super();
	}
	
	public String getCountry()
	{
		return country;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getRegistrationNumber()
	{
		return registrationNumber;
	}
	
	public void setCountry(String pCountry)
	{
		country = pCountry;
	}
	
	public void setDescription(String pDescription)
	{
		description = pDescription;
	}
	
	public void setRegistrationNumber(String pRegistrationNumber)
	{
		registrationNumber = pRegistrationNumber;
	}
	
}
