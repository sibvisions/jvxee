/*
 * Copyright 2009 SIB Visions GmbH
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

import java.io.OutputStream;
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
import javax.rad.model.ModelException;
import javax.rad.model.SortDefinition;
import javax.rad.model.condition.ICondition;
import javax.rad.model.datatype.BinaryDataType;
import javax.rad.model.datatype.StringDataType;
import javax.rad.model.reference.StorageReferenceDefinition;
import javax.rad.persist.DataSourceException;
import javax.rad.persist.IStorage;
import javax.rad.persist.MetaData;

import com.sibvisions.rad.persist.AbstractCachedStorage;
import com.sibvisions.rad.persist.AbstractStorage;

/**
 * 
 * @author Stefan Wurm
 */
public class JPAStorage extends AbstractCachedStorage {
	
	private JPAAccess jpaAccess;

	/** The encapsulation for the JPAServerColumnMetaData, JPAPrimaryKey, JPAForeignKey and JPAEmbeddedKey */
	private JPAServerMetaData serverMetaData;
		
	/** To create a CriteriaQuery for ICondition */
	private ConditionCriteriaMapper criteriaConditionMapper;
	
	/** The EntityClass for the JPAStorage */
	private Class masterEntity;
	
	/** The EntityClass for the ManyToMany relation */
	private Class detailEntity;	
	
	/** Determines the automatic link reference mode.  */
	private Boolean			bAutoLinkReference;	
	
	/** Determines the default automatic link reference mode. */
	private static boolean	bDefaultAutoLinkReference = true;	
	
	/** The open state of this DBStorage. */
	private boolean	 	bIsOpen  = false;	
	
	/** the list of sub storages. */
	private HashMap<String, IStorage> hmpSubStorages;	
	
	/**
	 * Creates a JPAStorage Object for the given Entity-Class
	 * 
	 * @param masterEntity
	 */
	public JPAStorage(Class masterEntity) throws DataSourceException {
		setMasterEntity(masterEntity);
	}
	
	
	/**
	 * Sets the Master Entity-Class for the JPAStorage
	 * 
	 * @param masterEntity
	 */
	public void setMasterEntity(Class masterEntity) throws DataSourceException {
		
		checkEntity(masterEntity);	
		
		this.masterEntity = masterEntity;
		this.setName(masterEntity.getSimpleName().toLowerCase());
	}

	/**
	 * Returns the Master Entity-Class of the JPAStorage
	 * 
	 * @return the master Entity-Class
	 */
	public Class getMasterEntity() {
		return masterEntity;
	}	
	
	/**
	 * Sets the Detail Entity-Class for the JPAStorage. This is needed for a Many-to-Many Relationship between entities.
	 * 
	 * @param detailEntity the detail Entity Class
	 */
	public void setDetailEntity(Class detailEntity) throws DataSourceException  {
		
		checkEntity(detailEntity);
		
		this.detailEntity = detailEntity;
		this.setName(this.getName()+detailEntity.getSimpleName().toLowerCase());
	}
	
	/**
	 * Returns the detail Entity-Class from The JPAStorage
	 * 
	 * @return the Detail Entity-Class
	 */
	public Class getDetailEntity() {
		return detailEntity;
	}		
	
	/**
	 * Checks if the given Class is an entity
	 * 
	 * @param entityClass
	 * @throws DataSourceException If the given Class is no entity
	 */
	private void checkEntity(Class entityClass) throws DataSourceException {
		
		try {
			
			Annotation [] annotations = entityClass.getAnnotations();
			
			for(Annotation annotation : annotations) {
				
				if(annotation.annotationType() == javax.persistence.Entity.class) {
					return;
				}
				
			}
			
			
		} catch(Exception e) {}

		throw new DataSourceException("Class "+entityClass.getName()+" is no Entity");
	}
	
	/**
	 * Creates a <code>JPAAccess</code> with the given <code>EntityManager</code> for this JPAStorage.
	 * There have to be one JPAAccess per JPAStorage.
	 * 
	 * @param entityManager
	 * @return 
	 * @throws DataSourceException If the EntityManager is not open or null
	 */
	public void setEntityManager(EntityManager entityManager) throws DataSourceException {
		
		if(entityManager == null) {
			throw new DataSourceException("EntityManager is null");
		}
		
		if(entityManager.isOpen()) {
			
			JPAAccess jpaAccess = new JPAAccess();
			jpaAccess.setEntityManager(entityManager);
			
			this.setJPAAccess(jpaAccess);
			
		} else {
		
			throw new DataSourceException("EntityManager is not open");
		
		}
	}		
	
	/**
	 * Returns the JPAAccess for the JPAStorage
	 * 
	 * @return The JPAAcces
	 */
	public JPAAccess getJPAAccess() {
		return jpaAccess;
	}

	/**
	 * Sets the JPAAccess for the JPAStorage
	 * 
	 * @param jpaAccess
	 */
	public void setJPAAccess(JPAAccess jpaAccess) {
		this.jpaAccess = jpaAccess;	
	}
	
	/**
	 * Opens the JPAStorage and checks if the DBAccess is != null.
	 * 
	 * @param pUseRepresentationColumnsAsQueryColumns	<code>yes</code> if the QueryColumns are set with 
	 *                                                  all representation columns including the Primary Key columns.
	 * @throws DataSourceException if the DBAccess is null.
	 */
	public void open() throws DataSourceException {
		openInternal(false);
	}	
	
	/**
	 * Opens the JPAStorage and checks if the DBAccess is != null.
	 * 
	 * @param pUseRepresentationColumnsAsQueryColumns	<code>yes</code> if the QueryColumns are set with 
	 *                                                  all representation columns including the Primary Key columns.
	 * @throws DataSourceException if the DBAccess is null.
	 */
	public void openInternal(boolean pUseRepresentationColumnsAsQueryColumns) throws DataSourceException {
		
		if (jpaAccess == null) {
			throw new DataSourceException("EntityManager is not set. Call setEntityManager first.");
		}
		
		serverMetaData = createServerMetaData(pUseRepresentationColumnsAsQueryColumns);
	
		bIsOpen = true;
	}		
	

	
	/**
	 * Returns if the DBStorage is open.
	 * 
	 * @return true if the DBStorage is open.
	 */
	public boolean isOpen()
	{
		return bIsOpen;
	}	
		
	
	/**
	 * Sets if the automatic link reference detection is en- or disabled. The automatic link reference is defined
	 * through a foreign key reference in the database.
	 *
	 * @param pAutoLinkReference true if the automatic LinkReference mode is on, <code>false</code> to disable 
	 *                           auto link reference mode
	 */
	public void setAutoLinkReference(boolean pAutoLinkReference)
	{
		bAutoLinkReference = Boolean.valueOf(pAutoLinkReference);
	}	
	
