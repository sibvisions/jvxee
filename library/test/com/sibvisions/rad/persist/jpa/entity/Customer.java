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

import static javax.persistence.CascadeType.ALL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class Customer implements Serializable
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	
	@JoinColumns({ @JoinColumn(name = "SALUTATION_ID", referencedColumnName = "ID"), @JoinColumn(name = "SALUTATION_SALUTATION", referencedColumnName = "SALUTATION"), })
	@ManyToOne
	private Salutation salutation;
	
	@JoinColumns({ @JoinColumn(name = "HEALTHINSURANCE_ID", referencedColumnName = "ID"), @JoinColumn(name = "HEALTHINSURANCE_NR", referencedColumnName = "NR"), })
	@ManyToOne
	private Healthinsurance healthinsurance;
	
	private String firstname;
	
	private String lastname;
	
	@Temporal(TemporalType.DATE)
	private Date birthday;
	
	private boolean privateCustomer = true;
	
	@Embedded
	private CustomerContact customerContact;
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Collection<Education> educations = new ArrayList<Education>();
	
	@OneToMany(cascade = ALL, fetch = FetchType.LAZY, mappedBy = "customer")
	private Collection<Address> addresses = new ArrayList<Address>();
	
	public Customer()
	{
	
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public long getId()
	{
		return id;
	}
	
	public void setId(long id)
	{
		this.id = id;
	}
	
	public Salutation getSalutation()
	{
		return salutation;
	}
	
	public void setSalutation(Salutation salutation)
	{
		this.salutation = salutation;
	}
	
	public Healthinsurance getHealthinsurance()
	{
		return healthinsurance;
	}
	
	public void setHealthinsurance(Healthinsurance healthinsurance)
	{
		this.healthinsurance = healthinsurance;
	}
	
	public CustomerContact getCustomerContact()
	{
		return customerContact;
	}
	
	public void setCustomerContact(CustomerContact customerContact)
	{
		this.customerContact = customerContact;
	}
	
	public String getFirstname()
	{
		return firstname;
	}
	
	public void setFirstname(String firstname)
	{
		this.firstname = firstname;
	}
	
	public String getLastname()
	{
		return lastname;
	}
	
	public void setLastname(String lastname)
	{
		this.lastname = lastname;
	}
	
	public Date getBirthday()
	{
		return birthday;
	}
	
	public void setBirthday(Date birthday)
	{
		this.birthday = birthday;
	}
	
	public boolean isPrivateCustomer()
	{
		return privateCustomer;
	}
	
	public void setPrivateCustomer(boolean privateCustomer)
	{
		this.privateCustomer = privateCustomer;
	}
	
	public Collection<Education> getEducations()
	{
		return educations;
	}
	
	public void setEducations(Collection<Education> educations)
	{
		this.educations = educations;
	}
	
	public Collection<Address> getAddresses()
	{
		return addresses;
	}
	
	public void setAddresses(Collection<Address> addresses)
	{
		this.addresses = addresses;
	}
	
}	// Customer
