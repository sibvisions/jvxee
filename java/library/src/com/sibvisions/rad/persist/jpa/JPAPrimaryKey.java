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

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link JPAPrimaryKey} is a {@link JPAEmbeddedKey} extension and
 * encapsulates additional information and methods and the
 * {@link JPAServerColumnMetaData} for an entity.
 * <p>
 * For example: An entity can have three different types of primary keys:
 * <p>
 * 1. A singled id attribute:
 * 
 * <pre>
 * <code>
 *    
 * {@literal @}Id
 * private int id;
 * </code>
 * </pre>
 * 
 * 2. Many id attributes in an IdClass
 * 
 * <pre>
 * <code>
 * public class CustomerPK implements Serializable
 * {
 *      {@literal @}Id
 *      private int id;
 *  
 *      {@literal @}Id
 *      private int socialInsuranceNumber;
 *  
 *      ....
 * } 
 * 
 * {@literal @}Entity
 * {@literal @}IdClass(CustomerPK.class)
 * public class Customer implements Serializable
 * { 
 *      {@literal @}Id
 *      private int id;
 *  
 *      {@literal @}Id
 *      private int socialInsuranceNumber;
 *  
 *      private String name;
 *  
 *      ....
 * }
 * </code>
 * </pre>
 * 
 * 3. An Embedded Primary Class
 * 
 * <pre>
 * <code>
 * 	
 * {@literal @}Embeddable
 * public class CustomerPK implements Serializable
 * { 
 *      {@literal @}Id
 *      private int id;
 *  
 *      {@literal @}Id
 *      private int socialInsuranceNumber;
 *  
 *      ....
 * } 
 * 
 * {@literal @}Entity
 * public class Customer implements Serializable 
 * {
 *      {@literal @}EmbeddedId
 *      private CustomerPK customerPK;
 *  
 *      private String name;
 *  
 *      ....
 * }
 * </code>
 * </pre>
 * <p>
 * And so the primary key from an entity is similar to an embedded Object in an
 * entity.
 * 
 * @author Stefan Wurm
 */
public class JPAPrimaryKey extends JPAEmbeddedKey
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** Defines if the primary key from the entity is an embedded class. **/
	private boolean embedded = false;
	
	/** Is needed because of a many to many relation where no entity exists. **/
	private Map<Class, JPAForeignKey> mapForeignKey = new HashMap<Class, JPAForeignKey>();
	
	/** Defines if the Id from the entity is a single id. **/
	private boolean singleIdAttribute = true;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Creates a new instance of {@link JPAPrimaryKey}.
	 */
	public JPAPrimaryKey()
	{
		super();
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
		return "JPAPrimaryKey [mapForeignKey=" + mapForeignKey
				+ ", isEmbedded=" + embedded + ", singleIdAttribute="
				+ singleIdAttribute + ", toString()=" + super.toString() + "]";
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Adds a {@link JPAForeignKey} to the {@link JPAPrimaryKey}. Is needed
	 * because of a Many To Many relation where no entity exists. For example:
	 * <p>
	 * If there is a ManyToMany relation between Order and Article it is not
	 * necessary to create an entity "OrderArticle".
	 * <p>
	 * And so the PrimaryKey for this relation is a combined ForeignKey from
	 * Order and Article
	 * 
	 * @param pEntity the class of the entity.
	 * @param pForeignKey the {@link JPAForeignKey}.
	 */
	public void addForeignKey(Class pEntity, JPAForeignKey pForeignKey)
	{
		mapForeignKey.put(pEntity, pForeignKey);
	}

	/**
	 * Gets the {@link JPAForeignKey} for the given class.
	 * 
	 * @param pEntity the class of the entity
	 * @return the {@link JPAForeignKey} for the given class
	 */
	public JPAForeignKey getForeignKey(Class pEntity)
	{
		return mapForeignKey.get(pEntity);
	}

	/**
	 * Returns the class of the key from the entity.
	 * 
	 * @return the class of the key.
	 */
	public Class getKeyClass()
	{
		return getJPAMappingType().getJavaTypeClass();
	}

	/**
	 * Returns the primary key for the entity with the given values
	 * <p>
	 * The given {@link Map} with the values for the key has as key the name of
	 * the {@link JPAServerColumnMetaData} and as value the value for the
	 * primary key.
	 * 
	 * <pre>
	 * { ID = 3, SOCIALINSURANCENUMBER = 12345 }
	 * </pre>
	 * 
	 * @param pData the {@link Map} with the values for the key.
	 * @return the primary key from the entity in the entity.
	 * @throws Exception if getting the primary key failed.
	 */
	public Object getKeyForEntity(Map<String, Object> pData) throws Exception
	{
		Object key = null;
		
		if (isSingleIdAttribute())
		{
			if (JPAStorageUtil.isPrimitiveOrWrapped(getKeyClass()))
			{
				JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaDataAsArray()[0];
				
				key = serverColumnMetaData.getJPAMappingType().castObjectToJavaType(pData.get(serverColumnMetaData.getName()));
			}
			else
			{
				// Is EmbeddedId
				
				key = getKeyClass().newInstance();
				
				for (JPAServerColumnMetaData serverColumnMetaData : getServerColumnMetaDataAsArray())
				{
					Object setValue = pData.get(serverColumnMetaData.getName());
					
					serverColumnMetaData.getJPAMappingType().setValue(key, setValue);
				}
			}
		}
		else
		{
			key = (Object)getKeyClass().newInstance();
			
			for (JPAServerColumnMetaData serverColumnMetaData : getServerColumnMetaDataAsArray())
			{
				Object setValue = pData.get(serverColumnMetaData.getName());
				
				serverColumnMetaData.getJPAMappingType().setValue(key, setValue);
			}
		}
		
		return key;
	}
	
	/**
	 * Gets if the Id of the entity is a single ID.
	 * 
	 * @return {@code true} if the Id of the entity is a single ID.
	 */
	public boolean isSingleIdAttribute()
	{
		return singleIdAttribute;
	}
	
	/**
	 * Sets if the Id of the entity is a single ID.
	 * 
	 * @param pSingleIdAttribute {@code true} if the Id of the entity is a
	 *            single ID.
	 */
	public void setSingleIdAttribute(boolean pSingleIdAttribute)
	{
		singleIdAttribute = pSingleIdAttribute;
	}
	
	/**
	 * Gets if the primary key from the entity is an embedded class.
	 * 
	 * @return {@code true} if the primary key from the entity is an embedded
	 *         class.
	 */
	public boolean isEmbedded()
	{
		return embedded;
	}
	
	/**
	 * Sets if the primary key from the entity is an embedded class.
	 * 
	 * @param pEmbedded {@code true} if the primary key from the entity is an
	 *            embedded class.
	 */
	public void setEmbedded(boolean pEmbedded)
	{
		embedded = pEmbedded;
	}
	
}		// JPAPrimaryKey
