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

import java.util.Map;

import javax.rad.model.condition.Equals;
import javax.rad.model.condition.ICondition;

/**
 * The {@link JPAForeignKey} is a {@link JPAEmbeddedKey} extension that
 * encapsulates additional information and methods. A {@link JPAForeignKey}
 * encapsulates the {@link JPAServerColumnMetaData} for an entity in an entity.
 * There exists an entity in an entity when there is a OneToOne or OneToMany
 * relationship between these entities.
 * <p>
 * And so an entity in an entity is similar to an embedded Object in an entity.
 * <p>
 * For example: An Entity "Customer" has a ManyToOne Relationship to Salutations
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
 *     {@literal @}ManyToOne
 *     private Salutations salutations;
 *  
 *     ...
 * } 
 * 
 * {@literal @}Entity
 * public class Salutations implements Serializable
 * {
 *     {@literal @}Id
 *     private int id;
 *  
 *     private String salutation;
 * }
 * </code>
 * </pre>
 * 
 * @author Stefan Wurm
 */
public class JPAForeignKey extends JPAEmbeddedKey
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** Defines if the Id from the entity in the entity is a single id. **/
	private boolean singleIdAttribute = true;
	
	/** The class of the key from the entity in the entity. **/
	private Class keyClass;
	
	/** The column names. **/
	private String[] referencedColumnNames;
	
	/** Getter Method Name for Detail Entities. */
	private String detailEntitiesMethode;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Creates a new instance of {@link JPAForeignKey}.
	 */
	public JPAForeignKey()
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
		return "JPAForeignKey [singleIdAttribute=" + singleIdAttribute
				+ ", keyClass=" + keyClass + ", toString()=" + super.toString()
				+ "]";
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Gets if the Id from the entity in the entity is a single id.
	 * 
	 * @return {@code true} if the Id from the entity in the entity is a single
	 *         id.
	 */
	public boolean isSingleIdAttribute()
	{
		return singleIdAttribute;
	}
	
	/**
	 * Sets if the Id from the entity in the entity is a single id.
	 * 
	 * @param pSingleIdAttribute {@code true} if the Id from the entity in the
	 *            entity is a single id.
	 */
	public void setSingleIdAttribute(boolean pSingleIdAttribute)
	{
		singleIdAttribute = pSingleIdAttribute;
	}
	
	/**
	 * Sets the class of the key from the entity in the entity.
	 * 
	 * @param pKeyClass the Class of the key.
	 */
	public void setKeyClass(Class pKeyClass)
	{
		keyClass = pKeyClass;
	}
	
	/**
	 * Gets the class of the key from the entity.
	 * 
	 * @return Class the Class of the key.
	 */
	public Class getKeyClass()
	{
		return keyClass;
	}
	
	/**
	 * Gets the referenced column names.
	 * 
	 * @return the referenced column names.
	 */
	public String[] getReferencedColumnNames()
	{
		return referencedColumnNames;
	}
	
	/**
	 * Set the referenced column names.
	 * 
	 * @param pReferencedColumnNames the referenced column names.
	 */
	public void setReferencedColumnNames(String[] pReferencedColumnNames)
	{
		referencedColumnNames = pReferencedColumnNames;
	}
	
	/**
	 * Set the name of the detail entities method.
	 * 
	 * @param pDetailEntitiesMethod the name of the detail entities method.
	 */
	public void setDetailEntitiesMethode(String pDetailEntitiesMethod)
	{
		detailEntitiesMethode = pDetailEntitiesMethod;
	}
	
	/**
	 * Gets the name of the detail entities method.
	 * 
	 * @return the name of the detail entities method.
	 */
	public String getDetailEntitiesMethode()
	{
		return detailEntitiesMethode;
	}
	
	/**
	 * Gets if the detail entities method is set.
	 * 
	 * @return {@code true} if the detail entities method is set.
	 */
	public boolean hasDetailEntitiesMethode()
	{
		if (detailEntitiesMethode != null && detailEntitiesMethode.length() > 0)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * Calls the detail entities method of the given entity object.
	 * 
	 * @param pEntity the entity object.
	 * @return the detail entity object.
	 */
	public Object getDetailEntities(Object pEntity)
	{
		try
		{
			return pEntity.getClass().getMethod(detailEntitiesMethode).invoke(pEntity);
		}
		catch (Exception e)
		{
			//nothing to be done
		}
		
		return null;
	}
	
	/**
	 * Gets the {@link ICondition} for this {@link JPAForeignKey}.
	 * 
	 * @return the {@link ICondition}.
	 */
	public ICondition getCondition()
	{
		ICondition condition = null;
		
		for (JPAServerColumnMetaData serverColumnMetaData : getServerColumnMetaDataAsCollection())
		{
			if (serverColumnMetaData.isKeyAttribute())
			{
				String name = serverColumnMetaData.getName();
				
				Equals compareCondition = new Equals(name, null, false);
				
				if (condition == null)
				{
					condition = compareCondition;
				}
				else
				{
					condition = condition.and(compareCondition);
				}
				
			}
		}
		
		return condition;
	}
	
	/**
	 * Gets the primary key for the entity in the entity with the given values.
	 * 
	 * <p>
	 * 
	 * For example: An entity can have three different types of primary keys:
	 * <p>
	 * 1. A singled id attribute:
	 * 
	 * <pre>
	 * <code>
	 * {@literal @}Id private int id;
	 * </code>
	 * </pre>
	 * 
	 * 2. Many id attributes in an IdClass
	 * 
	 * <pre>
	 * <code>
	 * public class CustomerPK implements Serializable {
	 * 	{@literal @}Id private int id;
	 * 	{@literal @}Id private int socialInsuranceNumber;
	 * 	....
	 * 
	 * }
	 * 
	 * {@literal @}Entity {@literal @}IdClass(CustomerPK.class)
	 * public class Customer implements Serializable {
	 * 	{@literal @}Id private int id;
	 * 	{@literal @}Id private int socialInsuranceNumber;
	 * 	
	 * 	private String name;
	 * 	
	 * 	....
	 * 	
	 * }
	 * </code>
	 * </pre>
	 * 
	 * 3. An Embedded Primary Class
	 * 
	 * <pre>
	 * <code>
	 * {@literal @}Embeddable
	 * public class CustomerPK implements Serializable {
	 * 	{@literal @}Id private int id;
	 * 	{@literal @}Id private int socialInsuranceNumber;
	 * 	
	 * 	....
	 * }
	 * 
	 * {@literal @}Entity public class Customer implements Serializable {
	 * 	{@literal @}EmbeddedId private CustomerPK customerPK;
	 * 	
	 * 	private String name;
	 * 	
	 * 	....
	 * }
	 * </code>
	 * </pre>
	 * 
	 * The {@link Map} with the values for the key has as key the name of the
	 * {@code JPAServerColumnMetaData} and as value the value for the primary
	 * key.
	 * 
	 * <pre>
	 * <code>
	 * { CUSTOMER_ID = 3, CUSTOMER_SOCIALINSURANCENUMBER = 12345 }
	 * </code>
	 * </pre>
	 * 
	 * @param pData the {@link Map} with the values for the key.
	 * @return the primary key from the entity in the entity.
	 * @throws Exception if getting the key failed.
	 */
	public Object getKeyForEntity(Map<String, Object> pData) throws Exception
	{
		Object key = null;
		
		if (isSingleIdAttribute())
		{
			if (JPAStorageUtil.isPrimitiveOrWrapped(getKeyClass()))
			{
				// Is single primitive or wrapped Id
				JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaDataAsArray()[0];
				
				key = serverColumnMetaData.getJPAMappingType().castObjectToJavaType(pData.get(serverColumnMetaData.getName()));
			}
			else
			{
				// Is EmbeddedId
				key = getKeyClass().newInstance();
				
				for (JPAServerColumnMetaData serverColumnMetaData : getServerColumnMetaDataAsArray())
				{
					if (serverColumnMetaData.isKeyAttribute())
					{
						Object setValue = pData.get(serverColumnMetaData.getName());
						
						serverColumnMetaData.getJPAMappingType().setValue(key, setValue);
					}
				}
			}
		}
		else
		{
			//is IdClass
			key = getKeyClass().newInstance();
			
			for (JPAServerColumnMetaData serverColumnMetaData : getServerColumnMetaDataAsArray())
			{
				if (serverColumnMetaData.isKeyAttribute())
				{
					Object setValue = pData.get(serverColumnMetaData.getName());
					
					serverColumnMetaData.getJPAMappingType().setValue(key, setValue);
				}
			}
		}
		
		return key;
	}
	
}	// JPAForeignKey
