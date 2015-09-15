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

import javax.rad.model.reference.StorageReferenceDefinition;
import javax.rad.persist.ColumnMetaData;

/**
 * A <code>JPAServerColumnMetaData</code> is a description of a <code>JPAMappingType</code>, 
 * a <code>IDataType</code> and other attributes of a JPA storage column.
 * It also includes the server relevant infos, in addition to the <code>ColumnMetaData</code> just for the client.
 * 
 * Every primitive or wrapped attribute from an entity is represented by one <code>JPAServerColumnMetaData</code>.
 *  
 * @see javax.rad.model.datatype.IDataType
 * @see javax.rad.persist.ColumnMetaData
 * 
 * @author Stefan Wurm
 */
public class JPAServerColumnMetaData 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/** The MappingType for this column meta data. **/
	private JPAMappingType jpaMappingType;
	
	/** The ColumnMetaData for the client. **/
	private ColumnMetaData	columnMetaData;
	
	/** The name of the column meta data. **/
	private String name;
	
	/** If the column meta data is part of the primary key. **/
	private boolean keyAttribute = false;
	
	/** if the JPAServerColumnMetaData is a storageReference. **/
	private boolean storageReference = false;

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Constructs a <code>JPAServerColumnMetaData</code>.
	 */	
	public JPAServerColumnMetaData() 
	{
		columnMetaData = new ColumnMetaData();
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Overwritten methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() 
	{
		final int prime = 31;
		
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj)
		{
			return true;
		}
		
		if (obj == null)
		{
			return false;
		}
		
		if (getClass() != obj.getClass())
		{
			return false;
		}
		
		JPAServerColumnMetaData other = (JPAServerColumnMetaData) obj;
		
		if (name == null) 
		{
			if (other.name != null)
			{
				return false;
			}
		} 
		else if (!name.equals(other.name))
		{
			return false;
		}
		
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() 
	{
		return "JPAServerColumnMetaData [jpaMappingType=" + jpaMappingType
				+ ", columnMetaData=" + columnMetaData + ", name=" + name
				+ ", keyAttribute=" + keyAttribute + "]";
	}	

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Returns the <code>JPAMappingType</code>. 
	 * 
	 * @return The <code>JPAMappingType</code> 
	 */
	public JPAMappingType getJPAMappingType() 
	{
		return jpaMappingType;
	}

	/**
	 * Set the <code>JPAMappingType</code> for this <code>JPAServerColumnMetaData</code>.
	 * 
	 * @param pJpaMappingType the JPAMappingType
	 */
	public void setJPAMappingType(JPAMappingType pJpaMappingType) 
	{
		jpaMappingType = pJpaMappingType;
		
		setDataType(jpaMappingType.getDataType());
	}
	
	/**
	 * Returns the ColumnMetaData client infos.
	 * 
	 * @return the ColumnMetaData client infos.
	 */
	public ColumnMetaData getColumnMetaData() 
	{
		return columnMetaData;
	}	

	/**
	 * Sets true if the <code>JPAServerColumnMetaData</code> is part of the primary key.
	 * 
	 * @param pKeyAttribute true if the JPAServerColumnMetaData is part of the primary key
	 */
	public void setKeyAttribute(boolean pKeyAttribute)
	{
		keyAttribute = pKeyAttribute;
	}
	
	/**
	 * Returns true if the <code>JPAServerColumnMetaData</code> is part of the primary key.
	 * 
	 * @return true if the <code>JPAServerColumnMetaData</code> is part of the primary key
	 */
	public boolean isKeyAttribute() 
	{
		return keyAttribute;
	}

	/**
	 * Returns true if the <code>JPAServerColumnMetaData</code> is a StorageReference Column.
	 * 
	 * @return true if the <code>JPAServerColumnMetaData</code> is a StorageReference Column
	 */
	public boolean isStorageReference() 
	{
		return storageReference;
	}

	/**
	 * Sets true if the <code>JPAServerColumnMetaData</code> is a StorageReference Column.
	 * 
	 * @param pStorageReference true if it is a StorageReference
	 */
	public void setStorageReference(boolean pStorageReference) 
	{
		storageReference = pStorageReference;
	}

	/**
	 * Returns the column name.
	 * 
	 * @return the column name
	 */
	public String getName()
	{
		return columnMetaData.getName();
	}

	/**
	 * Sets the Name of the column.
	 * 
	 * @param pName the column name
	 */
	public void setName(String pName) 
	{
		name = pName;
		
		columnMetaData.setName(pName);
	}
	
	/**
	 * Returns the default label.
	 *
	 * @return the default label.
	 */
	public String getLabel()
	{
		return columnMetaData.getLabel();
	}

	/**
	 * Sets default label.
	 *
	 * @param pLabel the default label. to set
	 */
	public void setLabel(String pLabel)
	{
		columnMetaData.setLabel(pLabel);
	}
	
	/**
	 * Sets the used data type this <code>ColumnMetaData</code>.
	 * 
	 * @param pTypeIdentifier the type identifier for this <code>ColumnMetaData</code>.
	 */
	public void setDataType(int pTypeIdentifier)
	{
		columnMetaData.setTypeIdentifier(pTypeIdentifier);
	}

	/**
	 * Sets whether values in this column may be null.
	 * 
	 * @param pNullable true if values in this column may be null.
	 */
	public void setNullable(boolean pNullable)
	{
		columnMetaData.setNullable(pNullable);
	}

	/**
	 * Returns true if values in this column may be null.
	 * 
	 * @return true if values in this column may be null.
	 */
	public boolean isNullable()
	{
		return columnMetaData.isNullable();
	}

	/**
	 * Returns the precision/size of this column. 
	 * 
	 * @return the precision/size of this column. 
	 */
	public int getPrecision()
	{
		return columnMetaData.getPrecision();
	}

	/**
	 * Sets the precision/size of this column. 
	 * 
	 * @param pPrecision
	 *            the precision/size of this column.
	 */
	public void setPrecision(int pPrecision)
	{
		columnMetaData.setPrecision(pPrecision);
	}

	/**	
	 * Returns the scale of this column. 
	 * 
	 * @return the scale of this column. 
	 */
	public int getScale()
	{
		return columnMetaData.getScale();
	}

	/**
	 * Sets the scale of this column. 
	 * 
	 * @param pScale the scale of this column.
	 */
	public void setScale(int pScale)
	{
		columnMetaData.setScale(pScale);
	}

	/**
	 * Sets whether this column is writeable.
	 * 
	 * @param pWriteable true if column is writeable.
	 */
	public void setWritable(boolean pWriteable)
	{
		columnMetaData.setWritable(pWriteable);
	}

	/**
	 * Returns whether this column is writeable.
	 * 
	 * @return true if column is writeable.
	 */
	public boolean isWritable()
	{
		return columnMetaData.isWritable();
	}

	/**
	 * Returns if this <code>ColumnMetaData</code> is signed.
	 *
	 * @return if this <code>ColumnMetaData</code> is signed.
	 */
	public boolean isSigned()
	{
		return columnMetaData.isSigned();
	}

	/**
	 * Sets if this <code>ColumnMetaData</code> is signed.
	 *
	 * @param pSigned true, if signed.
	 */
	public void setSigned(boolean pSigned)
	{
		columnMetaData.setSigned(pSigned);
	}
	
	/**
	 * Returns <code>true</code> if this <code>ColumnMetaData</code> is an auto increment column.
	 *
	 * @return <code>true</code> if this <code>ColumnMetaData</code> is an auto increment column.
	 */
	public boolean isAutoIncrement()
	{
		return columnMetaData.isAutoIncrement();
	}

	/**
	 * Sets if this <code>ColumnMetaData</code> is an auto increment column.
	 *
	 * @param pAutoIncrement the bAutoIncrement to set
	 */
	public void setAutoIncrement(boolean pAutoIncrement)
	{
		columnMetaData.setAutoIncrement(pAutoIncrement);
	}

	/**
	 * Returns the link reference for a server side dropdown list (automatic linked celleditor).
	 *
	 * @return the link reference for a server side dropdown list.
	 */
	public StorageReferenceDefinition getLinkReference()
	{
		return columnMetaData.getLinkReference();
	}

	/**
	 * Sets the link reference for a server side Dropdown list (automatic linked celleditor).
	 *
	 * @param pLinkReference the link reference to set
	 */
	public void setLinkReference(StorageReferenceDefinition pLinkReference)
	{
		if (isStorageReference()) 
		{
			columnMetaData.setLinkReference(pLinkReference);
		}
	}	

	/**
	 * Sets the default value of this column.
	 * 
	 * @param pValue the default value
	 */
	public void setDefaultValue(Object pValue)
	{
		columnMetaData.setDefaultValue(pValue);
	}
	
	/**
	 * Gets the default value of this column.
	 * 
	 * @return the default value or <code>null</code> if the column has no default value
	 */
	public Object getDefaultValue()
	{
		return columnMetaData.getDefaultValue();
	}
	
	/**
	 * Sets the allowed values for this column.
	 * 
	 * @param pValues the allowed values or <code>null</code> when any value is allowed
	 */
	public void setAllowedValues(Object[] pValues)
	{
		columnMetaData.setAllowedValues(pValues);
	}
	
	/**
	 * Gets the allowed values for this column.
	 * 
	 * @return an {@link Object}[] with values, possible for the column.
	 */
	public Object[] getAllowedValues()
	{
		return columnMetaData.getAllowedValues();
	}

}	// JPAServerColumnMetaData
