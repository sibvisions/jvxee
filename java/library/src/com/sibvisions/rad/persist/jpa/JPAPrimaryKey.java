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
 * The <code>JPAPrimaryKey</code> is a JPAEmbeddeKey an encapsulates additional information and methods.
 * A JPAPrimaryKey encapsulates the <code>JPAServerColumnMetaData</code> for an entity. 
 * 
 * For example: An entity can have three different types of primary keys:
 * 
 * *******************************************************************************
 * 
 * 1. A singled id attribute:
 *    
 *    		@Id
 *    		private int id;
 *    
 * 2. Many id attributes in an IdClass
 * 
 * 			public class CustomerPK implements Serializable
 *          {
 * 				@Id
 *  			private int id;
 *  
 *  			@Id
 *  			private int socialInsuranceNumber;
 *  
 *  			....
 * 			} 
 * 
 * 			@Entity
 * 			@IdClass(CustomerPK.class)
 * 			public class Customer implements Serializable
 * 			{ 
 * 				@Id
 *  			private int id;
 *  
 *  			@Id
 *  			private int socialInsuranceNumber;
 *  
 *  			private String name;
 *  
 *  			....
 * 			} 
 * 
 * 3. An Embedded Primary Class
 * 			
 * 			@Embeddable
 * 			public class CustomerPK implements Serializable
 * 			{ 
 * 				@Id
 *  			private int id;
 *  
 *  			@Id
 *  			private int socialInsuranceNumber;
 *  
 *  			....
 * 			} 
 * 
 * 			@Entity
 * 			public class Customer implements Serializable 
 *          {
 * 				@EmbeddedId
 * 				private CustomerPK customerPK;
 *  
 *  			private String name;
 *  
 *  			....
 *  		}
 *  
 *  *****************************************************************************
 * 
 * And so the primary key from an entity is similar to an embedded Object in an entity.
 * 
 * @see com.sibvisions.rad.persist.jpa.JPAEmbeddedKey
 * 
 * @author Stefan Wurm
 */
public class JPAPrimaryKey extends JPAEmbeddedKey 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** Is needed because of a Many To Many Relation where no entity exists. **/
	private Map<Class, JPAForeignKey> mapForeignKey = new HashMap<Class, JPAForeignKey>();

	/** Defines if the Primary Key from the entity is an embedded class. **/
	private boolean isEmbedded = false;
	
	/** Defines if the Id from the entity is a single id. **/
	private boolean singleIdAttribute = true;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Overwritten methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public String toString() 
	{
		return "JPAPrimaryKey [mapForeignKey=" + mapForeignKey
				+ ", isEmbedded=" + isEmbedded + ", singleIdAttribute="
				+ singleIdAttribute + ", toString()=" + super.toString() + "]";
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Returns true if the Id from the entity in the entity is a single id.
	 * 
	 * @return true if the Id from the entity in the entity is a single id
	 */	
	public boolean isSingleIdAttribute() 
	{
		return singleIdAttribute;
	}
	
	/**
	 * Sets the SingledIdAttribute.
	 * 
	 * @param pSingleIdAttribute
	 */
	public void setSingleIdAttribute(boolean pSingleIdAttribute) 
	{
		singleIdAttribute = pSingleIdAttribute;
	}
	
	/**
	 * Returns the class of the key from the entity.
	 * 
	 * @return Class
	 */	
	public Class getKeyClass() 
	{
		return super.getJPAMappingType().getJavaTypeClass();
	}
		
	/**
	 * True if the Primary Key from the entity is an embedded class.
	 *  
	 * @return true if the Primary Key from the entity is an embedded class
	 */
	public boolean isEmbedded() 
	{
		return isEmbedded;
	}

	/**
	 * Sets true if the primary key from the entity is an embedde class.
	 * 
	 * @param pIsEmbedded
	 */
	public void setEmbedded(boolean pIsEmbedded) 
	{
		isEmbedded = pIsEmbedded;
	}
	
	/**
	 * Adds a <code>JPAForeignKey</code> to the <code>JPAPrimaryKey</code>.
	 * Is needed because of a Many To Many relation where no entity exists.
	 * 
	 * For example:
	 * 
	 * If there is a ManyToMany relation between Order and Article it is not necessary
	 * to create an entity "OrderArticle".
	 * 
	 * And so the PrimaryKey for this relation is a combined ForeignKey from Order and Article
	 * 
	 * @param pEntity
	 * @param pForeignKey
	 */
	public void addForeignKey(Class pEntity, JPAForeignKey pForeignKey) 
	{
		mapForeignKey.put(pEntity, pForeignKey);
	}
	
	/**
	 * Returns the <code>JPAForeignKey</code> for the given class.
	 * 
	 * @param pEntity
	 * @return the <code>JPAForeignKey</code> for the given class 
	 */
	public JPAForeignKey getForeignKey(Class pEntity) 
	{
		return mapForeignKey.get(pEntity);
	}		

	/**
	 * Returns the Primary Key for the entity with the given values
	 *  
	 *  The Map with the Values for the Key has as Key the Name of the <code>JPAServerColumnMetaData</code> and as value the
	 *  value for the primary key.
	 *  
	 *  { ID = 3, SOCIALINSURANCENUMBER = 12345 }
	 * 
	 * @param pData the Map with the values for the key
	 * @return the primary key from the entity in the entity
	 * @throws Exception
	 */	
	public Object getKeyForEntity(Map<String, Object> pData) throws Exception 
	{
		
		Object key = null;

		if (isSingleIdAttribute()) 
		{
			if (JPAStorageUtil.isPrimitiveOrWrapped(getKeyClass())) 
			{
				JPAServerColumnMetaData serverColumnMetaData = super.getServerColumnMetaDataAsArray()[0];

				key = serverColumnMetaData.getJPAMappingType().castObjectToJavaType(pData.get(serverColumnMetaData.getName()));
			} 
			else 
			{ 
				// Is EmbeddedId
								
				key = getKeyClass().newInstance();
				
				for (JPAServerColumnMetaData serverColumnMetaData : super.getServerColumnMetaDataAsArray()) 
				{
					Object setValue = pData.get(serverColumnMetaData.getName());
					
					serverColumnMetaData.getJPAMappingType().setValue(key, setValue);
				}				
			}
		} 
		else 
		{
			key = (Object) getKeyClass().newInstance();
			
			for (JPAServerColumnMetaData serverColumnMetaData : super.getServerColumnMetaDataAsArray()) 
			{
				Object setValue = pData.get(serverColumnMetaData.getName());
				
				serverColumnMetaData.getJPAMappingType().setValue(key, setValue);
			}
		}
		
		return key;
	}
	
}	// JPAPrimaryKey
