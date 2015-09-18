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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.rad.model.ModelException;
import javax.rad.model.datatype.BigDecimalDataType;
import javax.rad.model.datatype.BinaryDataType;
import javax.rad.model.datatype.BooleanDataType;
import javax.rad.model.datatype.IDataType;
import javax.rad.model.datatype.StringDataType;
import javax.rad.model.datatype.TimestampDataType;

/**
 * The {@link JPAMappingType} encapsulates attributes and methods to do the
 * mapping between the attributes from the entity to the values of a DataRow.
 * <p>
 * Every {@link JPAServerColumnMetaData} and {@link JPAEmbeddedKey} holds one
 * {@link JPAMappingType}.
 * <p>
 * With a {@link JPAMappingType} it is possible to set and get values from an
 * Entity, for example:
 * 
 * <pre>
 * <code>
 * {@literal @}Entity
 * public class Customer implements Serializable  
 * {
 *      {@literal @}Id
 *      private int id;
 *  
 *      private String name;
 *      
 *      {@literal @}Embedded
 *      private Address address;
 *   
 *      ....
 * }
 * </code>
 * </pre>
 * <p>
 * The {@link JPAMappingType} for the Attribute id:
 * 
 * <pre>
 * The dataType is: BigDecimalDataType.TYPE_IDENTIFIER
 * The entityClass is: Customer
 * The javaTypeClass is: int
 * The getterMethodName is: getId
 * The setterMethodName is: setId
 * </pre>
 * 
 * The {@link JPAMappingType} for the Attribute address:
 * 
 * <pre>
 * The dataType is: ObjectDataType.TYPE_IDENTIFIER
 * The entityClass is: Customer
 * The javaTypeClass is: Address
 * The getterMethodName is: getAddress
 * The setterMethodName is: setAddress
 * </pre>
 * 
 * @author Stefan Wurm
 */
