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
 * 26.01.2013 - [JR] - executeFetch: throw new DataSourceException was missing
 */
package com.sibvisions.rad.persist.jpa;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Attribute.PersistentAttributeType;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.rad.model.SortDefinition;
import javax.rad.model.condition.ICondition;
import javax.rad.model.datatype.BinaryDataType;
import javax.rad.model.datatype.IDataType;
import javax.rad.model.datatype.StringDataType;
import javax.rad.model.reference.StorageReferenceDefinition;
import javax.rad.persist.ColumnMetaData;
import javax.rad.persist.DataSourceException;
import javax.rad.persist.IStorage;
import javax.rad.persist.MetaData;
import javax.rad.remote.IConnectionConstants;
import javax.rad.server.ISession;
import javax.rad.server.SessionContext;
import javax.rad.type.bean.IBean;

import com.sibvisions.rad.model.DataBookCSVExporter;
import com.sibvisions.rad.persist.AbstractCachedStorage;
import com.sibvisions.rad.persist.AbstractStorage;
import com.sibvisions.util.type.CommonUtil;
import com.sibvisions.util.type.StringUtil;

/**
 * The {@link JPAStorage} is an {@link AbstractCachedStorage} extension that
 * uses JPA, the Java Persistence API, as a backend.
 * 
 * @author Stefan Wurm
 */
