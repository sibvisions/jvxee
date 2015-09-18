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
import java.util.Set;

/**
 * The {@link JPAEmbeddedKey} encapsulates the {@link JPAServerColumnMetaData}
 * for an embedded class of an entity.
 * <p>
 * For example: An Entity "Customer" has an Embedded Object "Address"
 * 
 * <pre>
 * <code>
 * {@literal @}Entity
 * public class Customer implements Serializable
 * {
 *     {@literal @}Id
 *     private int id;
 *  
 *     private String name;
 *  
 *     {@literal @}Embedded
 *     private Address address;
 *  
 *     ....... 
 * } 
 * 
 * {@literal @}Embeddable
 * public class Address
 * {
 *     private String street;
 *     private String nr;
 *   
 *     .....
 * }
 * </code>
 * </pre>
 * 
 * It encapsulates the {@link JPAServerColumnMetaData} for the columns of the
 * embedded class. It also stores the JPAMappingType for the embedded class.
 * 
 * @author Stefan Wurm
 */
public class JPAEmbeddedKey
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** The {@link JPAMappingType} from the Embedded Object. **/
	private JPAMappingType jpaMappingType;
	
	/**
	 * The {@link Set} that holds the {@link JPAServerColumnMetaData} for the
	 * columns from the embedded object.
	 **/
	private Set<JPAServerColumnMetaData> serverColumnMetaData = new LinkedHashSet<JPAServerColumnMetaData>();
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Creates a new instance of {@link JPAEmbeddedKey}.
	 */
	public JPAEmbeddedKey()
	{
	}
	
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
				+ ", serverColumnMetaData=" + serverColumnMetaData;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Add a {@link JPAServerColumnMetaData} to the {@link JPAEmbeddedKey}.
	 * 
	 * @param pServerColumnMetaData the {@link JPAServerColumnMetaData} to add.
	 */
	public void addServerColumnMetaData(JPAServerColumnMetaData pServerColumnMetaData)
	{
		serverColumnMetaData.add(pServerColumnMetaData);
	}
	
	/**
	 * Returns the names from the columns in an array.
	 * 
	 * @return the names from the columns in an array.
	 */
	public String[] getColumnNames()
	{
		ArrayList<String> columnNames = new ArrayList<String>();
		
		for (JPAServerColumnMetaData jpaServerColumnMetaData : getServerColumnMetaDataAsCollection())
		{
			columnNames.add(jpaServerColumnMetaData.getName());
		}
		
		return columnNames.toArray(new String[getSize()]);
	}
	
	/**
	 * Gets the Number of encapsulated {@link JPAServerColumnMetaData}.
	 * 
	 * @return the Number of encapsulated {@link JPAServerColumnMetaData}.
	 */
	public int getSize()
	{
		return serverColumnMetaData.size();
	}
	
	/**
	 * Gets all {@link JPAServerColumnMetaData} from the {@link JPAEmbeddedKey}
	 * as an Array.
	 * 
	 * @return all {@link JPAServerColumnMetaData} as array.
	 */
	public JPAServerColumnMetaData[] getServerColumnMetaDataAsArray()
	{
		return serverColumnMetaData.toArray(new JPAServerColumnMetaData[serverColumnMetaData.size()]);
	}
	
	/**
	 * Gets all {@link JPAServerColumnMetaData} from the {@link JPAEmbeddedKey}
	 * in a Collection.
	 * 
	 * @return all {@link JPAServerColumnMetaData} as a collection.
	 */
	public Collection<JPAServerColumnMetaData> getServerColumnMetaDataAsCollection()
	{
		return serverColumnMetaData;
	}
	
	/**
	 * Gets the {@link JPAMappingType}.
	 * 
	 * @return the {@link JPAMappingType}.
	 */
	public JPAMappingType getJPAMappingType()
	{
		return jpaMappingType;
	}
	
	/**
	 * Sets the {@link JPAServerColumnMetaData}.
	 * 
	 * @param pJPAMappingType the {@link JPAMappingType}.
	 */
	public void setJPAMappingType(JPAMappingType pJPAMappingType)
	{
		jpaMappingType = pJPAMappingType;
	}
	
}	// JPAEmbeddedKey