	/**
	 * Returns if the automatic LinkReference mode is on or off.	
	 *
	 * @return <code>true</code>if the automatic LinkReference mode is on, otherwise <code>false</code>
	 * @see #setAutoLinkReference(boolean)
	 */
	public boolean isAutoLinkReference()
	{
		if (bAutoLinkReference == null)
		{
			return bDefaultAutoLinkReference;
		}
		
		return bAutoLinkReference.booleanValue();
	}
	
	/**
	 * Creates and sets a new <code>StorageReferenceDefinition</code> for the Columns of the <code>JPAForeignKey</code>
	 * e.g. its used to make an automatic linked celleditor from a custom written view and set it on all Columns of the <code>JPAForeignKey</code>. 
	 * 
	 * @param pJPAForeignKey
	 * @param pMasterEntity The Entity for the AutoLinkReference
	 * @throws ModelException
	 */
	public void createAutomaticLinkReference(JPAForeignKey pJPAForeignKey, Class pMasterEntity) throws ModelException
	{
		createAutomaticLinkReference(pJPAForeignKey, createAutomaticLinkStorage(pMasterEntity));
	}
	
	/**
	 * Creates a new <code>JPAStorage</code> which is configured for automatic link cell editors. The auto link
	 * reference feature is disabled for this storage.
	 * 
	 * @param pMasterEntity The Entity for the Storage
	 * @return the newly created storage
	 * @throws DataSourceException if the from clause causes errors or the metadata are not available
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
	 * Creates and sets a new <code>StorageReferenceDefinition</code> with the specified <code>JPAStorage</code> for
	 * the <code>JPAForeignKey</code> Columns. 
	 * 
	 * @param pJPAForeignKey
	 * @param pJPAStorage
	 * @throws ModelException
	 */
	protected void createAutomaticLinkReference(JPAForeignKey pJPAForeignKey, AbstractStorage pJPAStorage) throws ModelException {
		
		String sStorageName = pJPAStorage.getName();
		
		putSubStorage(sStorageName, pJPAStorage);
	   						
		// The storage has to be referenced by the getSubStorages Method from outside.
		StorageReferenceDefinition srdLink = new StorageReferenceDefinition(pJPAForeignKey.getColumnNames(), ".subStorages." + sStorageName, pJPAForeignKey.getReferencedColumnNames());
			
		for (JPAServerColumnMetaData serverColumnMetaData : pJPAForeignKey.getServerColumnMetaDataAsCollection()) {
			serverColumnMetaData.setLinkReference(srdLink);
		}
		
	}	
	
	/**
	 * Adds a sub storage to the internal list of all sub storages.
	 * 
	 * @param pSubStorage	the sub storage to use.
	 * @param pStorageName	the storage name to use.
	 * @return previous sub storage which was associated with the storage name  
	 */
	private IStorage putSubStorage(String pStorageName, IStorage pSubStorage)
	{
		if (hmpSubStorages == null)
		{
			hmpSubStorages = new HashMap<String, IStorage>();
		}		
		
		return hmpSubStorages.put(pStorageName, pSubStorage);
	}	
	
	
	/**
	 * Gets all known sub storages as key / value pair.
	 * 
	 * @return the key / value pair with subtablename and substorage or <code>null</code> if no substorages are
	 *         known
	 */
	public Map<String, IStorage> getSubStorages()
	{
		return hmpSubStorages;
	}	

