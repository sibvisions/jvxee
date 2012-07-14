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
 * The <code>JPAServerMetaData</code> is a description of all columns as <code>JPAServerColumnMetaData</code>.
 * One <code>JPAServerMetaData</code> encapsulates the <code>JPAServerColumnMetaData</code> for a JPAStorage in different groups.
 * The Groups of <code>JPAServerMetaData</code> are primary keys, foreign keys and embedded Objects in an Entity.
 * 
 * It also includes the server relevant infos, in addition to the <code>MetaData</code> just for the client.
 *  
 * @see com.sibvisions.rad.persist.jpa.ServerColumnMetaData
 * @see com.sibvisions.rad.persist.jpa.JPAPrimaryKey
 * @see com.sibvisions.rad.persist.jpa.JPAForeignKey
 * @see com.sibvisions.rad.persist.jpa.JPAEmbeddedKey
 * 
 * @author Stefan Wurm
 */
public class JPAServerMetaData 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/** The MetaData for the client. */
	private MetaData metaData;
	
	/** The encapsulation for the <code>JPAServerColumnMetaData</code> for the primary key columns. **/
	private JPAPrimaryKey primaryKey;
	
	/** The encapsulation for the <code>JPAServerColumnMetaData</code> for the embedded Objects columns. **/
	private Collection<JPAEmbeddedKey> cJPAEmbeddedKey = new ArrayList<JPAEmbeddedKey>();
	
	/** The encapsulation for the <code>JPAServerColumnMetaData</code> for the foreign key columns. **/
	private Collection<JPAForeignKey> cJPAForeignKey = new ArrayList<JPAForeignKey>();
	
	/** The encapsulation of all <code>JPAServerColumnMetaData</code> which are not primary key, foreign key or embedded Object columns. */
	private Collection<JPAServerColumnMetaData> cServerColumnMetaData = new LinkedHashSet<JPAServerColumnMetaData>();	

	/** If the JPAServerMetaData is a description of a ManyToMany relation between two entities. */
	private boolean manyToMany = false;

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Creates a new instance of <code>JPAServerMetaData</code> with new {@link MetaData}.
	 */	
	public JPAServerMetaData() 
	{
		metaData = new MetaData();
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
-	 * Sets the <code>JPAPrimaryKey</code> for the <code>JPAServerMetaData</code>.
	 * 
	 * @param pPrimaryKey the <code>JPAPrimaryKey</code> to set.
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
	 * Returns the <code>JPAPrimaryKey</code> for the <code>JPAServerMetaData</code>.
	 * 
	 * @return The JPAPrimaryKey Object
	 */	
	public JPAPrimaryKey getJPAPrimaryKey() 
	{
		return primaryKey;
	}	
	
	/**
	 * Add a <code>JPAEmbeddedKey</code> to the <code>JPAServerMetaData</code>.
	 * 
	 * @param pJPAEmbeddedKey the <code>JPAEmbeddedKey</code> to add.
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
	 * Returns all <code>JPAEmbeddedKey</code> from the <code>JPAServerMetaData</code>.
	 * 
	 * @return The JPAEmbeddedKey Objects in a Collection
	 */		
	public Collection<JPAEmbeddedKey> getJPAEmbeddedKeys() 
	{
		return cJPAEmbeddedKey;
	}	

	/**
	 * Add a <code>JPAForeignKey</code> to the <code>JPAServerMetaData</code>.
	 * 
	 * @param pJPAForeignKey the <code>JPAForeignKey</code> to add.
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
	 * Returns all <code>JPAForeignKey</code> from the <code>JPAServerMetaData</code>.
	 * 
	 * @return The JPAForeignKey Objects in a Collection
	 */		
	public Collection<JPAForeignKey> getJPAForeignKeys() 
	{
		return cJPAForeignKey;
	}
	
	/**
	 * Returns the MetaData client infos.
	 * 
	 * @return the MetaData client infos.
	 */	
	public MetaData getMetaData() 
	{
		return metaData;
	}

	/**
	 * Set the MetaData client infos.
	 * 
	 * @param pMetaData the metadata
	 */		
	public void setMetaData(MetaData pMetaData) 
	{
		metaData = pMetaData;
	}

	/**
	 * Returns if the <code>JPAServerMetaData</code> is a ManyToMany representation.
	 * 
	 * @return true when <code>JPAServerMetaData</code> is a ManyToMany representation
	 */		
	public boolean isManyToMany() 
	{
		return manyToMany;
	}

	/**
	 * Is true if the <code>JPAServerMetaData</code> is a ManyToMany representation.
	 * 
	 * @param pManyToMany true if it is a ManyToMany representation
	 */	
	public void setManyToMany(boolean pManyToMany) 
	{
		manyToMany = pManyToMany;
	}

	/**
	 * Add a <code>JPAServerColumnMetaData</code> to the <code>JPAServerMetaData</code>.
	 * 
	 * @param pServerColumnMetaData the <code>JPAServerColumnMetaData</code> to add.
	 */		
	public void addServerColumnMetaData(JPAServerColumnMetaData pServerColumnMetaData) 
	{
		cServerColumnMetaData.add(pServerColumnMetaData);
		
		metaData.addColumnMetaData(pServerColumnMetaData.getColumnMetaData());
	}

	/**
	 * Returns all <code>JPAServerColumnMetaData</code> columns which are not primary key, foreign key or embedded Object columns.
	 * 
	 * @return all <code>JPAServerColumnMetaData</code> which are not primary key, foreign key or embedded Object columns.
	 */
	public JPAServerColumnMetaData[] getServerColumnMetaData()
	{
		return cServerColumnMetaData.toArray(new JPAServerColumnMetaData[cServerColumnMetaData.size()]);
	}

	/**
	 * Returns the index for the columnName. 
	 * 
	 * @param pColumnName The Column Name
	 * @return the index
	 */
	public int getColumnMetaDataIndex(String pColumnName) 
	{
		return getMetaData().getColumnMetaDataIndex(pColumnName);
	}
	
	/**
	 * Returns the ColumnNames.
	 * 
	 * @return the ColumnNames
	 */
	public String[] getColumnNames() 
	{
		return metaData.getColumnNames();		
	}
	
	/**
	 * Returns the JPAForeignKey of the given ICondition.
	 * 
	 * @param pCondition the ICondition
	 * @return the JPAForeignKey
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
	 * Returns true if the given Conditions are true.
	 * 
	 * @param pCondition1 the first condition
	 * @param pCondition2 the second condition
	 * @return true if pCondtion1 is the same as pCondition2
	 */
	public boolean compareConditions(ICondition pCondition1, ICondition pCondition2) 
	{
		boolean bEqual = false;

		if (pCondition1 instanceof CompareCondition && pCondition2 instanceof CompareCondition) 
		{
			if (((CompareCondition) pCondition1).getColumnName().equals(((CompareCondition) pCondition2).getColumnName())) 
			{
				return true;
			}
		} 
		else if (pCondition1 instanceof OperatorCondition && pCondition2 instanceof OperatorCondition) 
		{
			ICondition[] subConditions1 = ((OperatorCondition) pCondition1).getConditions();
			ICondition[] subConditions2 = ((OperatorCondition) pCondition2).getConditions();
			
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
			ICondition subCondition1 = ((Not) pCondition1).getCondition();
			ICondition subCondition2 = ((Not) pCondition2).getCondition();
			
			bEqual = compareConditions(subCondition1, subCondition2);
		}
		
		return bEqual;
	}	

	/**
	 * Returns the JPAServerColumnMetaData for the given names.
	 * 
	 * @param pColumnName the name of the columnMetaData
	 * @return the JPAServerColumnMetaData
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
	 * Returns a <code>Map<String, Object></code> for the DataRow. 
	 * The key of the Map is the name of the column and the value is the Value from the DataRow
	 * 
	 * @param pDataRow The DataRow 
	 * @return The Map for the DataRow
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
	 * Returns a <code>Map<String, Object></code> for a Condition.
	 * The key of the Map is the name of the column and the value is the Value from the DataRow
	 * It contains all values and columnNames from the given condition.
	 * 
	 * @param pCondition condition
	 * @return The Map for the DataRow
	 */
	public Map<String, Object> getValueMapForCondition(ICondition pCondition) 
	{
		Map<String, Object> map = new HashMap<String, Object>();
		
		initializeMapForCondition(pCondition, map);
				
		return map;		
	}
	
	/**
	 * Initialize the given Map with the keys and values of the given condition.
	 * 
	 * @param pCondition 
	 * @param map 
	 */
	private void initializeMapForCondition(ICondition pCondition, Map<String, Object> map) 
	{
		if (pCondition instanceof CompareCondition) 
		{
			String columnName = ((CompareCondition) pCondition).getColumnName();
			Object value = ((CompareCondition) pCondition).getValue();				
			
			map.put(columnName, value);
		} 
		else if (pCondition instanceof OperatorCondition) 
		{
			for (ICondition subCondition : ((OperatorCondition) pCondition).getConditions()) 
			{
				initializeMapForCondition(subCondition, map);				
			}	
		} 
		else if (pCondition instanceof Not) 
		{
			initializeMapForCondition(((Not) pCondition).getCondition(), map);
		}
	}	
	
    /**
	 * Returns the quoted Representation column names.
	 *
	 * @return the quoted Representation column names.
	 */
	public String[] getRepresentationQuotedColumnNames() 
	{
		return metaData.getRepresentationColumnNames();
	}
	
	/**
	 * Sets the Representation column names. 
	 *
	 * @param pRepresentationColumnNames the Representation column names to set
	 */
	public void setRepresentationColumnNames(String[] pRepresentationColumnNames) 
	{
		metaData.setRepresentationColumnNames(pRepresentationColumnNames);
	}	
	
}	// JPAServerMetaData
