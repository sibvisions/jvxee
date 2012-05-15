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
 * The <code>JPAForeignKey</code> is a JPAEmbeddeKey an encapsulates additional information and methods.
 * A JPAForeignKey encapsulates the <code>JPAServerColumnMetaData</code> for an entity in an entity. There
 * exists an entity in an entity when there is a OneToOne or OneToMany relationship between these entities.
 * 
 * And so an entity in an entity is similar to an embedded Object in an entity.
 * 
 * For example: An Entity "Customer" has a ManyToOne Relationship to Salutations
 * 
 * @Entity
 * public class Customer implements Serializable
 * {
 * 	   @Id
 *     private int id;
 *  
 *     private String name;
 *  
 *     @ManyToOne
 *     private Salutations salutations;
 *  
 *     ...
 * } 
 * 
 * @Entity
 * public class Salutations implements Serializable
 * {
 *     @Id
 *     private int id;
 *  
 *     private String salutation;
 * }
 *
 * @see com.sibvisions.rad.persist.jpa.JPAEmbeddedKey
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

	private String [] referencedColumnNames; 
	
	/** Getter Method Name for Detail Entities. */
	private String detailEntitiesMethode;
	
	/** The Add Methode Name to the Detail Entities. */	
	private String addEntityMethode;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Overwritten methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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
	 * Sets the class of the key from the entity in the entity.
	 * 
	 * @param pKeyClass
	 */
	public void setKeyClass(Class pKeyClass) 
	{
		keyClass = pKeyClass;
	}
	
	/**
	 * Returns the class of the key from the entity.
	 * 
	 * @return Class
	 */
	public Class getKeyClass() 
	{
		return keyClass;
	}

	public String[] getReferencedColumnNames() 
	{
		return referencedColumnNames;
	}

	public void setReferencedColumnNames(String[] pReferencedColumnNames) 
	{
		referencedColumnNames = pReferencedColumnNames;
	}

	public void setDetailEntitiesMethode(String pDetailEntitiesMethode) 
	{
		detailEntitiesMethode = pDetailEntitiesMethode;
	}
	
	public String getDetailEntitiesMethode() 
	{
		return detailEntitiesMethode;
	}	
	
	public boolean hasDetailEntitiesMethode() 
	{
		if (detailEntitiesMethode != null && detailEntitiesMethode.length() > 0) 
		{
			return true;
		} 
		
		return false;
	}	
	
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
	
	public void setAddEntityMethode(String pAddEntityMethode)
	{
		addEntityMethode = pAddEntityMethode;
	}	
	
	public void addEntity(Object pEntity, Object pAddEntity) throws Exception 
	{
		try 
		{
			pEntity.getClass().getMethod(addEntityMethode, pAddEntity.getClass()).invoke(pEntity);
		} 
		catch (Exception e) 
		{
			//nothing to be done
		}
	}	
		
	
	public ICondition getCondition() 
	{
		ICondition condition = null;
		
		for (JPAServerColumnMetaData serverColumnMetaData : super.getServerColumnMetaDataAsCollection()) 
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
	 * Returns the Primary Key for the entity in the entity with the given values
	 * 
	 * ****************************************************************************
	 * 
	 * For example: An entity can have three different types of primary keys:
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
	 *          { 
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
	 *  The Map with the Values for the Key has as Key the Name of the <code>JPAServerColumnMetaData</code> and as value the
	 *  value for the primary key.
	 *  
	 *  { CUSTOMER_ID = 3, CUSTOMER_SOCIALINSURANCENUMBER = 12345 }
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
				// Is single primitive or wrapped Id
				JPAServerColumnMetaData serverColumnMetaData = super.getServerColumnMetaDataAsArray()[0];

				key = serverColumnMetaData.getJPAMappingType().castObjectToJavaType(pData.get(serverColumnMetaData.getName()));
			} 
			else 
			{ 
				// Is EmbeddedId
				key = getKeyClass().newInstance();
				
				for (JPAServerColumnMetaData serverColumnMetaData : super.getServerColumnMetaDataAsArray()) 
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
			
			for (JPAServerColumnMetaData serverColumnMetaData : super.getServerColumnMetaDataAsArray()) 
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