	@Override
	public void writeCSV(OutputStream pStream, String[] pColumnNames,
			String[] pLabels, ICondition pFilter, SortDefinition pSort,
			String pSeparator) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Creates the <code>JPAServerMetaData</code> for this JPAStorage.
	 * 
	 * @return The <code>JPAServerMetaData</code> for this JPAStorage
	 * @throws Exception
	 */
	private JPAServerMetaData createServerMetaData(boolean pUseRepresentationColumns) {
		
		JPAServerMetaData serverMetaData = new JPAServerMetaData();

		try {

			if(masterEntity != null && detailEntity != null) { // Is a Many-to-Many Relation
	
				JPAPrimaryKey jpaPrimaryKey = new JPAPrimaryKey();
				
				JPAForeignKey jpaForeignKey = createForeignKey(masterEntity);
				
				JPAMappingType jpaDataType = new JPAMappingType();
				
				jpaDataType.setEntityClass(null); // By a ManyToMany Relation there exists no Entity Class 
				jpaDataType.setJavaTypeClass(masterEntity);
				
				jpaForeignKey.setJPAMappingType(jpaDataType);		
				
				Attribute detailRelationAttribute = getDetailRelationAttribute(masterEntity, detailEntity, PersistentAttributeType.MANY_TO_MANY);
				
				if(detailRelationAttribute != null) {
				
					jpaForeignKey.setDetailEntitiesMethode(JPAStorageUtil.getGetterMethodNameForAttribute(detailRelationAttribute));
				
				}				
				
				serverMetaData.addJPAForeignKey(jpaForeignKey);
				
				jpaPrimaryKey.addForeignKey(masterEntity, jpaForeignKey);				
				
				jpaForeignKey = createForeignKey(detailEntity);
				
				jpaDataType = new JPAMappingType();
				
				jpaDataType.setEntityClass(null); // By a ManyToMany Relation there exists no Entity Class 
				jpaDataType.setJavaTypeClass(detailEntity);
				
				jpaForeignKey.setJPAMappingType(jpaDataType);		
				
				serverMetaData.addJPAForeignKey(jpaForeignKey);
				
				jpaPrimaryKey.addForeignKey(detailEntity, jpaForeignKey);	
	
				serverMetaData.setManyToMany(true);
				serverMetaData.setJPAPrimaryKey(jpaPrimaryKey);
				
			} else {
			
				JPAPrimaryKey jpaPrimaryKey = createPrimaryKey(masterEntity);				
				serverMetaData.setJPAPrimaryKey(jpaPrimaryKey);

				ArrayList<String> uniqueKeyColumnNames = this.getUniqueKeyColumnNames(masterEntity);
				
				Set<Attribute> attributes = jpaAccess.getAttributes(masterEntity);
	
				for(Attribute attribute : attributes) {
					
					if(!JPAStorageUtil.isPrimaryKeyAttribute(attribute, masterEntity)) { // PrimaryKey Attributes already initialized
					
						if(attribute.getPersistentAttributeType() == PersistentAttributeType.BASIC) {
	
							if(pUseRepresentationColumns && uniqueKeyColumnNames.size() > 0) {
							
								if(uniqueKeyColumnNames.contains(JPAStorageUtil.getNameForAttribute(attribute))) {
								
									JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attribute, masterEntity);
			
									serverMetaData.addServerColumnMetaData(serverColumnMetaData);
								
								}
							
							} else {
								
								JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attribute, masterEntity);
								
								serverMetaData.addServerColumnMetaData(serverColumnMetaData);
								
							}
							
						} else if(!pUseRepresentationColumns && (attribute.getPersistentAttributeType() == PersistentAttributeType.MANY_TO_ONE || attribute.getPersistentAttributeType() == PersistentAttributeType.ONE_TO_ONE)) {
		
							JPAForeignKey jpaForeignKey = createForeignKey(attribute.getJavaType());
							
							Attribute detailRelationAttribute = getDetailRelationAttribute(attribute.getJavaType(), masterEntity, PersistentAttributeType.ONE_TO_MANY);
							
							if(detailRelationAttribute != null) {
							
								jpaForeignKey.setDetailEntitiesMethode(JPAStorageUtil.getGetterMethodNameForAttribute(detailRelationAttribute));
							
							}
							
							JPAMappingType jpaDataType = new JPAMappingType();
							jpaDataType.setEntityClass(attribute.getDeclaringType().getJavaType());
							jpaDataType.setJavaTypeClass(attribute.getJavaType());
							jpaDataType.setDataType(JPAStorageUtil.getDataTypeIdentifierForJavaType(attribute.getJavaType()));
							jpaDataType.setGetterMethodName(JPAStorageUtil.getGetterMethodNameForAttribute(attribute));
							jpaDataType.setSetterMethodName(JPAStorageUtil.getSetterMethodNameForAttribute(attribute));
		
							jpaForeignKey.setJPAMappingType(jpaDataType);
							
							for(JPAServerColumnMetaData serverColumnMetaData : jpaForeignKey.getServerColumnMetaDataAsCollection()) {
								serverColumnMetaData.getJPAMappingType().addPathNavigation(attribute.getName());
							}
							
							serverMetaData.addJPAForeignKey(jpaForeignKey);
			
						} else if(attribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {	
							
							JPAEmbeddedKey jpaKey = new JPAEmbeddedKey();
							
							EmbeddableType embeddableType = jpaAccess.getEmbeddableType(attribute.getJavaType());
							
							Set<Attribute> setAttribute = embeddableType.getAttributes();
							
							for(Attribute attributeEmbedded : setAttribute) {
								
								if(pUseRepresentationColumns && uniqueKeyColumnNames.size() > 0) {
									
									if(uniqueKeyColumnNames.contains(JPAStorageUtil.getNameForAttribute(attribute))) {
										
										JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attributeEmbedded, attribute.getJavaType());	
										jpaKey.addServerColumnMetaData(serverColumnMetaData);											
										
									}
									
								} else {
									
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
							
							for(JPAServerColumnMetaData serverColumnMetaData : jpaKey.getServerColumnMetaDataAsCollection()) {
								serverColumnMetaData.getJPAMappingType().addPathNavigation(attribute.getName());
							}							
							
							serverMetaData.addJPAEmbeddedKey(jpaKey);			
	
						} 
					}
					
				}
								
				if(uniqueKeyColumnNames.size() > 0) { // All Unique Keys are Representation Column Names
					serverMetaData.setRepresentationColumnNames(uniqueKeyColumnNames.toArray(new String [0]));
				} else { // If there are no Unique Keys then all ColumnNames are Representation ColumnNames
					serverMetaData.setRepresentationColumnNames(serverMetaData.getColumnNames());
				}
				
			}
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return serverMetaData;
	}

	/**
	 * Creates the <code>JPAPrimaryKey</code> for the given entityClass
	 * 
	 * @param pEntityClass The Class for generating the JPAPrimaryKey
	 * @return the <code>JPAPrimaryKey</code>
	 * @throws Exception
	 */
	private JPAPrimaryKey createPrimaryKey(Class pEntityClass) throws Exception {
		
		EntityType entityType = jpaAccess.getEntityType(pEntityClass);
		
		JPAPrimaryKey jpaPrimaryKey = new JPAPrimaryKey();	
		JPAMappingType jpaDataType = new JPAMappingType();
		
		jpaPrimaryKey.setSingleIdAttribute(entityType.hasSingleIdAttribute());
				
		if(entityType.hasSingleIdAttribute()) {

			if(JPAStorageUtil.isPrimitiveOrWrapped(entityType.getIdType().getJavaType())) {
				
				Class idClass = entityType.getIdType().getJavaType();

				jpaDataType.setEntityClass(pEntityClass);
				jpaDataType.setJavaTypeClass(idClass);
				jpaDataType.setDataType(JPAStorageUtil.getDataTypeIdentifierForJavaType(idClass));		
				
				for(Attribute attribute : jpaAccess.getAttributes(pEntityClass)) {
					
					if(JPAStorageUtil.isPrimaryKeyAttribute(attribute, pEntityClass)) {
						
						jpaDataType.setGetterMethodName(JPAStorageUtil.getGetterMethodNameForAttribute(attribute));
						jpaDataType.setSetterMethodName(JPAStorageUtil.getSetterMethodNameForAttribute(attribute));
						
						JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attribute, pEntityClass);
						serverColumnMetaData.setKeyAttribute(true);

						jpaPrimaryKey.addServerColumnMetaData(serverColumnMetaData);	
						
					}
					
				}
				
			} else { //EmbeddedId Key Class
				
				Class idClass = entityType.getIdType().getJavaType();

				jpaDataType.setEntityClass(pEntityClass);
				jpaDataType.setJavaTypeClass(idClass);
				jpaDataType.setDataType(JPAStorageUtil.getDataTypeIdentifierForJavaType(idClass));				
				
				Attribute attribute = entityType.getId(idClass);
				
				jpaDataType.setGetterMethodName(JPAStorageUtil.getGetterMethodNameForAttribute(attribute));
				jpaDataType.setSetterMethodName(JPAStorageUtil.getSetterMethodNameForAttribute(attribute));

				EmbeddableType embeddableType = jpaAccess.getEmbeddableType(entityType.getIdType().getJavaType());
				
				Set<Attribute> setAttribute = embeddableType.getAttributes();
				
				for(Attribute attributeEmbedded : setAttribute) {
					
					JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attributeEmbedded, idClass);
					serverColumnMetaData.getJPAMappingType().addPathNavigation(attribute.getName());
					serverColumnMetaData.setKeyAttribute(true);

					jpaPrimaryKey.addServerColumnMetaData(serverColumnMetaData);
								
				}		
				
				jpaPrimaryKey.setEmbedded(true);
				
			}	
			
		} else {
			
			Class idClass = entityType.getIdType().getJavaType();

			jpaDataType.setEntityClass(pEntityClass);
			jpaDataType.setJavaTypeClass(idClass);
			jpaDataType.setDataType(JPAStorageUtil.getDataTypeIdentifierForJavaType(idClass));

			Set<Attribute> setAttribute = entityType.getIdClassAttributes();
			
			for(Attribute attribute : setAttribute) {
				
				JPAServerColumnMetaData serverColumnMetaData = getServerColumnMetaData(attribute, pEntityClass);
				serverColumnMetaData.setKeyAttribute(true);

				jpaPrimaryKey.addServerColumnMetaData(serverColumnMetaData);
							
			}			
			
		}