public class JPAMappingType
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** The {@link IDataType} from the column in the data row. **/
	private IDataType dataType;
	
	/** The {@link Class} of the entity. **/
	private Class entityClass;
	
	/** The name of the getter method for the attribute. **/
	private String getterMethodName;
	
	/** The {@link Class java type} of the attribute. **/
	private Class javaTypeClass;
	
	/** The path navigation. **/
	private ArrayList<String> pathNavigation = new ArrayList<String>();
	
	/** The name of the setter method for the attribute. **/
	private String setterMethodName;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Creates a new instance of {@link JPAMappingType}.
	 */
	public JPAMappingType()
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
		return "JPAMappingType [dataType=" + dataType + ", entityClass="
				+ entityClass + ", javaTypeClass=" + javaTypeClass
				+ ", getterMethodName=" + getterMethodName
				+ ", setterMethodName=" + setterMethodName + "]";
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Adds a path element.
	 * 
	 * @param pPathNavigation the path element to add to the navigation.
	 */
	public void addPathNavigation(String pPathNavigation)
	{
		pathNavigation.add(pPathNavigation);
	}
	
	/**
	 * Casts the given value from the data row to the java type from the
	 * attribute of the entity.
	 * 
	 * @param pValue the value to cast.
	 * @return the casted value.
	 * @throws ModelException if the conversion of the given value failed.
	 */
	public Object castObjectToJavaType(Object pValue) throws ModelException
	{
		pValue = dataType.convertToTypeClass(pValue);
		
		if (pValue == null)
		{
			return null;
		}
		else if (dataType instanceof StringDataType)
		{
			if (javaTypeClass == String.class)
			{
				return (String)pValue;
			}
			else if (javaTypeClass == char.class || javaTypeClass == Character.class)
			{
				return Character.valueOf(((String)pValue).charAt(0));
			}
		}
		else if (dataType instanceof BigDecimalDataType)
		{
			if (javaTypeClass == java.lang.Byte.class || javaTypeClass == byte.class)
			{
				return new Byte(((BigDecimal)pValue).byteValue());
			}
			else if (javaTypeClass == java.lang.Short.class || javaTypeClass == short.class)
			{
				return new Short(((BigDecimal)pValue).shortValue());
			}
			else if (javaTypeClass == java.lang.Integer.class || javaTypeClass == int.class)
			{
				return new Integer(((BigDecimal)pValue).intValue());
			}
			else if (javaTypeClass == java.lang.Long.class || javaTypeClass == long.class)
			{
				return new Long(((BigDecimal)pValue).longValue());
			}
			else if (javaTypeClass == java.lang.Float.class || javaTypeClass == float.class)
			{
				return new Float(((BigDecimal)pValue).floatValue());
			}
			else if (javaTypeClass == java.lang.Double.class || javaTypeClass == double.class)
			{
				return new Double(((BigDecimal)pValue).doubleValue());
			}
			else if (javaTypeClass == java.math.BigDecimal.class)
			{
				return pValue;
			}
		}
		else if (dataType instanceof BinaryDataType)
		{
			if (javaTypeClass == java.lang.Byte[].class)
			{
				return (java.lang.Byte[])pValue;
			}
			else if (javaTypeClass == byte[].class)
			{
				return (byte[])pValue;
			}
		}
		else if (dataType instanceof BooleanDataType)
		{
			return (java.lang.Boolean)pValue;
		}
		else if (dataType instanceof TimestampDataType)
		{
			if (javaTypeClass == java.util.Date.class)
			{
				return new java.util.Date(((Timestamp)pValue).getTime());
			}
			else if (javaTypeClass == java.sql.Date.class)
			{
				return new java.sql.Date(((Timestamp)pValue).getTime());
			}
		}
		else
		{
			// ObjectDataType
			return pValue;
		}
		
		throw new ModelException("Conversion failed! Type not supported ! from " +
				pValue.getClass().getName() + " to IDataType " + dataType.getTypeClass() + " to java Type " + javaTypeClass);
	}
	
	/**
	 * Returns the Value for the given entity.
	 * 
	 * @param pEntity The Entity, the Primary Key, Foreign Key or Embedded
	 *            Object
	 * @return the value for the given entity.
	 * @throws Exception if getting the value failed.
	 */
	public Object getValue(Object pEntity) throws Exception
	{
		if (pEntity != null)
		{
			if (JPAStorageUtil.isPrimitiveOrWrapped(pEntity.getClass()))
			{
				return pEntity;
			}
			
			return pEntity.getClass().getMethod(getterMethodName).invoke(pEntity);
			
		}
		
		return null;
	}
	
	/**
	 * Sets the given value to the given entity.
	 * 
	 * @param pEntity the entity, the primary key, foreign key or embedded
	 *            Object.
	 * @param pValue the value to set.
	 * @throws Exception if setting the value failed.
	 */
	public void setValue(Object pEntity, Object pValue) throws Exception
	{
		if (pEntity != null)
		{
			Object setValue = castObjectToJavaType(pValue);
			
			if (setValue != null)
			{
				pEntity.getClass().getMethod(setterMethodName, javaTypeClass).invoke(pEntity, setValue);
			}
		}
	}
	
	/**
	 * Returns the DataType Identifier.
	 * 
	 * @return the DataType Identifier.
	 */
	public int getDataType()
	{
		return dataType.getTypeIdentifier();
	}
	
	/**
	 * Sets the DataType Identifier.
	 * 
	 * @param pDataTypeIdentifier The DataTypeIdentifier.
	 */
	public void setDataType(IDataType pDataTypeIdentifier)
	{
		dataType = pDataTypeIdentifier;
	}
	
	/**
	 * Returns the class of the entity.
	 * 
	 * @return the class of the entity.
	 */
	public Class getEntityClass()
	{
		return entityClass;
	}
	
	/**
	 * Sets the class of the entity.
	 * 
	 * @param pEntityClass The class of the entity.
	 */
	public void setEntityClass(Class pEntityClass)
	{
		this.entityClass = pEntityClass;
	}
	
	/**
	 * Returns the class of the java Type.
	 * 
	 * @return the class of the java Type.
	 */
	public Class getJavaTypeClass()
	{
		return javaTypeClass;
	}
	
	/**
	 * Sets the class of the java Type.
	 * 
	 * @param pJavaTypeClass the class of the java type.
	 */
	public void setJavaTypeClass(Class pJavaTypeClass)
	{
		javaTypeClass = pJavaTypeClass;
	}
	
	/**
	 * Sets the name of the getter method for the Attribute.
	 * 
	 * @param pGetterMethodName the name of the getter method.
	 */
	public void setGetterMethodName(String pGetterMethodName)
	{
		this.getterMethodName = pGetterMethodName;
	}
	
	/**
	 * Sets the name of the setter method for the Attribute.
	 * 
	 * @param pSetterMethodName the name of the setter method.
	 */
	public void setSetterMethodName(String pSetterMethodName)
	{
		this.setterMethodName = pSetterMethodName;
	}
	
	/**
	 * Returns a {@link List} with the names of the attributes to this
	 * attribute.
	 * <p>
	 * For a Example the path navigation to the attribute street is: Address,
	 * street
	 * 
	 * <pre>
	 * <code>
	 * {@literal @}Entity
	 * public class Customer implements Serializable
	 * {
	 * 	   {@literal @}Id
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
	 * @return the {@link List} as path navigation.
	 */
	public List<String> getPathNavigation()
	{
		List<String> reversedPathNavigation = new ArrayList<String>(pathNavigation);
		Collections.reverse(reversedPathNavigation);
		
		return reversedPathNavigation;
	}
	
}	// JPAMappingType
