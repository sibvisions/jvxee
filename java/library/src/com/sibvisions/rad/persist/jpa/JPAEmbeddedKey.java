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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * The <code>JPAEmbeddedKey</code> encapsulates the <code>JPAServerColumnMetaData</code> for an embedded class of an entity.
 * 
 * For example: An Entity "Customer" has an Embedded Object "Address"
 * 
 * @Entity
 * public class Customer implements Serializable
 * {
 * 	   @Id
 *     private int id;
 *  
 *     private String name;
 *  
 *     @Embedded
 *     private Address address;
 *  
 *     ....... 
 * } 
 * 
 * @Embeddable
 * public class Address
 * {
 *     private String street;
 *     private String nr;
 *   
 *     .....
 * }
 * 
 * It encapsulates the <code>JPAServerColumnMetaData</code> for the columns of the embedded class.
 * It also stores the JPAMappingType for the embedded class.
 * 
 * @see com.sibvisions.rad.persist.jpa.JPAServerColumnMetaData
 * 
 * @author Stefan Wurm
 */
public class JPAEmbeddedKey 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** The JPAMappingType from the Embedded Object. **/
	private JPAMappingType jpaMappingType; 

	/** The <code>JPAServerColumnMetaData</code> for the columns from the embedded object. **/
	private Collection<JPAServerColumnMetaData> cServerColumnMetaData = new LinkedHashSet<JPAServerColumnMetaData>();
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Overwritten methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() 
	{
		return "JPAEmbeddedKey [jpaMappingType=" + jpaMappingType
				+ ", cServerColumnMetaData=" + cServerColumnMetaData;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Returns the <code>JPAMappingType</code>.
	 * 
	 * @return The JPAMappingType
	 */
	public JPAMappingType getJPAMappingType() 
	{
		return jpaMappingType;
	}

	/**
	 * Sets the <code>JPAServerColumnMetaData</code>.
	 * 
	 * @param pJPAMappingType
	 */
	public void setJPAMappingType(JPAMappingType pJPAMappingType)
	{
		jpaMappingType = pJPAMappingType;
	}

	/**
	 * Add a <code>JPAServerColumnMetaData</code> to the <code>JPAEmbeddeKey</code>.
	 * 
	 * @param pServerColumnMetaData the <code>JPAServerColumnMetaData</code> to add.
	 */			
	public void addServerColumnMetaData(JPAServerColumnMetaData pServerColumnMetaData) 
	{
		cServerColumnMetaData.add(pServerColumnMetaData);
	}

	/**
	 * Returns all <code>JPAServerColumnMetaData</code> from the <code>JPAEmbeddeKey</code> as an Array.
	 * 
	 * @return All <code>JPAServerColumnMetaData</code> as Array
	 */
	public JPAServerColumnMetaData[] getServerColumnMetaDataAsArray() 
	{
		return cServerColumnMetaData.toArray(new JPAServerColumnMetaData[cServerColumnMetaData.size()]);
	}

	/**
	 * Returns all <code>JPAServerColumnMetaData</code> from the <code>JPAEmbeddeKey</code> in a Collection.
	 * 
	 * @return All <code>JPAServerColumnMetaData</code> in a Collection
	 */	
	public Collection<JPAServerColumnMetaData> getServerColumnMetaDataAsCollection() 
	{
		return cServerColumnMetaData;
	}
	
	/** 
	 * Returns the names from the columns in an array.
	 * 
	 * @return the names from the columns in an array. 
	 */
	public String [] getColumnNames() 
	{
		ArrayList<String> columnNames = new ArrayList<String>();

		for (JPAServerColumnMetaData jpaServerColumnMetaData : getServerColumnMetaDataAsCollection()) 
		{
			columnNames.add(jpaServerColumnMetaData.getName());	
		}
		
		return columnNames.toArray(new String[getSize()]);
	}	

	/**
	 * Returns the Number of encapsulated <code>JPAServerColumnMetaData</code>.
	 * 
	 * @return The Number of <code>JPAServerColumnMetaData</code>
	 */
	public int getSize() 
	{
		return cServerColumnMetaData.size();
	}
	
}	// JPAEmbeddedKey