		jpaPrimaryKey.setJPAMappingType(jpaDataType);			

		
		return jpaPrimaryKey;	
	}
	
	/**
	 * Creates the <code>JPAForeignKey</code> for the given entity class
	 * 
	 * @param pEntityClass
	 * @param pParentEntityClass
	 * @return The <code>JPAForeignKey</code> for the given entity class
	 * @throws Exception
	 */
	private JPAForeignKey createForeignKey(Class pEntityClass) throws Exception {
		
		EntityType entityType = jpaAccess.getEntityType(pEntityClass);

		JPAForeignKey jpaForeignKey = new JPAForeignKey();
		jpaForeignKey.setSingleIdAttribute(entityType.hasSingleIdAttribute());
		
		JPAPrimaryKey jpaPrimaryKey = this.createPrimaryKey(pEntityClass);
				
		Attribute bestAttributeForStorageReference = this.getBestAttributeForStorageReference(pEntityClass);
		
		if(isAutoLinkReference() && bestAttributeForStorageReference != null) { 
		
			JPAServerColumnMetaData serverColumnMetaDataForStorageReference = this.getServerColumnMetaData(bestAttributeForStorageReference, bestAttributeForStorageReference.getDeclaringType().getJavaType());
			serverColumnMetaDataForStorageReference.setStorageReference(true);
			
			jpaPrimaryKey.addServerColumnMetaData(serverColumnMetaDataForStorageReference);
			
		}
		
		jpaForeignKey.setReferencedColumnNames(jpaPrimaryKey.getColumnNames());	
		
		jpaForeignKey.setKeyClass(entityType.getIdType().getJavaType());

		for(JPAServerColumnMetaData serverColumnMetaData : jpaPrimaryKey.getServerColumnMetaDataAsArray()) {
			
			String foreignKeyName = entityType.getName().toUpperCase()+"_"+serverColumnMetaData.getName();			
			
			serverColumnMetaData.setName(foreignKeyName);
			jpaForeignKey.addServerColumnMetaData(serverColumnMetaData);
			
		}


		if(isAutoLinkReference() && bestAttributeForStorageReference != null) {
			createAutomaticLinkReference(jpaForeignKey, pEntityClass);
		}
			
		return jpaForeignKey;		
	}
	
	/**
	 * Returns the best Attribute for the AutomaticLinkReference
	 * 
	 * @param pEntityClass The Class to search for the best attribute
	 * @return
	 * @throws Exception
	 */
	private Attribute getBestAttributeForStorageReference(Class pEntityClass) throws Exception {

		ArrayList<String> uniqueKeys = this.getUniqueKeyColumnNames(pEntityClass);

		Set<Attribute> attributes = jpaAccess.getAttributes(pEntityClass);
		
		for(Attribute attribute : attributes) {	

			if(attribute.getJavaType() == java.lang.String.class) {
				
				if(uniqueKeys.size() > 0) {
					
					if(uniqueKeys.contains(JPAStorageUtil.getNameForAttribute(attribute))) {
						return attribute;	
					}
						
				}  else {
					return attribute;
				}

			}
		}	
		
//		for(Attribute attribute : attributes) {
//			
//			if(attribute.getPersistentAttributeType() == PersistentAttributeType.EMBEDDED) {
//				
//				EmbeddableType embeddableType = jpaAccess.getMetamodel().embeddable(attribute.getJavaType());
//				
//				Set<Attribute> setAttribute = embeddableType.getAttributes();
//				
//				for(Attribute attributeEmbedded : setAttribute) {
//					
//					if(attributeEmbedded.getJavaType() == java.lang.String.class) {
//						
//						if(uniqueKeys.size() > 0) {
//							
//							if(uniqueKeys.contains(JPAStorageUtil.getNameForAttribute(attributeEmbedded))) {
//								return attributeEmbedded;	
//							}
//								
//						}  else {
//							return attributeEmbedded;
//						}
//						
//						
//						
//					}
//								
//				}	
//				
//			}
//			
//		}
		
		for(Attribute attribute : attributes) {	
			
			if(!JPAStorageUtil.isPrimaryKeyAttribute(attribute, pEntityClass)) {
				
				if(uniqueKeys.size() > 0) {
					
					if(uniqueKeys.contains(JPAStorageUtil.getNameForAttribute(attribute))) {
						return attribute;	
					}
						
				}  else {
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
	 * @Entity
	 * public class Customer {
	 *   ...
	 *   @OnToMany
	 *   Collection<Address> addresses = new Collection<Address>
	 *   ...
	 * }
	 * 
	 * @Entity
	 * public class Address {
	 *   ...
	 *   String street;
	 *   ....
	 * }
	 * 
	 * For this example the returned Attribute is "addresses"
	 * 
	 * @param pMasterEntity the Master Entity (in the example "Customer")
	 * @param pDetailEntity the Detail Entity (in the example "Address")
	 * @param pPersistentAttributeType the PersistentAttributeType (OneToMany or ManyToMany)
	 * @return
	 * @throws Exception
	 */
	private Attribute getDetailRelationAttribute(Class pMasterEntity, Class pDetailEntity, PersistentAttributeType pPersistentAttributeType) throws Exception {
		
		Set<Attribute> targetAttributes = jpaAccess.getAttributes(pMasterEntity);
		
		for(Attribute targetAttribute : targetAttributes) {
			
			if(targetAttribute.getPersistentAttributeType() == pPersistentAttributeType) {	
				
				if(targetAttribute.isCollection()) {
					
					Annotation[] annotations = JPAStorageUtil.getAnnotationsForAttribute(targetAttribute, pMasterEntity);
					
					for(Annotation annotation : annotations) {
							
						if(annotation.annotationType() == javax.persistence.ManyToMany.class || annotation.annotationType() == javax.persistence.OneToMany.class) {
							
							Class typeClass = JPAStorageUtil.getTypeClassForAttribute(targetAttribute);		
							
							if(typeClass == null) {
								typeClass = (Class) annotation.getClass().getMethod("targetEntity").invoke(annotation);													
							}											
							
							if(typeClass == pDetailEntity) {
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
	 * Returns the UniqueKey Column Names for the given Entity
	 * 
	 * @param pEntityClass
	 * @return
	 * @throws Exception
	 */
	private ArrayList<String> getUniqueKeyColumnNames(Class pEntityClass) throws Exception {
		
		ArrayList<String> uniqueColumnNames = new ArrayList<String>();
		
		Set<Attribute> attributes = jpaAccess.getAttributes(pEntityClass);
		
		for(Attribute attribute : attributes) { // Unique Keys can be defined in JPA 2.0 in the Annotation Column from a field
			
			Annotation[] annotations = JPAStorageUtil.getAnnotationsForAttribute(attribute, pEntityClass);
			
			for(Annotation annotation : annotations) {
					
				if(annotation.annotationType() == javax.persistence.Column.class) {
					
					if(((Boolean) annotation.getClass().getMethod("unique").invoke(annotation)).booleanValue()) {
						uniqueColumnNames.add(JPAStorageUtil.getNameForAttribute(attribute));
					}
					
				} 
				
			}			
			
		}
		
		Annotation[] annotations = pEntityClass.getAnnotations();

		for(Annotation annotation : annotations) {  // Unique Keys can also be defined in the Entity Class in the Annotation UniqueConstraint
			
			if(annotation.annotationType() == javax.persistence.Table.class) {
				
				javax.persistence.UniqueConstraint uniqueConstraint = (javax.persistence.UniqueConstraint) annotation.getClass().getMethod("uniqueConstraints").invoke(annotation);
					
				for(String columnName : uniqueConstraint.columnNames()) {
					if(!uniqueColumnNames.contains(columnName.toUpperCase())) {
						uniqueColumnNames.add(columnName.toUpperCase());
					}
				}
				
			} 
			
		}	
		
		
		return uniqueColumnNames;
	}
	
	/**
	 * Returns the JPAServerColumnMetaData for the given Attribute
	 * 
	 * @param pAttribute
	 * @param pEntityClass
	 * @return the JPAServerColumnMetaData for the given Attribute
	 * @throws Exception
	 */
	private JPAServerColumnMetaData getServerColumnMetaData(Attribute pAttribute, Class pEntityClass) throws Exception {
		
		JPAServerColumnMetaData serverColumnMetaData = new JPAServerColumnMetaData();

		JPAMappingType jpaDataType = new JPAMappingType();
		jpaDataType.setEntityClass(pAttribute.getDeclaringType().getJavaType());
		jpaDataType.setJavaTypeClass(pAttribute.getJavaType());
		jpaDataType.setDataType(JPAStorageUtil.getDataTypeIdentifierForJavaType(pAttribute.getJavaType()));
		jpaDataType.addPathNavigation(pAttribute.getName());
		jpaDataType.setGetterMethodName(JPAStorageUtil.getGetterMethodNameForAttribute(pAttribute));
		jpaDataType.setSetterMethodName(JPAStorageUtil.getSetterMethodNameForAttribute(pAttribute));
		serverColumnMetaData.setJPAMappingType(jpaDataType);
		
		serverColumnMetaData.setLabel(JPAStorageUtil.getLabelForAttribute(pAttribute));
		serverColumnMetaData.setName(JPAStorageUtil.getNameForAttribute(pAttribute));
		serverColumnMetaData.setName(pAttribute.getName().toUpperCase());
		serverColumnMetaData.setNullable(true);
		serverColumnMetaData.setPrecision(0);
		serverColumnMetaData.setScale(0);
		serverColumnMetaData.setDefaultValue(JPAStorageUtil.getDefaultValueForAttribute(pAttribute, pEntityClass));	
		
		if(jpaDataType.getDataType() == StringDataType.TYPE_IDENTIFIER) {
			serverColumnMetaData.setPrecision(Integer.MAX_VALUE);
		}  else if(jpaDataType.getDataType() == BinaryDataType.TYPE_IDENTIFIER) {
			serverColumnMetaData.setPrecision(Integer.MAX_VALUE);
		}

		Annotation[] annotations = JPAStorageUtil.getAnnotationsForAttribute(pAttribute, pEntityClass);
		
		for(Annotation annotation : annotations) {
				
			if(annotation.annotationType() == javax.persistence.Column.class) {

				serverColumnMetaData.setNullable(((Boolean) annotation.getClass().getMethod("nullable").invoke(annotation)).booleanValue());
				serverColumnMetaData.setPrecision(((Integer) annotation.getClass().getMethod("precision").invoke(annotation)).intValue());
				serverColumnMetaData.setScale(((Integer) annotation.getClass().getMethod("scale").invoke(annotation)).intValue());			
				
				if(jpaDataType.getDataType() == BinaryDataType.TYPE_IDENTIFIER) {
					if(serverColumnMetaData.getPrecision() == 0) {
						serverColumnMetaData.setPrecision(Integer.MAX_VALUE);
					}
				}
				
				if(jpaDataType.getDataType() == StringDataType.TYPE_IDENTIFIER || jpaDataType.getDataType() == BinaryDataType.TYPE_IDENTIFIER) {
					serverColumnMetaData.setPrecision(((Integer) annotation.getClass().getMethod("length").invoke(annotation)).intValue());
				}
				
			} else if(annotation.annotationType() == javax.persistence.GeneratedValue.class) {
				serverColumnMetaData.setAutoIncrement(true);					
			}
			
		}

		return serverColumnMetaData;
		
	}		

	/**
	 * {@inheritDoc}
	 */
	public MetaData executeGetMetaData() throws DataSourceException {
				
		if (!isOpen())
		{
			throw new DataSourceException("JPAStorage isn't open!");			
		}
		
		return serverMetaData.getMetaData();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getEstimatedRowCount(ICondition pFilter)
			throws DataSourceException {
		
		CriteriaQuery<Long> countCriteriaQuery = criteriaConditionMapper.getCountCriteriaQuery(pFilter, masterEntity, null);
		
		Long count = jpaAccess.countByCriteria(countCriteriaQuery);	
		
		return count.intValue();
	}

	/**
	 * {@inheritDoc}
	 */	
	public List<Object[]> executeFetch(ICondition pFilter, SortDefinition pSort, int pFromRow, int pMinimumRowCount) throws DataSourceException {


		if (!isOpen()) {
			throw new DataSourceException("JPAStorage isn't open!");			
		}		
		
		List<Object[]> objects = new ArrayList<Object[]>();
		Collection objectList = new ArrayList();
		
		Object entityM = null;

		try {
		
				
			if(pFilter != null || pSort != null) {
				
				if(criteriaConditionMapper == null) {
					criteriaConditionMapper = new ConditionCriteriaMapper(serverMetaData, jpaAccess.getCriteriaBuilder());
				}
				
				
				JPAForeignKey jpaForeignKey = serverMetaData.getJPAForeignKeyForCondition(pFilter);

				if(jpaForeignKey != null && jpaForeignKey.hasDetailEntitiesMethode() && pSort == null) {
				
					Map<String, Object> map = serverMetaData.getValueMapForCondition(pFilter);
					
					Object primaryKey = jpaForeignKey.getKeyForEntity(map);
					
					if(primaryKey == null) {
						
				        CriteriaQuery criteriaQuery = criteriaConditionMapper.getCriteriaQuery(pFilter, pSort, masterEntity, null);
						
						objectList = jpaAccess.findByCriteria(criteriaQuery);	

					} else {
						
						entityM = jpaAccess.findById(primaryKey, jpaForeignKey.getJPAMappingType().getJavaTypeClass());
						
						jpaAccess.refresh(entityM, jpaForeignKey.getJPAMappingType().getJavaTypeClass());
					
						Object obj = jpaForeignKey.getDetailEntities(entityM);
						
						if(obj != null && obj instanceof Collection) {
							
							if(obj instanceof Collection) {
								objectList = (Collection) obj;	
							} else {
								objectList.add(obj);
							}
						}
							
					}
					
				
				} else {
				
					if(serverMetaData.isManyToMany()) {
						
						CriteriaQuery criteriaQuery = criteriaConditionMapper.getCriteriaQuery(pFilter, null, masterEntity, null);
						
						Collection col = jpaAccess.findByCriteria(criteriaQuery);
						
						if(col.size() == 1) {
							entityM = col.iterator().next();	
						}
						
						criteriaQuery = criteriaConditionMapper.getCriteriaQuery(pFilter, pSort, masterEntity, getDetailRelationAttribute(masterEntity, detailEntity, PersistentAttributeType.MANY_TO_MANY).getName());
						
						objectList = jpaAccess.findByCriteria(criteriaQuery);	
						
						
					} else {
					
				        CriteriaQuery criteriaQuery = criteriaConditionMapper.getCriteriaQuery(pFilter, pSort, masterEntity, null);

						objectList = jpaAccess.findByCriteria(criteriaQuery);	
					
					}
					
				}
				
			} else {
				
				objectList = (Collection) jpaAccess.findAll(masterEntity);
				
			}

	        for(Object entityD : objectList) {
	        	
	        	if(serverMetaData.isManyToMany()) {

    	        	Object dataRow [] = this.getDataRowForEntities(entityM, entityD); 	       
    		        objects.add(dataRow);

	        	} else {
	        		
		        	Object dataRow [] = this.getDataRowForEntity(entityD); 	       
			        objects.add(dataRow);	        		
	        		
	        	}
 
	        }	
	        
		} catch(DataSourceException dse) {
			throw dse;
			
		} catch(Exception e) {
			e.printStackTrace();
			new DataSourceException("Fetch was not possible", e);
		}


		objects.add(null);   

		return objects;
	}

	/**
	 * {@inheritDoc}
	 */	
	public Object[] executeRefetchRow(Object[] pDataRow) throws DataSourceException {

		if (!isOpen()) {
			throw new DataSourceException("JPAStorage isn't open!");			
		}			
		
		Object [] dataRow = null;
		
		try {
		
			Object primaryKey = serverMetaData.getJPAPrimaryKey().getKeyForEntity(serverMetaData.getMapForDataRow(pDataRow));
	
			Object entityForFetch = jpaAccess.findById(primaryKey, masterEntity);
			
			dataRow = this.getDataRowForEntity(entityForFetch);
			
		} catch(DataSourceException dse) {
			
			throw dse;
			
		} catch(Exception e) {
			new DataSourceException("Refetch was not possible", e);
		}
		
		return dataRow;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] executeInsert(Object[] pDataRow) throws DataSourceException {
		
		if (!isOpen()) {
			throw new DataSourceException("JPAStorage isn't open!");			
		}			
		
		Object [] dataRow = null;
			
		try {
			
			if(serverMetaData.isManyToMany()) {
				
				JPAForeignKey foreignKey1 = serverMetaData.getJPAPrimaryKey().getForeignKey(masterEntity);
				JPAForeignKey foreignKey2 = serverMetaData.getJPAPrimaryKey().getForeignKey(detailEntity);

				Object primaryKey1 = foreignKey1.getKeyForEntity(serverMetaData.getMapForDataRow(pDataRow));
				Object primaryKey2 = foreignKey2.getKeyForEntity(serverMetaData.getMapForDataRow(pDataRow));
				
				if(primaryKey1 != null && primaryKey2 != null) {
				
					Object entity1 = jpaAccess.findById(primaryKey1, masterEntity);
					
					Collection objectList = (Collection) foreignKey1.getDetailEntities(entity1);
	
					Object entity2 = jpaAccess.findById(primaryKey2, detailEntity);
	
					objectList.add(entity2);
					
					jpaAccess.update(entity1, masterEntity);
					
					dataRow = this.getDataRowForEntities(entity1, entity2);
				
				}
				
			} else {
			
				Object entityForInsert = masterEntity.newInstance();
			
				this.mappeDataRowToEntity(pDataRow, entityForInsert);
				
				entityForInsert = jpaAccess.insert(entityForInsert, masterEntity);
				
				dataRow = this.getDataRowForEntity(entityForInsert);
				
			}

		} catch(DataSourceException dse) {
			
			throw dse;
			
		} catch(Exception e) {
			throw new DataSourceException("Insert was not possible", e);
		}
		
		return dataRow;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] executeUpdate(Object[] pOldDataRow, Object[] pNewDataRow) throws DataSourceException {

		if (!isOpen()) {
			throw new DataSourceException("JPAStorage isn't open!");			
		}			
		
		Object [] dataRow = null;
		
		try {
			
			if(serverMetaData.isManyToMany()) {
				
				JPAForeignKey foreignKey1 = serverMetaData.getJPAPrimaryKey().getForeignKey(masterEntity);
				JPAForeignKey foreignKey2 = serverMetaData.getJPAPrimaryKey().getForeignKey(detailEntity);

				Object primaryKey1 = foreignKey1.getKeyForEntity(serverMetaData.getMapForDataRow(pOldDataRow));
				Object primaryKeyOld2 = foreignKey2.getKeyForEntity(serverMetaData.getMapForDataRow(pOldDataRow));
				Object primaryKeyNew2 = foreignKey2.getKeyForEntity(serverMetaData.getMapForDataRow(pNewDataRow));
				
				Object entity1 = jpaAccess.findById(primaryKey1, masterEntity);

				Collection objectList = (Collection) foreignKey1.getDetailEntities(entity1);

				Object entityOld2 = jpaAccess.findById(primaryKeyOld2, detailEntity);

				objectList.remove(entityOld2);
				
				Object entityNew2 = jpaAccess.findById(primaryKeyNew2, detailEntity);
				
				objectList.add(entityNew2);
				
				jpaAccess.update(entity1, masterEntity);
				
				dataRow = this.getDataRowForEntities(entity1, entityNew2);
				
			} else {
				
				Object primaryKey = serverMetaData.getJPAPrimaryKey().getKeyForEntity(serverMetaData.getMapForDataRow(pOldDataRow));
				
				Object entityForUpdate = jpaAccess.findById(primaryKey, masterEntity);
		
				this.mappeDataRowToEntity(pNewDataRow, entityForUpdate);
				
				jpaAccess.update(entityForUpdate, masterEntity);
				
				dataRow = this.getDataRowForEntity(entityForUpdate);
				
			}

		} catch(DataSourceException dse) {
			
			throw dse;
			
		} catch(Exception e) {
			throw new DataSourceException("Update was not possible", e);
		}
		
		return dataRow;
	}

	/**
	 * {@inheritDoc}
	 */
	public void executeDelete(Object[] pDeleteDataRow) throws DataSourceException {

		if (!isOpen()) {
			throw new DataSourceException("JPAStorage isn't open!");			
		}			
		
		try {
			
			if(serverMetaData.isManyToMany()) {
				
				JPAForeignKey foreignKey1 = serverMetaData.getJPAPrimaryKey().getForeignKey(masterEntity);
				JPAForeignKey foreignKey2 = serverMetaData.getJPAPrimaryKey().getForeignKey(detailEntity);

				Object primaryKey1 = foreignKey1.getKeyForEntity(serverMetaData.getMapForDataRow(pDeleteDataRow));
				Object primaryKey2 = foreignKey2.getKeyForEntity(serverMetaData.getMapForDataRow(pDeleteDataRow));
				
				Object entity1 = jpaAccess.findById(primaryKey1, masterEntity);

				Collection objectList = (Collection) foreignKey1.getDetailEntities(entity1);
					
				Object entity2 = jpaAccess.findById(primaryKey2, detailEntity);

				objectList.remove(entity2);
				
				jpaAccess.update(entity1, masterEntity);
				
			} else {
				
				Object primaryKey = serverMetaData.getJPAPrimaryKey().getKeyForEntity(serverMetaData.getMapForDataRow(pDeleteDataRow));
				
				Object entityForDelete = jpaAccess.findById(primaryKey, masterEntity);
				
				jpaAccess.delete(entityForDelete, masterEntity);
				
			}

		} catch(DataSourceException dse) {
			
			throw dse;
			
		} catch(Exception e) {
			throw new DataSourceException("Delete was not possible", e);
		}
	}
	
	protected Object [] getDataRowForEntities(Object pEntity1, Object pEntity2) throws DataSourceException {
		
		if (!isOpen()) {
			throw new DataSourceException("JPAStorage isn't open!");			
		}			
		
		try {
		
			ArrayList dataRow = new ArrayList();
			
			JPAForeignKey jpaForeignKey1 = serverMetaData.getJPAPrimaryKey().getForeignKey(pEntity1.getClass());
	
			Object primaryKey1 = jpaAccess.getIdentifier(pEntity1);
			
			for(JPAServerColumnMetaData serverColumnMetaData : jpaForeignKey1.getServerColumnMetaDataAsArray()) {
				
				if(serverColumnMetaData.isKeyAttribute()) {
					
		    		if(JPAStorageUtil.isPrimitiveOrWrapped(primaryKey1.getClass())) {
		    			
		    			dataRow.add(serverColumnMetaData.getJPAMappingType().getValue(pEntity1));   	
		    			
		    		} else {
		    			
		    			dataRow.add(serverColumnMetaData.getJPAMappingType().getValue(primaryKey1)); 
		        		
		    		}					
					
				} else {
					
						dataRow.add(serverColumnMetaData.getJPAMappingType().getValue(pEntity1)); 
					
				}
				
	
		
			}
			
			JPAForeignKey jpaForeignKey2 = serverMetaData.getJPAPrimaryKey().getForeignKey(pEntity2.getClass());
			
			Object primaryKey2 = jpaAccess.getIdentifier(pEntity2);
			
			for(JPAServerColumnMetaData serverColumnMetaData : jpaForeignKey2.getServerColumnMetaDataAsArray()) {
				
				if(serverColumnMetaData.isKeyAttribute()) {
				
		    		if(JPAStorageUtil.isPrimitiveOrWrapped(primaryKey2.getClass())) {
		    			
		    			dataRow.add(primaryKey2);    	
		    			
		    		} else {
		    			
		    			dataRow.add(serverColumnMetaData.getJPAMappingType().getValue(primaryKey2)); 
		        		
		    		}
		    		
				} else {
					
					dataRow.add(serverColumnMetaData.getJPAMappingType().getValue(pEntity2)); 
				
				}	    		
				
			}	
			
			return dataRow.toArray();
			
		} catch(Exception e) {
    		throw new DataSourceException("Problems by mapping entities to a DataRow", e);				
		}		
	}		

	protected Object [] getDataRowForEntity(Object pEntity) throws DataSourceException {

		if (!isOpen()) {
			throw new DataSourceException("JPAStorage isn't open!");			
		}			
		
		try {
		
	    	Object [] dataRow = new Object[serverMetaData.getMetaData().getColumnMetaData().length];
	    	
	    	Object primaryKey = jpaAccess.getIdentifier(pEntity);
	    	 	
	    	// PrimaryKey Columns
	    	for(JPAServerColumnMetaData serverColumnMetaData : serverMetaData.getJPAPrimaryKey().getServerColumnMetaDataAsArray()) {
	    		
	    		int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());
	
	        	dataRow[index] = serverColumnMetaData.getJPAMappingType().getValue(primaryKey);    
				
	    	}
	    	
	    	// Columns from the Entity
			for(JPAServerColumnMetaData serverColumnMetaData : serverMetaData.getServerColumnMetaData()) {
	
				int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());
				
				dataRow[index] = serverColumnMetaData.getJPAMappingType().getValue(pEntity);			
	
			}
			
			// Embedded Columns
			for(JPAEmbeddedKey jpaEmbeddedKey : serverMetaData.getJPAEmbeddedKeys()) {
				
				Object entityInEntity = jpaEmbeddedKey.getJPAMappingType().getValue(pEntity);
				
				if(entityInEntity != null) {
	
					for(JPAServerColumnMetaData serverColumnMetaData : jpaEmbeddedKey.getServerColumnMetaDataAsArray()) {
	
						int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());
	
		    			dataRow[index] = serverColumnMetaData.getJPAMappingType().getValue(entityInEntity); 
					}
				
				}
	
			}		
			
			// ForeignKey Columns
			for(JPAForeignKey foreignKey : serverMetaData.getJPAForeignKeys()) {
				
				Object entityInEntity = foreignKey.getJPAMappingType().getValue(pEntity);
				
				if(entityInEntity != null) {
	
					primaryKey = jpaAccess.getIdentifier(entityInEntity);
				
					for(JPAServerColumnMetaData serverColumnMetaData : foreignKey.getServerColumnMetaDataAsArray()) {
	
						int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());
						
						if(serverColumnMetaData.isKeyAttribute()) {
	
			        		dataRow[index] = serverColumnMetaData.getJPAMappingType().getValue(primaryKey);    
	
						} else {
							
							dataRow[index] = serverColumnMetaData.getJPAMappingType().getValue(entityInEntity); 
							
						}
	
					}
				
				}
	
			}
		
		
			return dataRow;
			
		} catch(Exception e) {
    		throw new DataSourceException("Problems by mapping an entity to a DataRow", e);				
		}
		
	}	

	protected void mappeDataRowToEntity(Object [] pDataRow, Object pEntity) throws DataSourceException {

		if (!isOpen()) {
			throw new DataSourceException("JPAStorage isn't open!");			
		}			
		
		try {
		
	    	// PrimaryKey Columns
	    	for(JPAServerColumnMetaData serverColumnMetaData : serverMetaData.getJPAPrimaryKey().getServerColumnMetaDataAsArray()) {
	 
	    		int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());	
	    		
	    		if(serverMetaData.getJPAPrimaryKey().isEmbedded()) {
	    			
	    			Object primaryKey = serverMetaData.getJPAPrimaryKey().getKeyForEntity(serverMetaData.getMapForDataRow(pDataRow));
	    			
	    			serverMetaData.getJPAPrimaryKey().getJPAMappingType().setValue(pEntity, primaryKey);
	    			
	    			break;
	    			
	    		} else {
	    			
	    			serverColumnMetaData.getJPAMappingType().setValue(pEntity, pDataRow[index]);    			
	    			
	    		}
	
	    	}		
			
	    	// Columns from the Entity
			for(JPAServerColumnMetaData serverColumnMetaData : serverMetaData.getServerColumnMetaData()) {
	
	    		int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());			
	
				serverColumnMetaData.getJPAMappingType().setValue(pEntity, pDataRow[index]);
								
			}
			
			// Embedded Columns
			for(JPAEmbeddedKey jpaEmbeddedKey : serverMetaData.getJPAEmbeddedKeys()) {
				
				Object embeddedEntity = jpaEmbeddedKey.getJPAMappingType().getValue(pEntity);
				
				if(embeddedEntity == null) { // If the embeddedEntity is null, it has to be instantiated
					
					embeddedEntity = jpaEmbeddedKey.getJPAMappingType().getJavaTypeClass().newInstance();
					
				}
				
				for(JPAServerColumnMetaData serverColumnMetaData : jpaEmbeddedKey.getServerColumnMetaDataAsArray()) {
					
					int index = serverMetaData.getColumnMetaDataIndex(serverColumnMetaData.getName());	
					
					serverColumnMetaData.getJPAMappingType().setValue(embeddedEntity, pDataRow[index]);
	
				}
				
				jpaEmbeddedKey.getJPAMappingType().setValue(pEntity, embeddedEntity);
	
			}			
			
			// ForeignKey Columns
			for(JPAForeignKey foreignKey : serverMetaData.getJPAForeignKeys()) {
	
				Object primaryKey = foreignKey.getKeyForEntity(serverMetaData.getMapForDataRow(pDataRow));
				
				if(primaryKey != null) {
					
					Object entityInEntity = jpaAccess.findById(primaryKey, foreignKey.getJPAMappingType().getJavaTypeClass());
	
					if(entityInEntity != null) {
						foreignKey.getJPAMappingType().setValue(pEntity, entityInEntity);				
					}
				
				}
	
			}
			
		} catch(Exception e) {
			e.printStackTrace();
    		throw new DataSourceException("Problems by mapping a DataRow to an entity", e);			
		}
	}	
	
}