public class JPAStorage extends AbstractCachedStorage
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/** Determines the default automatic link reference mode. */
	private static boolean defaultAutoLinkReference = true;
	
	/** Determines the automatic link reference mode. */
	private Boolean autoLinkReference;
	
	/** To create a criteria query for an ICondition. */
	private ConditionCriteriaMapper criteriaConditionMapper;
	
	/** The EntityClass for the ManyToMany relation. */
	private Class detailEntity;
	
	/** The internal JPA access object. */
	private JPAAccess jpaAccess;
	
	/** The user-defined JPA access object. */
	private JPAAccess jpaAccessUser;
	
	/** The EntityClass for the JPAStorage. */
	private Class masterEntity;
	
	/** The name of this storage. */
	private String name = "JPAStorage";
	
	/** The open state of this DBStorage. */
	private boolean open = false;
	
	/**
	 * The encapsulation for the JPAServerColumnMetaData, JPAPrimaryKey,
	 * JPAForeignKey and JPAEmbeddedKey.
	 */
	private JPAServerMetaData serverMetaData;
	
	/** the list of sub storages. */
	private HashMap<String, IStorage> subStorages;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Creates a new instance of {@link JPAStorage} for the given entity class.
	 * 
	 * @param pMasterEntity the master entity class.
	 * @throws DataSourceException if set master entity fails.
	 * @throws IllegalArgumentException if the given master entity is
	 *             {@code null}.
	 */
	public JPAStorage(Class pMasterEntity) throws DataSourceException
	{
		setMasterEntity(pMasterEntity);
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Abstract methods implementation
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws Throwable
	{
		// TODO Is there something to close?
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeDelete(Object[] pDeleteDataRow) throws DataSourceException
	{
		if (!isOpen())
		{
			throw new DataSourceException("JPAStorage isn't open!");
		}
		
		try
		{
			if (serverMetaData.isManyToMany())
			{
				JPAForeignKey foreignKey1 = serverMetaData.getJPAPrimaryKey().getForeignKey(masterEntity);
				JPAForeignKey foreignKey2 = serverMetaData.getJPAPrimaryKey().getForeignKey(detailEntity);
				
				Object primaryKey1 = foreignKey1.getKeyForEntity(serverMetaData.getMapForDataRow(pDeleteDataRow));
				Object primaryKey2 = foreignKey2.getKeyForEntity(serverMetaData.getMapForDataRow(pDeleteDataRow));
				
				Object entity1 = jpaAccess.findById(primaryKey1, masterEntity);
				
				Collection objectList = (Collection)foreignKey1.getDetailEntities(entity1);
				
				Object entity2 = jpaAccess.findById(primaryKey2, detailEntity);
				
				objectList.remove(entity2);
				
				jpaAccess.update(entity1, masterEntity);
			}
			else
			{
				Object primaryKey = serverMetaData.getJPAPrimaryKey().getKeyForEntity(serverMetaData.getMapForDataRow(pDeleteDataRow));
				
				Object entityForDelete = jpaAccess.findById(primaryKey, masterEntity);
				
				jpaAccess.delete(entityForDelete, masterEntity);
			}
		}
		catch (DataSourceException dse)
		{
			throw dse;
		}
		catch (Exception e)
		{
			throw new DataSourceException("Delete was not possible", e);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Object[]> executeFetch(ICondition pFilter, SortDefinition pSort, int pFromRow, int pMinimumRowCount) throws DataSourceException
	{
		if (!isOpen())
		{
			throw new DataSourceException("JPAStorage isn't open!");
		}
		
		List<Object[]> objects = new ArrayList<Object[]>();
		Collection objectList = new ArrayList();
		
		Object entityM = null;
		
		try
		{
			if (pFilter != null || pSort != null)
			{
				JPAForeignKey jpaForeignKey = serverMetaData.getJPAForeignKeyForCondition(pFilter);
				
				if (jpaForeignKey != null && jpaForeignKey.hasDetailEntitiesMethode() && pSort == null)
				{
					Map<String, Object> map = serverMetaData.getValueMapForCondition(pFilter);
					
					Object primaryKey = jpaForeignKey.getKeyForEntity(map);
					
					if (primaryKey == null)
					{
						CriteriaQuery criteriaQuery = criteriaConditionMapper.getCriteriaQuery(pFilter, pSort, masterEntity, null);
						
						objectList = jpaAccess.findByCriteria(criteriaQuery);
					}
					else
					{
						entityM = jpaAccess.findById(primaryKey, jpaForeignKey.getJPAMappingType().getJavaTypeClass());
						
						jpaAccess.refresh(entityM, jpaForeignKey.getJPAMappingType().getJavaTypeClass());
						
						Object obj = jpaForeignKey.getDetailEntities(entityM);
						
						if (obj != null && obj instanceof Collection)
						{
							if (obj instanceof Collection)
							{
								objectList = (Collection)obj;
							}
							else
							{
								objectList.add(obj);
							}
						}
					}
				}
				else
				{
					if (serverMetaData.isManyToMany())
					{
						CriteriaQuery criteriaQuery = criteriaConditionMapper.getCriteriaQuery(pFilter, null, masterEntity, null);
						
						Collection col = jpaAccess.findByCriteria(criteriaQuery);
						
						if (col.size() == 1)
						{
							entityM = col.iterator().next();
						}
						
						criteriaQuery = criteriaConditionMapper.getCriteriaQuery(pFilter, pSort, masterEntity,
								getDetailRelationAttribute(masterEntity, detailEntity,
										PersistentAttributeType.MANY_TO_MANY).getName());
										
						objectList = jpaAccess.findByCriteria(criteriaQuery);
					}
					else
					{
						CriteriaQuery criteriaQuery = criteriaConditionMapper.getCriteriaQuery(pFilter, pSort, masterEntity, null);
						
						objectList = jpaAccess.findByCriteria(criteriaQuery);
					}
				}
			}
			else
			{
				objectList = (Collection)jpaAccess.findAll(masterEntity);
			}
			
			for (Object entityD : objectList)
			{
				if (serverMetaData.isManyToMany())
				{
					Object[] dataRow = getDataRowForEntities(entityM, entityD);
					objects.add(dataRow);
					
				}
				else
				{
					Object[] dataRow = getDataRowForEntity(entityD);
					objects.add(dataRow);
				}
			}
		}
		catch (DataSourceException dse)
		{
			throw dse;
		}
		catch (Exception e)
		{
			throw new DataSourceException("Fetch was not possible", e);
		}
		
		objects.add(null);
		
		return objects;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] executeInsert(Object[] pDataRow) throws DataSourceException
	{
		if (!isOpen())
		{
			throw new DataSourceException("JPAStorage isn't open!");
		}
		
		Object[] dataRow = null;
		
		try
		{
			if (serverMetaData.isManyToMany())
			{
				JPAForeignKey foreignKey1 = serverMetaData.getJPAPrimaryKey().getForeignKey(masterEntity);
				JPAForeignKey foreignKey2 = serverMetaData.getJPAPrimaryKey().getForeignKey(detailEntity);
				
				Object primaryKey1 = foreignKey1.getKeyForEntity(serverMetaData.getMapForDataRow(pDataRow));
				Object primaryKey2 = foreignKey2.getKeyForEntity(serverMetaData.getMapForDataRow(pDataRow));
				
				if (primaryKey1 != null && primaryKey2 != null)
				{
					Object entity1 = jpaAccess.findById(primaryKey1, masterEntity);
					
					Collection objectList = (Collection)foreignKey1.getDetailEntities(entity1);
					
					Object entity2 = jpaAccess.findById(primaryKey2, detailEntity);
					
					objectList.add(entity2);
					
					jpaAccess.update(entity1, masterEntity);
					
					dataRow = getDataRowForEntities(entity1, entity2);
				}
			}
			else
			{
				Object entityForInsert = masterEntity.newInstance();
				
				mapDataRowToEntity(pDataRow, entityForInsert);
				
				entityForInsert = jpaAccess.insert(entityForInsert, masterEntity);
				
				dataRow = getDataRowForEntity(entityForInsert);
			}
		}
		catch (DataSourceException dse)
		{
			throw dse;
		}
		catch (Exception e)
		{
			throw new DataSourceException("Insert was not possible", e);
		}
		
		return dataRow;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] executeRefetchRow(Object[] pDataRow) throws DataSourceException
	{
		if (!isOpen())
		{
			throw new DataSourceException("JPAStorage isn't open!");
		}
		
		Object[] dataRow = null;
		
		try
		{
			Object primaryKey = serverMetaData.getJPAPrimaryKey().getKeyForEntity(serverMetaData.getMapForDataRow(pDataRow));
			
			Object entityForFetch = jpaAccess.findById(primaryKey, masterEntity);
			
			dataRow = getDataRowForEntity(entityForFetch);
		}
		catch (DataSourceException dse)
		{
			throw dse;
		}
		catch (Exception e)
		{
			new DataSourceException("Refetch was not possible", e);
		}
		
		return dataRow;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] executeUpdate(Object[] pOldDataRow, Object[] pNewDataRow) throws DataSourceException
	{
		if (!isOpen())
		{
			throw new DataSourceException("JPAStorage isn't open!");
		}
		
		Object[] dataRow = null;
		
		try
		{
			if (serverMetaData.isManyToMany())
			{
				JPAForeignKey foreignKey1 = serverMetaData.getJPAPrimaryKey().getForeignKey(masterEntity);
				JPAForeignKey foreignKey2 = serverMetaData.getJPAPrimaryKey().getForeignKey(detailEntity);
				
				Object primaryKey1 = foreignKey1.getKeyForEntity(serverMetaData.getMapForDataRow(pOldDataRow));
				Object primaryKeyOld2 = foreignKey2.getKeyForEntity(serverMetaData.getMapForDataRow(pOldDataRow));
				Object primaryKeyNew2 = foreignKey2.getKeyForEntity(serverMetaData.getMapForDataRow(pNewDataRow));
				
				Object entity1 = jpaAccess.findById(primaryKey1, masterEntity);
				
				Collection objectList = (Collection)foreignKey1.getDetailEntities(entity1);
				
				Object entityOld2 = jpaAccess.findById(primaryKeyOld2, detailEntity);
				
				objectList.remove(entityOld2);
				
				Object entityNew2 = jpaAccess.findById(primaryKeyNew2, detailEntity);
				
				objectList.add(entityNew2);
				
				jpaAccess.update(entity1, masterEntity);
				
				dataRow = getDataRowForEntities(entity1, entityNew2);
			}
			else
			{
				Object primaryKey = serverMetaData.getJPAPrimaryKey().getKeyForEntity(serverMetaData.getMapForDataRow(pOldDataRow));
				
				Object entityForUpdate = jpaAccess.findById(primaryKey, masterEntity);
				
				mapDataRowToEntity(pNewDataRow, entityForUpdate);
				
				jpaAccess.update(entityForUpdate, masterEntity);
				
				dataRow = getDataRowForEntity(entityForUpdate);
			}
		}
		catch (DataSourceException dse)
		{
			throw dse;
		}
		catch (Exception e)
		{
			throw new DataSourceException("Update was not possible", e);
		}
		
		return dataRow;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getEstimatedRowCount(ICondition pFilter) throws DataSourceException
	{
		if (!isOpen())
		{
			throw new DataSourceException("JPAStorage isn't open!");
		}
		
		CriteriaQuery<Long> countCriteriaQuery = criteriaConditionMapper.getCountCriteriaQuery(pFilter, masterEntity, null);
		
		return jpaAccess.countByCriteria(countCriteriaQuery).intValue();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public MetaData getMetaData() throws DataSourceException
	{
		if (!isOpen())
		{
			throw new DataSourceException("JPAStorage isn't open!");
		}
		
		return serverMetaData.getMetaData();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeCSV(OutputStream pStream, String[] pColumnNames,
			String[] pLabels, ICondition pFilter, SortDefinition pSort,
			String pSeparator) throws Exception
	{
		ISession session = SessionContext.getCurrentSession();
		
		String sEncoding = null;
		
		if (session != null)
		{
			sEncoding = (String)session.getProperty(IConnectionConstants.PREFIX_CLIENT + IConnectionConstants.PREFIX_SYSPROP + "file.encoding");
			
			if (sEncoding == null)
			{
				sEncoding = (String)session.getProperty(IConnectionConstants.PREFIX_CLIENT + "defaultCharset");
			}
		}
		
		if (StringUtil.isEmpty(sEncoding))
		{
			sEncoding = DataBookCSVExporter.getDefaultEncoding();
		}
		
		OutputStreamWriter out = new OutputStreamWriter(pStream, sEncoding);
		
		try
		{
			List<Object[]> lResult = executeFetch(pFilter, pSort, 0, 0);
			
			if (pColumnNames == null)
			{
				pColumnNames = serverMetaData.getColumnNames();
			}
			
			if (pLabels == null)
			{
				pLabels = new String[pColumnNames.length];
				
				for (int i = 0; i < pColumnNames.length; i++)
				{
					pLabels[i] = serverMetaData.getServerColumnMetaData(pColumnNames[i]).getLabel();
					
					if (pLabels[i] == null)
					{
						pLabels[i] = ColumnMetaData.getDefaultLabel(pColumnNames[i]);
					}
				}
			}
			
			// write column headers with the defined label
			for (int i = 0; i < pLabels.length; i++)
			{
				if (i > 0)
				{
					out.write(pSeparator);
				}
				out.write(StringUtil.quote(pLabels[i], '"'));
			}
			out.write("\n");
			
			//cache the column datatypes
			IDataType[] dataTypes = new IDataType[pColumnNames.length];
			
			for (int i = 0, anz = pColumnNames.length; i < anz; i++)
			{
				dataTypes[i] = serverMetaData.getServerColumnMetaData(pColumnNames[i]).getColumnMetaData().getDataType();
			}
			
			IBean bnRowData;
			
			//write rows (last row
			for (int i = 0; i < lResult.size() - 1; i++)
			{
				bnRowData = createBean(lResult.get(i));
				
				for (int j = 0; j < pColumnNames.length; j++)
				{
					if (j > 0)
					{
						out.write(pSeparator);
					}
					
					DataBookCSVExporter.writeQuoted(out, dataTypes[j], bnRowData.get(pColumnNames[j]), pSeparator);
				}
				out.write("\n");
			}
			
			out.flush();
		}
		finally
		{
			CommonUtil.close(out);
		}
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Overwritten methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return name;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Sets the master entity class for the JPAStorage.
	 * 
	 * @param pMasterEntity the master entity class.
	 * @throws DataSourceException if entity check fails.
	 * @throws IllegalArgumentException if the given master entity is
	 *             {@code null}.
	 */
	public void setMasterEntity(Class pMasterEntity) throws DataSourceException
	{
		if (pMasterEntity == null)
		{
			throw new IllegalArgumentException("Master entity can not be null!");
		}
		
		checkEntity(pMasterEntity);
		
		masterEntity = pMasterEntity;
		
		updateName();
	}
	
	/**
	 * Returns the master entity class of the JPAStorage.
	 * 
	 * @return the master entity class.
	 */
	public Class getMasterEntity()
	{
		return masterEntity;
	}
	
	/**
	 * Sets the Detail Entity-Class for the JPAStorage. This is needed for a
	 * Many-to-Many Relationship between entities.
	 * 
	 * @param pDetailEntity the detail Entity Class.
	 * @throws DataSourceException if check entity fails.
	 */
	public void setDetailEntity(Class pDetailEntity) throws DataSourceException
	{
		checkEntity(pDetailEntity);
		
		detailEntity = pDetailEntity;
		
		updateName();
	}
	
	/**
	 * Returns the detail entity class from the JPAStorage.
	 * 
	 * @return the detail entity class
	 */
	public Class getDetailEntity()
	{
		return detailEntity;
	}
	
	/**
	 * Creates a {@link JPAAccess} with the given {@link EntityManager} for this
	 * JPAStorage. There have to be one JPAAccess per JPAStorage.
	 * 
	 * @param pEntityManager The EntityManager.
	 * @throws DataSourceException If the EntityManager is not open or null.
	 */
	public void setEntityManager(EntityManager pEntityManager) throws DataSourceException
	{
		
		if (pEntityManager == null)
		{
			throw new DataSourceException("EntityManager is null");
		}
		
		if (pEntityManager.isOpen())
		{
			if (jpaAccessUser != null)
			{
				jpaAccessUser.setEntityManager(pEntityManager);
			}
			else
			{
				JPAAccess jpa = new JPAAccess();
				jpa.setEntityManager(pEntityManager);
				
				setJPAAccessIntern(jpa);
			}
		}
		else
		{
			throw new DataSourceException("EntityManager is not open");
		}
	}
	
	/**
	 * Returns the JPAAccess for the storage.
	 * 
	 * @return The JPAAcces.
	 */
	public JPAAccess getJPAAccess()
	{
		return jpaAccess;
	}
	
	/**
	 * Sets the user-defined JPA access for the storage.
	 * 
	 * @param pJPAAccess The JPA access.
	 */
	public void setJPAAccess(JPAAccess pJPAAccess)
	{
		jpaAccessUser = pJPAAccess;
		
		//set the entity manager from the internal JPAAccess to the user-defined JPAAccess
		if (jpaAccessUser != null && jpaAccess != null)
		{
			jpaAccessUser.setEntityManager(jpaAccess.getEntityManager());
		}
		
		setJPAAccessIntern(pJPAAccess);
	}
	
	/**
	 * Gets all known sub storages as key / value pair.
	 * 
	 * @return the key / value pair with subtablename and substorage or {@code
	 *         null} if no substorages are known.
	 */
	public Map<String, IStorage> getSubStorages()
	{
		return subStorages;
	}
	
	/**
	 * Opens the JPAStorage and checks if the JPA access is set.
	 * 
	 * @throws DataSourceException if the JPA access is null.
	 */
	public void open() throws DataSourceException
	{
		openInternal(false);
	}
	
	/**
	 * Opens the JPA Storage and checks if the JPA access is set.
	 * 
	 * @param pUseRepresentationColumnsAsQueryColumns {@code true} if the
	 *            QueryColumns are set with all representation columns including
	 *            the Primary Key columns.
	 * @throws DataSourceException if the JPA access is null.
	 */
	public void openInternal(boolean pUseRepresentationColumnsAsQueryColumns) throws DataSourceException
	{
		if (jpaAccess == null)
		{
			throw new DataSourceException("EntityManager is not set. Call setEntityManager first.");
		}
		
		serverMetaData = createServerMetaData(pUseRepresentationColumnsAsQueryColumns);
		criteriaConditionMapper = new ConditionCriteriaMapper(serverMetaData, jpaAccess.getCriteriaBuilder());
		
		open = true;
	}
	
	/**
	 * Returns if the JPA Storage is open.
	 * 
	 * @return true if the JPA Storage is open.
	 */
	public boolean isOpen()
	{
		return open;
	}
	
	/**
	 * Sets if the automatic link reference detection is en- or disabled. The
	 * automatic link reference is defined through a foreign key reference in
	 * the entity.
	 *
	 * @param pAutoLinkReference true if the automatic link reference mode is
	 *            on, <code>false</code> to disable auto link reference mode.
	 */
	public void setAutoLinkReference(boolean pAutoLinkReference)
	{
		autoLinkReference = Boolean.valueOf(pAutoLinkReference);
	}
	
	/**
	 * Returns if the automatic link reference mode is on or off.
	 *
	 * @return <code>true</code>if the automatic link reference mode is on,
	 *         otherwise <code>false</code>.
	 * @see #setAutoLinkReference(boolean)
	 */
	public boolean isAutoLinkReference()
	{
		if (autoLinkReference == null)
		{
			return defaultAutoLinkReference;
		}
		
		return autoLinkReference.booleanValue();
	}
	
	/**
	 * Creates and sets a new {@link StorageReferenceDefinition} for the Columns
	 * of the {@link JPAForeignKey} e.g. its used to make an automatic linked
	 * celleditor from a custom written view and set it on all Columns of the
	 * {@link JPAForeignKey}.
	 * 
	 * @param pJPAForeignKey The JPAForeignKey.
	 * @param pMasterEntity The Entity for the AutoLinkReference.
	 * @throws DataSourceException if creating the link reference did not
	 *             succeed.
	 */
	public void createAutomaticLinkReference(JPAForeignKey pJPAForeignKey, Class pMasterEntity) throws DataSourceException
	{
		createAutomaticLinkReference(pJPAForeignKey, createAutomaticLinkStorage(pMasterEntity));
	}
	
	/**
	 * Gets the {@link JPAServerMetaData JPA server meta data}.
	 *
	 * @return the {@link JPAServerMetaData JPA server meta data}.
	 */
	JPAServerMetaData getJPAServerMetaData()
	{
		return serverMetaData;
	}
	
	/**
	 * Creates and sets a new {@link StorageReferenceDefinition} with the
	 * specified {@link JPAStorage} for the {@link JPAForeignKey} Columns.
	 * 
	 * @param pJPAForeignKey The JPAForeignKey.
	 * @param pJPAStorage The Storage Object.
	 */
	protected void createAutomaticLinkReference(JPAForeignKey pJPAForeignKey, AbstractStorage pJPAStorage)
	{
		String sStorageName = pJPAStorage.getName();
		
		putSubStorage(sStorageName, pJPAStorage);
		
		// The storage has to be referenced by the getSubStorages method from outside.
		StorageReferenceDefinition srdLink = new StorageReferenceDefinition(pJPAForeignKey.getColumnNames(), ".subStorages." + sStorageName,
				pJPAForeignKey.getReferencedColumnNames());
				
		for (JPAServerColumnMetaData serverColumnMetaData : pJPAForeignKey.getServerColumnMetaDataAsCollection())
		{
			serverColumnMetaData.setLinkReference(srdLink);
		}
	}
	
	/**
	 * Creates a new {@link JPAStorage} which is configured for automatic link
	 * cell editors. The auto link reference feature is disabled for this
	 * storage.
	 * 
	 * @param pMasterEntity The Entity for the Storage.
	 * @return the newly created storage.
	 * @throws DataSourceException if the from clause causes errors or the
	 *             metadata are not available.
	 */
	protected JPAStorage createAutomaticLinkStorage(Class pMasterEntity) throws DataSourceException
	{
		JPAStorage jpaStorage = new JPAStorage(pMasterEntity);
		jpaStorage.setEntityManager((getJPAAccess().getEntityManager()));
		jpaStorage.setAutoLinkReference(false);
		jpaStorage.openInternal(true);
		
		return jpaStorage;
	}
	
	/**
	 * Returns the DataRow for a ManyToMany relation between to Entities.
	 * 
	 * @param pEntity1 the first entity.
	 * @param pEntity2 the second entity.
	 * @return the DataRow.
	 * @throws DataSourceException if the storage is not open or one of the
	 *             entities could not be mapped.
	 */
	protected Object[] getDataRowForEntities(Object pEntity1, Object pEntity2) throws DataSourceException
	{
		if (!isOpen())
		{
			throw new DataSourceException("JPAStorage isn't open!");
		}
		
		try
		{
			ArrayList dataRow = new ArrayList();
			
			JPAForeignKey jpaForeignKey1 = serverMetaData.getJPAPrimaryKey().getForeignKey(pEntity1.getClass());
			
			Object primaryKey1 = jpaAccess.getIdentifier(pEntity1);
			
			for (JPAServerColumnMetaData serverColumnMetaData : jpaForeignKey1.getServerColumnMetaDataAsArray())
			{
				if (serverColumnMetaData.isKeyAttribute())
				{
					if (JPAStorageUtil.isPrimitiveOrWrapped(primaryKey1.getClass()))
					{
						dataRow.add(serverColumnMetaData.getJPAMappingType().getValue(pEntity1));
					}
					else
					{
						dataRow.add(serverColumnMetaData.getJPAMappingType().getValue(primaryKey1));
					}
					
				}
				else
				{
					dataRow.add(serverColumnMetaData.getJPAMappingType().getValue(pEntity1));
				}
			}
			
			JPAForeignKey jpaForeignKey2 = serverMetaData.getJPAPrimaryKey().getForeignKey(pEntity2.getClass());
			
			Object primaryKey2 = jpaAccess.getIdentifier(pEntity2);
			
			for (JPAServerColumnMetaData serverColumnMetaData : jpaForeignKey2.getServerColumnMetaDataAsArray())
			{
				if (serverColumnMetaData.isKeyAttribute())
				{
					if (JPAStorageUtil.isPrimitiveOrWrapped(primaryKey2.getClass()))
					{
						dataRow.add(primaryKey2);
					}
					else
					{
						dataRow.add(serverColumnMetaData.getJPAMappingType().getValue(primaryKey2));
					}
				}
				else
				{
					dataRow.add(serverColumnMetaData.getJPAMappingType().getValue(pEntity2));
				}
			}
			
			return dataRow.toArray();
		}
		catch (Exception e)
		{
			throw new DataSourceException("Problems by mapping entities to a DataRow", e);
		}
	}
	
	/**
	 * Returns the DataRow for the given entity-object.
	 * 
	 * @param pEntity the entity for which to get the data row.
	 * @return the DataRow.
	 * @throws DataSourceException if the storage is not open or the entity
	 *             could not be mapped.
	 */
	protected Object[] getDataRowForEntity(Object pEntity) throws DataSourceException
	{
		if (!isOpen())
		{
			throw new DataSourceException("JPAStorage isn't open!");
		}
		
		try
		{
			Object[] dataRow = new Object[serverMetaData.getMetaData().getColumnMetaData().length];
			
			Object primaryKey = jpaAccess.getIdentifier(pEntity);
			
			// PrimaryKey Columns
			for (JPAServerColumnMetaData serverColumnMetaData : serverMetaData.getJPAPrimaryKey().getServerColumnMetaDataAsArray())
			{
				int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());
				
				dataRow[index] = serverColumnMetaData.getJPAMappingType().getValue(primaryKey);
			}
			
			// Columns from the Entity
			for (JPAServerColumnMetaData serverColumnMetaData : serverMetaData.getServerColumnMetaData())
			{
				int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());
				
				dataRow[index] = serverColumnMetaData.getJPAMappingType().getValue(pEntity);
			}
			
			// Embedded Columns
			for (JPAEmbeddedKey jpaEmbeddedKey : serverMetaData.getJPAEmbeddedKeys())
			{
				Object entityInEntity = jpaEmbeddedKey.getJPAMappingType().getValue(pEntity);
				
				if (entityInEntity != null)
				{
					for (JPAServerColumnMetaData serverColumnMetaData : jpaEmbeddedKey.getServerColumnMetaDataAsArray())
					{
						int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());
						
						dataRow[index] = serverColumnMetaData.getJPAMappingType().getValue(entityInEntity);
					}
				}
			}
			
			// ForeignKey Columns
			for (JPAForeignKey foreignKey : serverMetaData.getJPAForeignKeys())
			{
				Object entityInEntity = foreignKey.getJPAMappingType().getValue(pEntity);
				
				if (entityInEntity != null)
				{
					primaryKey = jpaAccess.getIdentifier(entityInEntity);
					
					for (JPAServerColumnMetaData serverColumnMetaData : foreignKey.getServerColumnMetaDataAsArray())
					{
						int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());
						
						if (serverColumnMetaData.isKeyAttribute())
						{
							dataRow[index] = serverColumnMetaData.getJPAMappingType().getValue(primaryKey);
						}
						else
						{
							dataRow[index] = serverColumnMetaData.getJPAMappingType().getValue(entityInEntity);
						}
					}
				}
			}
			
			return dataRow;
		}
		catch (Exception e)
		{
			throw new DataSourceException("Problems by mapping an entity to a DataRow", e);
		}
	}
	
	/**
	 * Writes all values from the DataRow to the entity-object.
	 * 
	 * @param pDataRow the DataRow;
	 * @param pEntity the entity-object.
	 * @throws DataSourceException if the storage is not open or the data row
	 *             could not be mapped to the entity.
	 */
	protected void mapDataRowToEntity(Object[] pDataRow, Object pEntity) throws DataSourceException
	{
		if (!isOpen())
		{
			throw new DataSourceException("JPAStorage isn't open!");
		}
		
		try
		{
			// PrimaryKey Columns
			for (JPAServerColumnMetaData serverColumnMetaData : serverMetaData.getJPAPrimaryKey().getServerColumnMetaDataAsArray())
			{
				int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());
				
				if (serverMetaData.getJPAPrimaryKey().isEmbedded())
				{
					Object primaryKey = serverMetaData.getJPAPrimaryKey().getKeyForEntity(serverMetaData.getMapForDataRow(pDataRow));
					
					serverMetaData.getJPAPrimaryKey().getJPAMappingType().setValue(pEntity, primaryKey);
					
					break;
				}
				else
				{
					serverColumnMetaData.getJPAMappingType().setValue(pEntity, pDataRow[index]);
				}
			}
			
			// Columns from the Entity
			for (JPAServerColumnMetaData serverColumnMetaData : serverMetaData.getServerColumnMetaData())
			{
				int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());
				
				serverColumnMetaData.getJPAMappingType().setValue(pEntity, pDataRow[index]);
			}
			
			// Embedded Columns
			for (JPAEmbeddedKey jpaEmbeddedKey : serverMetaData.getJPAEmbeddedKeys())
			{
				Object embeddedEntity = jpaEmbeddedKey.getJPAMappingType().getValue(pEntity);
				
				if (embeddedEntity == null)
				{
					// If the embeddedEntity is null, it has to be instantiated
					
					embeddedEntity = jpaEmbeddedKey.getJPAMappingType().getJavaTypeClass().newInstance();
				}
				
				for (JPAServerColumnMetaData serverColumnMetaData : jpaEmbeddedKey.getServerColumnMetaDataAsArray())
				{
					int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());
					
					serverColumnMetaData.getJPAMappingType().setValue(embeddedEntity, pDataRow[index]);
				}
				
				jpaEmbeddedKey.getJPAMappingType().setValue(pEntity, embeddedEntity);
			}
			
			// ForeignKey Columns
			for (JPAForeignKey foreignKey : serverMetaData.getJPAForeignKeys())
			{
				Object primaryKey = foreignKey.getKeyForEntity(serverMetaData.getMapForDataRow(pDataRow));
				
				if (primaryKey != null)
				{
					Object entityInEntity = jpaAccess.findById(primaryKey, foreignKey.getJPAMappingType().getJavaTypeClass());
					
					if (entityInEntity != null)
					{
						foreignKey.getJPAMappingType().setValue(pEntity, entityInEntity);
					}
				}
			}
		}
		catch (Exception e)
		{
			throw new DataSourceException("Problems by mapping a DataRow to an entity", e);
		}
	}
	
	/**
	 * Checks if the given Class is an entity.
	 * 
	 * @param pEntityClass the entity class.
	 * @throws DataSourceException of the given Class is no entity.
	 */
	private void checkEntity(Class pEntityClass) throws DataSourceException
	{
		try
		{
			Annotation[] annotations = pEntityClass.getAnnotations();
			
			for (Annotation annotation : annotations)
			{
				if (annotation.annotationType() == javax.persistence.Entity.class)
				{
					return;
				}
			}
		}
		catch (Exception e)
		{
			//nothing to be done
		}
		
		throw new DataSourceException("Class " + pEntityClass.getName() + " is no Entity");
	}
	
	/**
	 * Creates the {@link JPAForeignKey} for the given entity class.
	 * 
	 * @param pEntityClass The Class of the entity
	 * @param pReferencingColumnName the name of the referencing column.
	 * @return The {@link JPAForeignKey} for the given entity class
	 * @throws Exception if creating the foreign key failed.
	 */
	private JPAForeignKey createForeignKey(Class pEntityClass, String pReferencingColumnName) throws Exception
	{
		EntityType entityType = jpaAccess.getEntityType(pEntityClass);
		
		JPAForeignKey jpaForeignKey = new JPAForeignKey();
		jpaForeignKey.setSingleIdAttribute(entityType.hasSingleIdAttribute());
		
		JPAPrimaryKey jpaPrimaryKey = createPrimaryKey(pEntityClass);
		
		Attribute bestAttributeForStorageReference = getBestAttributeForStorageReference(pEntityClass);
		
		if (isAutoLinkReference() && bestAttributeForStorageReference != null)
		{
			JPAServerColumnMetaData serverColumnMetaDataForStorageReference = getServerColumnMetaData(bestAttributeForStorageReference,
					bestAttributeForStorageReference.getDeclaringType().getJavaType());
			serverColumnMetaDataForStorageReference.setStorageReference(true);
			
			jpaPrimaryKey.addServerColumnMetaData(serverColumnMetaDataForStorageReference);
			
		}
		
		jpaForeignKey.setReferencedColumnNames(jpaPrimaryKey.getColumnNames());
		
		jpaForeignKey.setKeyClass(entityType.getIdType().getJavaType());
		
		for (JPAServerColumnMetaData serverColumnMetaData : jpaPrimaryKey.getServerColumnMetaDataAsArray())
		{
			if (pReferencingColumnName != null)
			{
				// If there is a referencing column name provided, we will
				// use a different naming scheme to make sure that foreign key
				// columns can not collide.
				
				String referencedEntityName = serverColumnMetaData.getJPAMappingType().getEntityClass().getSimpleName().toUpperCase();
				String columnName = serverColumnMetaData.getName();
				
				// If the column does start with the name of the table, we will
				// strip the table name. This is to avoid having two identical
				// prefixes like "LOCATION_NAME_LOCATION_NAME".
				if (columnName.startsWith(referencedEntityName + "_"))
				{
					columnName = columnName.substring(referencedEntityName.length() + 1);
				}
				
				// If the referencing column does end with the name we just
				// created, we will simply use the source column name.
				// This is simply to avoid two identical postfixes, like
				// "LOCATION_NAME_NAME".
				// Otherwise we will append our generated name.
				if (pReferencingColumnName.endsWith(columnName))
				{
					columnName = pReferencingColumnName;
				}
				else
				{
					columnName = pReferencingColumnName + "_" + columnName;
				}
				
				serverColumnMetaData.setName(columnName);
			}
			else
			{
				String foreignKeyName = entityType.getName().toUpperCase() + "_" + serverColumnMetaData.getName();
				
				serverColumnMetaData.setName(foreignKeyName);
			}
			
			jpaForeignKey.addServerColumnMetaData(serverColumnMetaData);
		}
		
		if (isAutoLinkReference() && bestAttributeForStorageReference != null)
		{
			createAutomaticLinkReference(jpaForeignKey, pEntityClass);
		}
		
		return jpaForeignKey;
	}
	
	/**
	 * Creates the {@link JPAPrimaryKey} for the given entity class.
	 * 
	 * @param pEntityClass The Class for generating the JPAPrimaryKey
	 * @return the {@link JPAPrimaryKey}
	 * @throws Exception if creating the primary key failed.
	 */
	private JPAPrimaryKey createPrimaryKey(Class pEntityClass) throws Exception
	{
		EntityType entityType = jpaAccess.getEntityType(pEntityClass);
		
		JPAPrimaryKey jpaPrimaryKey = new JPAPrimaryKey();
		JPAMappingType jpaDataType = new JPAMappingType();
		
		jpaPrimaryKey.setSingleIdAttribute(entityType.hasSingleIdAttribute());
		
		if (entityType.hasSingleIdAttribute())
		{
			if (JPAStorageUtil.isPrimitiveOrWrapped(entityType.getIdType().getJavaType()))
			{
				Class idClass = entityType.getIdType().getJavaType();
				
				jpaDataType.setEntityClass(pEntityClass);
				jpaDataType.setJavaTypeClass(idClass);
				jpaDataType.setDataType(JPAStorageUtil.getDataTypeIdentifierForJavaType(idClass));
				
				for (Attribute attribute : jpaAccess.getAttributes(pEntityClass))
				{
					if (JPAStorageUtil.isPrimaryKeyAttribute(attribute))
					{
						jpaDataType.setGetterMethodName(JPAStorageUtil.getGetterMethodNameForAttribute(attribute));
						jpaDataType.setSetterMethodName(JPAStorageUtil.getSetterMethodNameForAttribute(attribute));
						
						JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attribute, pEntityClass);
						serverColumnMetaData.setKeyAttribute(true);
						
						jpaPrimaryKey.addServerColumnMetaData(serverColumnMetaData);
					}
				}
			}
			else
			{
				//EmbeddedId Key Class
				
				Class idClass = entityType.getIdType().getJavaType();
				
				jpaDataType.setEntityClass(pEntityClass);
				jpaDataType.setJavaTypeClass(idClass);
				jpaDataType.setDataType(JPAStorageUtil.getDataTypeIdentifierForJavaType(idClass));
				
				Attribute attribute = entityType.getId(idClass);
				
				jpaDataType.setGetterMethodName(JPAStorageUtil.getGetterMethodNameForAttribute(attribute));
				jpaDataType.setSetterMethodName(JPAStorageUtil.getSetterMethodNameForAttribute(attribute));
				
				EmbeddableType embeddableType = jpaAccess.getEmbeddableType(entityType.getIdType().getJavaType());
				
				Set<Attribute> setAttribute = embeddableType.getAttributes();
				
				for (Attribute attributeEmbedded : setAttribute)
				{
					JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attributeEmbedded, idClass);
					serverColumnMetaData.getJPAMappingType().addPathNavigation(attribute.getName());
					serverColumnMetaData.setKeyAttribute(true);
					
					jpaPrimaryKey.addServerColumnMetaData(serverColumnMetaData);
				}
				
				jpaPrimaryKey.setEmbedded(true);
			}
		}
		else
		{
			Class idClass = entityType.getIdType().getJavaType();
			
			jpaDataType.setEntityClass(pEntityClass);
			jpaDataType.setJavaTypeClass(idClass);
			jpaDataType.setDataType(JPAStorageUtil.getDataTypeIdentifierForJavaType(idClass));
			
			Set<Attribute> setAttribute = entityType.getIdClassAttributes();
			
			for (Attribute attribute : setAttribute)
			{
				JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attribute, pEntityClass);
				serverColumnMetaData.setKeyAttribute(true);
				
				jpaPrimaryKey.addServerColumnMetaData(serverColumnMetaData);
			}
		}
		
		jpaPrimaryKey.setJPAMappingType(jpaDataType);
		
		return jpaPrimaryKey;
	}
	
	/**
	 * Creates the {@link JPAServerMetaData} for this JPAStorage.
	 * 
	 * @param pUseRepresentationColumns If the representation Column Names
	 *            should be used
	 * @return The {@link JPAServerMetaData} for this JPAStorage
	 * @throws DataSourceException if create server metadata fails
	 */
	private JPAServerMetaData createServerMetaData(boolean pUseRepresentationColumns) throws DataSourceException
	{
		JPAServerMetaData smdNew = new JPAServerMetaData();
		
		try
		{
			// Is a Many-to-Many Relation
			if (detailEntity != null)
			{
				JPAPrimaryKey jpaPrimaryKey = new JPAPrimaryKey();
				
				JPAForeignKey jpaForeignKey = createForeignKey(masterEntity, null);
				
				JPAMappingType jpaDataType = new JPAMappingType();
				
				// By a ManyToMany Relation there exists no Entity Class
				jpaDataType.setEntityClass(null);
				jpaDataType.setJavaTypeClass(masterEntity);
				
				jpaForeignKey.setJPAMappingType(jpaDataType);
				
				Attribute detailRelationAttribute = getDetailRelationAttribute(masterEntity, detailEntity, PersistentAttributeType.MANY_TO_MANY);
				
				if (detailRelationAttribute != null)
				{
					jpaForeignKey.setDetailEntitiesMethode(JPAStorageUtil.getGetterMethodNameForAttribute(detailRelationAttribute));
				}
				
				smdNew.addJPAForeignKey(jpaForeignKey);
				
				jpaPrimaryKey.addForeignKey(masterEntity, jpaForeignKey);
				
				jpaForeignKey = createForeignKey(detailEntity, null);
				
				jpaDataType = new JPAMappingType();
				
				// By a ManyToMany Relation there exists no Entity Class
				jpaDataType.setEntityClass(null);
				jpaDataType.setJavaTypeClass(detailEntity);
				
				jpaForeignKey.setJPAMappingType(jpaDataType);
				
				smdNew.addJPAForeignKey(jpaForeignKey);
				
				jpaPrimaryKey.addForeignKey(detailEntity, jpaForeignKey);
				
				smdNew.setManyToMany(true);
				smdNew.setJPAPrimaryKey(jpaPrimaryKey);
			}
			else
			{
				JPAPrimaryKey jpaPrimaryKey = createPrimaryKey(masterEntity);
				
				smdNew.setJPAPrimaryKey(jpaPrimaryKey);
				
				ArrayList<String> uniqueKeyColumnNames = getUniqueKeyColumnNames(masterEntity);
				
				Set<Attribute> attributes = jpaAccess.getAttributes(masterEntity);
				
				for (Attribute attribute : attributes)
				{
					// PrimaryKey Attributes already initialized
					if (!JPAStorageUtil.isPrimaryKeyAttribute(attribute))
					{
						if (attribute.getPersistentAttributeType() == PersistentAttributeType.BASIC)
						{
							if (pUseRepresentationColumns && uniqueKeyColumnNames.size() > 0)
							{
								if (uniqueKeyColumnNames.contains(JPAStorageUtil.getNameForAttribute(attribute)))
								{
									JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attribute, masterEntity);
									
									smdNew.addServerColumnMetaData(serverColumnMetaData);
								}
							}
							else
							{
								JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attribute, masterEntity);
								
								smdNew.addServerColumnMetaData(serverColumnMetaData);
							}
						}
						else if (!pUseRepresentationColumns
								&& (attribute.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_ONE
										|| attribute.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE))
						{
							JPAForeignKey jpaForeignKey = createForeignKey(attribute.getJavaType(), attribute.getName().toUpperCase());
							
							Attribute detailRelationAttribute = getDetailRelationAttribute(attribute.getJavaType(), masterEntity, PersistentAttributeType.ONE_TO_MANY);
							
							if (detailRelationAttribute != null)
							{
								jpaForeignKey.setDetailEntitiesMethode(JPAStorageUtil.getGetterMethodNameForAttribute(detailRelationAttribute));
							}
							
							JPAMappingType jpaDataType = new JPAMappingType();
							jpaDataType.setEntityClass(attribute.getDeclaringType().getJavaType());
							jpaDataType.setJavaTypeClass(attribute.getJavaType());
							jpaDataType.setDataType(JPAStorageUtil.getDataTypeIdentifierForJavaType(attribute.getJavaType()));
							jpaDataType.setGetterMethodName(JPAStorageUtil.getGetterMethodNameForAttribute(attribute));
							jpaDataType.setSetterMethodName(JPAStorageUtil.getSetterMethodNameForAttribute(attribute));
							
							jpaForeignKey.setJPAMappingType(jpaDataType);
							
							for (JPAServerColumnMetaData serverColumnMetaData : jpaForeignKey.getServerColumnMetaDataAsCollection())
							{
								serverColumnMetaData.getJPAMappingType().addPathNavigation(attribute.getName());
							}
							
							smdNew.addJPAForeignKey(jpaForeignKey);
						}
						else if (attribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED)
						{
							JPAEmbeddedKey jpaKey = new JPAEmbeddedKey();
							
							EmbeddableType embeddableType = jpaAccess.getEmbeddableType(attribute.getJavaType());
							
							Set<Attribute> setAttribute = embeddableType.getAttributes();
							
							//an expensive method!
							String sAttribName = JPAStorageUtil.getNameForAttribute(attribute);
							
							for (Attribute attributeEmbedded : setAttribute)
							{
								if (pUseRepresentationColumns && uniqueKeyColumnNames.size() > 0)
								{
									if (uniqueKeyColumnNames.contains(sAttribName))
									{
										JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attributeEmbedded, attribute.getJavaType());
										jpaKey.addServerColumnMetaData(serverColumnMetaData);
									}
								}
								else
								{
									JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attributeEmbedded, attribute.getJavaType());
									jpaKey.addServerColumnMetaData(serverColumnMetaData);
								}
							}
							
							JPAMappingType jpaDataType = new JPAMappingType();
							jpaDataType.setEntityClass(attribute.getDeclaringType().getJavaType());
							jpaDataType.setJavaTypeClass(attribute.getJavaType());
							jpaDataType.setDataType(JPAStorageUtil.getDataTypeIdentifierForJavaType(attribute.getJavaType()));
							jpaDataType.setGetterMethodName(JPAStorageUtil.getGetterMethodNameForAttribute(attribute));
							jpaDataType.setSetterMethodName(JPAStorageUtil.getSetterMethodNameForAttribute(attribute));
							
							jpaKey.setJPAMappingType(jpaDataType);
							
							for (JPAServerColumnMetaData serverColumnMetaData : jpaKey.getServerColumnMetaDataAsCollection())
							{
								serverColumnMetaData.getJPAMappingType().addPathNavigation(attribute.getName());
							}
							
							smdNew.addJPAEmbeddedKey(jpaKey);
						}
					}
				}
				
				if (uniqueKeyColumnNames.size() > 0)
				{
					// All Unique Keys are Representation Column Names				
					smdNew.setRepresentationColumnNames(uniqueKeyColumnNames.toArray(new String[0]));
				}
				else
				{
					// If there are no Unique Keys then all ColumnNames are Representation ColumnNames
					smdNew.setRepresentationColumnNames(smdNew.getColumnNames());
				}
			}
		}
		catch (Exception e)
		{
			throw new DataSourceException("Create metadata failed!", e);
		}
		
		return smdNew;
	}
	
	/**
	 * Returns the best Attribute for the automatic link reference.
	 * 
	 * @param pEntityClass The Class to search for the best attribute
	 * @return The Attribute for the automatic link reference
	 * @throws Exception if getting the best attribute failed.
	 */
	private Attribute getBestAttributeForStorageReference(Class pEntityClass) throws Exception
	{
		ArrayList<String> uniqueKeys = getUniqueKeyColumnNames(pEntityClass);
		
		Set<Attribute> attributes = jpaAccess.getAttributes(pEntityClass);
		
		for (Attribute attribute : attributes)
		{
			if (attribute.getJavaType() == java.lang.String.class)
			{
				if (uniqueKeys.size() > 0)
				{
					if (uniqueKeys.contains(JPAStorageUtil.getNameForAttribute(attribute)))
					{
						return attribute;
					}
				}
				else
				{
					return attribute;
				}
			}
		}
		
		for (Attribute attribute : attributes)
		{
			if (!JPAStorageUtil.isPrimaryKeyAttribute(attribute))
			{
				if (uniqueKeys.size() > 0)
				{
					if (uniqueKeys.contains(JPAStorageUtil.getNameForAttribute(attribute)))
					{
						return attribute;
					}
				}
				else
				{
					return attribute;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the attribute which is the Detail of a Master Entity.
	 * 
	 * Example:
	 * 
	 * @Entity public class Customer { ...
	 * @OnToMany Collection&lt;Address&gt; addresses = new
	 *           Collection&lt;Address&gt; ... }
	 * 			
	 * @Entity public class Address { ... String street; .... }
	 * 		
	 *         For this example the returned Attribute is "addresses"
	 * 		
	 * @param pMasterEntity the Master Entity (in the example "Customer")
	 * @param pDetailEntity the Detail Entity (in the example "Address")
	 * @param pPersistentAttributeType the PersistentAttributeType (OneToMany or
	 *            ManyToMany)
	 * @return The Attribute for the detail Relation
	 * @throws Exception if getting the detail relation attribute failed.
	 */
	private Attribute getDetailRelationAttribute(Class pMasterEntity, Class pDetailEntity, PersistentAttributeType pPersistentAttributeType) throws Exception
	{
		Set<Attribute> targetAttributes = jpaAccess.getAttributes(pMasterEntity);
		
		for (Attribute targetAttribute : targetAttributes)
		{
			if (targetAttribute.getPersistentAttributeType() == pPersistentAttributeType)
			{
				if (targetAttribute.isCollection())
				{
					Annotation[] annotations = JPAStorageUtil.getAnnotationsForAttribute(targetAttribute);
					
					for (Annotation annotation : annotations)
					{
						if (annotation.annotationType() == javax.persistence.ManyToMany.class
								|| annotation.annotationType() == javax.persistence.OneToMany.class)
						{
							Class typeClass = JPAStorageUtil.getTypeClassForAttribute(targetAttribute);
							
							if (typeClass == null)
							{
								typeClass = (Class)annotation.getClass().getMethod("targetEntity").invoke(annotation);
							}
							
							if (typeClass == pDetailEntity)
							{
								return targetAttribute;
							}
							
						}
						
					}
					
				}
				
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the UniqueKey Column Names for the given entity.
	 * 
	 * @param pEntityClass The Class of the entity
	 * @return A List with the UniqueKey Column Names
	 * @throws Exception if getting the unique key columns failed.
	 */
	private ArrayList<String> getUniqueKeyColumnNames(Class pEntityClass) throws Exception
	{
		ArrayList<String> uniqueColumnNames = new ArrayList<String>();
		
		Set<Attribute> attributes = jpaAccess.getAttributes(pEntityClass);
		
		for (Attribute attribute : attributes)
		{
			// Unique Keys can be defined in JPA 2.0 in the Annotation Column from a field
			Annotation[] annotations = JPAStorageUtil.getAnnotationsForAttribute(attribute);
			
			//an expensive method!
			String sAttribName = JPAStorageUtil.getNameForAttribute(attribute, annotations);
			
			for (Annotation annotation : annotations)
			{
				if (annotation.annotationType() == javax.persistence.Column.class)
				{
					if (((Boolean)annotation.getClass().getMethod("unique").invoke(annotation)).booleanValue())
					{
						uniqueColumnNames.add(sAttribName);
					}
				}
			}
		}
		
		Annotation[] annotations = pEntityClass.getAnnotations();
		
		for (Annotation annotation : annotations)
		{
			// Unique Keys can also be defined in the Entity Class in the Annotation UniqueConstraint
			if (annotation.annotationType() == javax.persistence.Table.class)
			{
				javax.persistence.UniqueConstraint uniqueConstraint = (javax.persistence.UniqueConstraint)annotation.getClass().getMethod("uniqueConstraints").invoke(annotation);
				
				for (String columnName : uniqueConstraint.columnNames())
				{
					if (!uniqueColumnNames.contains(columnName.toUpperCase()))
					{
						uniqueColumnNames.add(columnName.toUpperCase());
					}
				}
			}
		}
		
		return uniqueColumnNames;
	}
	
	/**
	 * Returns the JPAServerColumnMetaData for the given Attribute.
	 * 
	 * @param pAttribute the {@link Attribute} for which to get the
	 *            {@link JPAServerColumnMetaData}.
	 * @param pEntityClass the {@link Class entity class} associated with the
	 *            {@link Attribute}.
	 * @return the JPAServerColumnMetaData for the given Attribute
	 * @throws Exception if getting the server column meta data failed.
	 */
	private JPAServerColumnMetaData getServerColumnMetaData(Attribute pAttribute, Class pEntityClass) throws Exception
	{
		JPAServerColumnMetaData serverColumnMetaData = new JPAServerColumnMetaData();
		
		JPAMappingType jpaDataType = new JPAMappingType();
		jpaDataType.setEntityClass(pAttribute.getDeclaringType().getJavaType());
		jpaDataType.setJavaTypeClass(pAttribute.getJavaType());
		jpaDataType.setDataType(JPAStorageUtil.getDataTypeIdentifierForJavaType(pAttribute.getJavaType()));
		jpaDataType.addPathNavigation(pAttribute.getName());
		jpaDataType.setGetterMethodName(JPAStorageUtil.getGetterMethodNameForAttribute(pAttribute));
		jpaDataType.setSetterMethodName(JPAStorageUtil.getSetterMethodNameForAttribute(pAttribute));
		serverColumnMetaData.setJPAMappingType(jpaDataType);
		
		Annotation[] annotations = JPAStorageUtil.getAnnotationsForAttribute(pAttribute);
		
		serverColumnMetaData.setName(JPAStorageUtil.getNameForAttribute(pAttribute, annotations));
		serverColumnMetaData.setLabel(JPAStorageUtil.getLabelForAttribute(pAttribute));
		
		serverColumnMetaData.setNullable(true);
		serverColumnMetaData.setPrecision(0);
		serverColumnMetaData.setScale(0);
		serverColumnMetaData.setDefaultValue(JPAStorageUtil.getDefaultValueForAttribute(pAttribute, pEntityClass));
		
		if (jpaDataType.getDataType() == StringDataType.TYPE_IDENTIFIER)
		{
			serverColumnMetaData.setPrecision(Integer.MAX_VALUE);
		}
		else if (jpaDataType.getDataType() == BinaryDataType.TYPE_IDENTIFIER)
		{
			serverColumnMetaData.setPrecision(Integer.MAX_VALUE);
		}
		
		for (Annotation annotation : annotations)
		{
			if (annotation.annotationType() == javax.persistence.Column.class)
			{
				serverColumnMetaData.setNullable(((Boolean)annotation.getClass().getMethod("nullable").invoke(annotation)).booleanValue());
				serverColumnMetaData.setPrecision(((Integer)annotation.getClass().getMethod("precision").invoke(annotation)).intValue());
				serverColumnMetaData.setScale(((Integer)annotation.getClass().getMethod("scale").invoke(annotation)).intValue());
				
				if (jpaDataType.getDataType() == BinaryDataType.TYPE_IDENTIFIER)
				{
					if (serverColumnMetaData.getPrecision() == 0)
					{
						serverColumnMetaData.setPrecision(Integer.MAX_VALUE);
					}
				}
				
				if (jpaDataType.getDataType() == StringDataType.TYPE_IDENTIFIER
						|| jpaDataType.getDataType() == BinaryDataType.TYPE_IDENTIFIER)
				{
					serverColumnMetaData.setPrecision(((Integer)annotation.getClass().getMethod("length").invoke(annotation)).intValue());
				}
			}
			else if (annotation.annotationType() == javax.persistence.GeneratedValue.class)
			{
				serverColumnMetaData.setAutoIncrement(true);
			}
		}
		
		return serverColumnMetaData;
		
	}
	
	/**
	 * Adds a sub storage to the internal list of all sub storages.
	 * 
	 * @param pSubStorage the sub storage to use.
	 * @param pStorageName the storage name to use.
	 * @return previous sub storage which was associated with the storage name.
	 */
	private IStorage putSubStorage(String pStorageName, IStorage pSubStorage)
	{
		if (subStorages == null)
		{
			subStorages = new HashMap<String, IStorage>();
		}
		
		return subStorages.put(pStorageName, pSubStorage);
	}
	
	/**
	 * Updates the name based on the {@link #masterEntity} and
	 * {@link #detailEntity} (if any}.
	 */
	private void updateName()
	{
		name = masterEntity.getSimpleName().toLowerCase();
		
		if (detailEntity != null)
		{
			name = name + detailEntity.getSimpleName().toLowerCase();
		}
	}
	
	/**
	 * Sets the JPA access for this storage.
	 * 
	 * @param pJPAAccess The JPA access.
	 */
	private void setJPAAccessIntern(JPAAccess pJPAAccess)
	{
		jpaAccess = pJPAAccess;
	}
	
}	// JPAStorage
