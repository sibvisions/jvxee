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
package com.sibvisions.rad.persist.jpa.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Embeddable
public class SalutationPK implements Serializable
{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	private String salutation;
	
	public SalutationPK()
	{
	
	}
	
	public long getId()
	{
		return id;
	}
	
	public void setId(long id)
	{
		this.id = id;
	}
	
	public String getSalutation()
	{
		return salutation;
	}
	
	public void setSalutation(String salutation)
	{
		this.salutation = salutation;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)(id ^ (id >>> 32));
		result = prime * result
				+ ((salutation == null) ? 0 : salutation.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SalutationPK other = (SalutationPK)obj;
		if (id != other.id)
			return false;
		if (salutation == null)
		{
			if (other.salutation != null)
				return false;
		}
		else if (!salutation.equals(other.salutation))
			return false;
		return true;
	}
	
}
