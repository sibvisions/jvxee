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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.rad.model.condition.CompareCondition;
import javax.rad.model.condition.ICondition;
import javax.rad.model.condition.Not;
import javax.rad.model.condition.OperatorCondition;
import javax.rad.persist.ColumnMetaData;
import javax.rad.persist.MetaData;

/**
 * The {@link JPAServerMetaData} is a description of all columns as
 * {@link JPAServerColumnMetaData} One {@link JPAServerMetaData} encapsulates the
 * {@link JPAServerColumnMetaData} for a JPAStorage in different groups. The
 * Groups of {@link JPAServerMetaData} are primary keys, foreign keys and
 * embedded Objects in an Entity.
 * <p>
 * It also includes the server relevant infos, in addition to the
 * {@link MetaData} just for the client.
 * 
 * @author Stefan Wurm
 */
public class JPAServerMetaData
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** The {@link MetaData} for the client. */
	private MetaData metaData;
	
	/**
	 * The encapsulation for the {@link JPAServerColumnMetaData} for the primary
	 * key columns.
	 **/
	private JPAPrimaryKey primaryKey;
	
	/**
	 * The encapsulation for the {@link JPAServerColumnMetaData} for the
	 * embedded Objects columns.
	 **/
	private Collection<JPAEmbeddedKey> cJPAEmbeddedKey = new ArrayList<JPAEmbeddedKey>();
	
	/**
	 * The encapsulation for the {@link JPAServerColumnMetaData} for the foreign
	 * key columns.
	 **/
	private Collection<JPAForeignKey> cJPAForeignKey = new ArrayList<JPAForeignKey>();
	
	/**
	 * The encapsulation of all {@link JPAServerColumnMetaData} which are not
	 * primary key, foreign key or embedded Object columns.
	 */
	private Collection<JPAServerColumnMetaData> cServerColumnMetaData = new LinkedHashSet<JPAServerColumnMetaData>();
	
	/**
	 * If the {@link JPAServerMetaData} is a description of a ManyToMany
	 * relation between two entities.
	 */
	private boolean manyToMany = false;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Creates a new instance of {@link JPAServerMetaData}.
	 */
	public JPAServerMetaData()
	{
		metaData = new MetaData();
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Gets if the given {@link ICondition}s are {@code true}.
	 * 
	 * @param pCondition1 the first {@link ICondition}
	 * @param pCondition2 the second {@link ICondition}
	 * @return {@code true} if both {@link ICondition} are {@code true}.
	 */
	public boolean compareConditions(ICondition pCondition1, ICondition pCondition2)
	{
		boolean bEqual = false;
		
		if (pCondition1 instanceof CompareCondition && pCondition2 instanceof CompareCondition)
		{
			if (((CompareCondition)pCondition1).getColumnName().equals(((CompareCondition)pCondition2).getColumnName()))
			{
				return true;
			}
		}
		else if (pCondition1 instanceof OperatorCondition && pCondition2 instanceof OperatorCondition)
		{
			ICondition[] subConditions1 = ((OperatorCondition)pCondition1).getConditions();
			ICondition[] subConditions2 = ((OperatorCondition)pCondition2).getConditions();
			
			if (subConditions1.length == subConditions2.length)
			{
				for (int i = 0; i < subConditions1.length; i++)
				{
					bEqual = compareConditions(subConditions1[i], subConditions2[i]);
				}
			}
			else
			{
				return false;
			}
		}
		else if (pCondition1 instanceof Not && pCondition2 instanceof Not)
		{
			ICondition subCondition1 = ((Not)pCondition1).getCondition();
			ICondition subCondition2 = ((Not)pCondition2).getCondition();
			
			bEqual = compareConditions(subCondition1, subCondition2);
		}
		
		return bEqual;
	}
	
	/**
	 * Gets if the {@link JPAServerMetaData} is a ManyToMany representation.
	 * 
	 * @return true when {@link JPAServerMetaData} is a ManyToMany
	 *         representation.
	 */
	public boolean isManyToMany()
	{
		return manyToMany;
	}
	
	/**
	 * - * Sets the {@link JPAPrimaryKey} for the {@link JPAServerMetaData}.
	 * 
	 * @param pPrimaryKey the {@link JPAPrimaryKey} to set.
	 */
	public void setJPAPrimaryKey(JPAPrimaryKey pPrimaryKey)
	{
		primaryKey = pPrimaryKey;
		
		metaData.setPrimaryKeyColumnNames(pPrimaryKey.getColumnNames());
		
		// Set the ColumnMetaData for the primary Keys
		for (JPAServerColumnMetaData serverColumnMetaData : pPrimaryKey.getServerColumnMetaDataAsArray())
		{
			metaData.addColumnMetaData(serverColumnMetaData.getColumnMetaData());
		}
	}
	
	/**
	 * Gets the {@link JPAPrimaryKey} for the {@link JPAServerMetaData}.
	 * 
	 * @return the {@link JPAPrimaryKey} object.
	 */
	public JPAPrimaryKey getJPAPrimaryKey()
	{
		return primaryKey;
	}
	
	/**
	 * Add a {@link JPAEmbeddedKey} to the {@link JPAServerMetaData}.
	 * 
	 * @param pJPAEmbeddedKey the {@link JPAEmbeddedKey} to add.
	 */
	public void addJPAEmbeddedKey(JPAEmbeddedKey pJPAEmbeddedKey)
	{
		cJPAEmbeddedKey.add(pJPAEmbeddedKey);
		
		// Set the ColumnMetaData for the Embedded Entities
		for (JPAServerColumnMetaData serverColumnMetaData : pJPAEmbeddedKey.getServerColumnMetaDataAsArray())
		{
			metaData.addColumnMetaData(serverColumnMetaData.getColumnMetaData());
		}
	}
	
	/**
	 * Gets all {@link JPAPrimaryKey} from the {@link JPAServerMetaData}.
	 * 
	 * @return the {@link JPAPrimaryKey} objects in a collection.
	 */
	public Collection<JPAEmbeddedKey> getJPAEmbeddedKeys()
	{
		return cJPAEmbeddedKey;
	}
	
	/**
	 * Add a {@link JPAForeignKey} to the {@link JPAServerMetaData}.
	 * 
	 * @param pJPAForeignKey the {@link JPAForeignKey} to add.
	 */
	public void addJPAForeignKey(JPAForeignKey pJPAForeignKey)
	{
		cJPAForeignKey.add(pJPAForeignKey);
		
		// Set the ColumnMetaData for the foreign Keys
		for (JPAServerColumnMetaData serverColumnMetaData : pJPAForeignKey.getServerColumnMetaDataAsArray())
		{
			metaData.addColumnMetaData(serverColumnMetaData.getColumnMetaData());
		}
	}
	
	/**
	 * Gets all {@link JPAForeignKey} from the {@link JPAServerMetaData}.
	 * 
	 * @return the {@link JPAForeignKey} objects in a collection.
	 */
	public Collection<JPAForeignKey> getJPAForeignKeys()
	{
		return cJPAForeignKey;
	}
	
	/**
	 * Gets the {@link MetaData}.
	 * 
	 * @return the {@link MetaData}.
	 */
	public MetaData getMetaData()
	{
		return metaData;
	}
	
	/**
	 * Set the {@link MetaData} client infos.
	 * 
	 * @param pMetaData the {@link MetaData}.
	 */
	public void setMetaData(MetaData pMetaData)
	{
		metaData = pMetaData;
	}
	
	/**
	 * Is {@code true} if the {@link JPAServerMetaData} is a ManyToMany
	 * representation.
	 * 
	 * @param pManyToMany {@code true} if it is a ManyToMany representation.
	 */
	public void setManyToMany(boolean pManyToMany)
	{
		manyToMany = pManyToMany;
	}
	
	/**
	 * Add a {@link JPAServerColumnMetaData} to the {@link JPAServerMetaData}.
	 * 
	 * @param pServerColumnMetaData the {@link JPAServerColumnMetaData} to add.
	 */
	public void addServerColumnMetaData(JPAServerColumnMetaData pServerColumnMetaData)
	{
		cServerColumnMetaData.add(pServerColumnMetaData);
		
		metaData.addColumnMetaData(pServerColumnMetaData.getColumnMetaData());
	}
	
	/**
	 * Gets all {@link JPAServerColumnMetaData} columns which are not primary
	 * key, foreign key or embedded object columns.
	 * 
	 * @return all {@link JPAServerColumnMetaData} which are not primary key,
	 *         foreign key or embedded object columns.
	 */
	public JPAServerColumnMetaData[] getServerColumnMetaData()
	{
		return cServerColumnMetaData.toArray(new JPAServerColumnMetaData[cServerColumnMetaData.size()]);
	}
	
	/**
	 * Gets the index for the columnName.
	 * 
	 * @param pColumnName the column name.
	 * @return the index
	 */
	public int getColumnMetaDataIndex(String pColumnName)
	{
		return getMetaData().getColumnMetaDataIndex(pColumnName);
	}
	
	/**
	 * Gets the column names.
	 * 
	 * @return the column names.
	 */
	public String[] getColumnNames()
	{
		return metaData.getColumnNames();
	}
	
	/**
	 * Gets the {@link JPAForeignKey} of the given {@link ICondition}.
	 * 
	 * @param pCondition the {@link ICondition}.
	 * @return the {@link JPAForeignKey}.
	 */
	public JPAForeignKey getJPAForeignKeyForCondition(ICondition pCondition)
	{
		for (JPAForeignKey jpaForeignKey : cJPAForeignKey)
		{
			ICondition condition = jpaForeignKey.getCondition();
			
			if (compareConditions(condition, pCondition))
			{
				return jpaForeignKey;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets the {@link JPAServerColumnMetaData} for the given names.
	 * 
	 * @param pColumnName the name of the column.
	 * @return the {@link JPAServerColumnMetaData}.
	 */
	public JPAServerColumnMetaData getServerColumnMetaData(String pColumnName)
	{
		for (JPAServerColumnMetaData serverColumnMetaData : primaryKey.getServerColumnMetaDataAsCollection())
		{
			if (serverColumnMetaData.isKeyAttribute() && serverColumnMetaData.getName().equals(pColumnName))
			{
				return serverColumnMetaData;
			}
		}
		
		for (JPAEmbeddedKey jpaEmbeddedKey : cJPAEmbeddedKey)
		{
			for (JPAServerColumnMetaData serverColumnMetaData : jpaEmbeddedKey.getServerColumnMetaDataAsCollection())
			{
				if (serverColumnMetaData.getName().equals(pColumnName))
				{
					return serverColumnMetaData;
				}
			}
		}
		
		for (JPAForeignKey jpaForeignKey : cJPAForeignKey)
		{
			for (JPAServerColumnMetaData serverColumnMetaData : jpaForeignKey.getServerColumnMetaDataAsCollection())
			{
				if (serverColumnMetaData.getName().equals(pColumnName))
				{
					return serverColumnMetaData;
				}
			}
		}
		
		for (JPAServerColumnMetaData serverColumnMetaData : cServerColumnMetaData)
		{
			if (serverColumnMetaData.getName().equals(pColumnName))
			{
				return serverColumnMetaData;
			}
		}
		
		return null;
	}
	
	/**
	 * Gets a {@link Map} for the data row. The key of the Map is the name of
	 * the column and the value is the Value from the data row.
	 * 
	 * @param pDataRow the data row.
	 * @return the {@link Map} for the given data row.
	 */
	public Map<String, Object> getMapForDataRow(Object[] pDataRow)
	{
		Map<String, Object> mapDataRow = new HashMap<String, Object>();
		
		for (int i = 0; i < getMetaData().getColumnMetaData().length; i++)
		{
			ColumnMetaData serverColumnMetaData = getMetaData().getColumnMetaData()[i];
			
			mapDataRow.put(serverColumnMetaData.getName(), pDataRow[i]);
		}
		
		return mapDataRow;
	}
	
	/**
	 * Gets a {@link Map} for the given {@link ICondition}. The key of the
	 * {@link Map} is the name of the column and the value is the value from the
	 * data row. It contains all values and columnNames from the given
	 * {@link ICondition}.
	 * 
	 * @param pCondition the {@link ICondition}.
	 * @return the {@link Map} for the given data row.
	 */
	public Map<String, Object> getValueMapForCondition(ICondition pCondition)
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		initializeMapForCondition(pCondition, map);
		
		return map;
	}
	
	/**
	 * Gets the quoted representation column names.
	 *
	 * @return the quoted representation column names.
	 */
	public String[] getRepresentationQuotedColumnNames()
	{
		return metaData.getRepresentationColumnNames();
	}
	
	/**
	 * Sets the representation column names.
	 *
	 * @param pRepresentationColumnNames the representation column names to set.
	 */
	public void setRepresentationColumnNames(String[] pRepresentationColumnNames)
	{
		metaData.setRepresentationColumnNames(pRepresentationColumnNames);
	}
	
	/**
	 * Initialize the given {@link Map} with the keys and values of the given
	 * {@link ICondition}.
	 * 
	 * @param pCondition the {@link ICondition}.
	 * @param map the {@link Map}.
	 */
	private void initializeMapForCondition(ICondition pCondition, Map<String, Object> map)
	{
		if (pCondition instanceof CompareCondition)
		{
			String columnName = ((CompareCondition)pCondition).getColumnName();
			Object value = ((CompareCondition)pCondition).getValue();
			
			map.put(columnName, value);
		}
		else if (pCondition instanceof OperatorCondition)
		{
			for (ICondition subCondition : ((OperatorCondition)pCondition).getConditions())
			{
				initializeMapForCondition(subCondition, map);
			}
		}
		else if (pCondition instanceof Not)
		{
			initializeMapForCondition(((Not)pCondition).getCondition(), map);
		}
	}
	
}	// JPAServerMetaData
