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
import java.util.ArrayList;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.rad.model.SortDefinition;
import javax.rad.model.condition.And;
import javax.rad.model.condition.CompareCondition;
import javax.rad.model.condition.Equals;
import javax.rad.model.condition.Greater;
import javax.rad.model.condition.GreaterEquals;
import javax.rad.model.condition.ICondition;
import javax.rad.model.condition.Less;
import javax.rad.model.condition.LessEquals;
import javax.rad.model.condition.Like;
import javax.rad.model.condition.LikeIgnoreCase;
import javax.rad.model.condition.Not;
import javax.rad.model.condition.OperatorCondition;
import javax.rad.model.condition.Or;
import javax.rad.persist.DataSourceException;

/**
 * The <code>ConditionCriteriaMapper</code> creates from the ICondition and the SortDefinition
 * a CriteriaQuery. 
 *  
 * @see javax.rad.model.datatype.IDataType
 * @see javax.rad.persist.ColumnMetaData
 * 
 * @author Stefan Wurm
 */
public class ConditionCriteriaMapper 
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/** The ServerMetaData from the Storage. **/
	private JPAServerMetaData serverMetaData;
	
	/** The CriteriaBuilder from the entityManager. **/
	private CriteriaBuilder criteriaBuilder;
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Creates the ConditionCriteriaMapper.
	 * 
	 * @param pServerMetaData
	 * @param pCriteriaBuilder
	 */
	public ConditionCriteriaMapper(JPAServerMetaData pServerMetaData, CriteriaBuilder pCriteriaBuilder) 
	{
		serverMetaData = pServerMetaData;
		criteriaBuilder = pCriteriaBuilder;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Returns the correct CriteriaQuery for the ICondition and SortDefinition for the given entity.
	 * 
	 * @param pCondition the ICondition
	 * @param pSort the SortDefinition
	 * @param pFromEntity the Entity to select
	 * @param pJoinAttributeName the Attribute Name for a join. It is used in a ManyToMany Realtion
	 * @return
	 * @throws DataSourceException Throws a DataSourceException when the defined Columns are not in the ServerMetaData
	 */
	public CriteriaQuery getCriteriaQuery(ICondition pCondition, SortDefinition pSort, Class pFromEntity, String pJoinAttributeName) throws DataSourceException 
	{
		CriteriaQuery criteriaQuery = criteriaBuilder.createQuery();
		From from = criteriaQuery.from(pFromEntity);
		
		if (pCondition != null) 
		{
			if (pJoinAttributeName != null) 
			{ 
				// Is a ManyToMany Relationship

				Predicate predicate = null;
				
				if (pCondition instanceof CompareCondition) 
				{
					predicate = getPredicateForCompareCondition((CompareCondition) pCondition, from);
				} 
				else if (pCondition instanceof OperatorCondition) 
				{	
					predicate = getPredicateForOperatorCondition((OperatorCondition) pCondition, from);
				} 
				else if (pCondition instanceof Not) 
				{
					predicate = getPredicateForNotCondition((Not) pCondition, from);
				}				

//TODO remove debug code???				
//				JPAForeignKey jpaForeignKey = serverMetaData.getJPAForeignKeyForCondition(pCondition);
	
				if (predicate != null) 
				{
					from = from.join(pJoinAttributeName);
					criteriaQuery.select(from).where(predicate);
				}
			} 
			else 
			{
				if (pCondition instanceof CompareCondition) 
				{
					Predicate predicate = getPredicateForCompareCondition((CompareCondition) pCondition, from);
					
					criteriaQuery.where(predicate);
					
					criteriaQuery.select(from);
				} 
				else if (pCondition instanceof OperatorCondition) 
				{	
					Predicate predicate = getPredicateForOperatorCondition((OperatorCondition) pCondition, from);
					
					criteriaQuery.where(predicate);
					
					criteriaQuery.select(from);
				} 
				else if (pCondition instanceof Not) 
				{
					Predicate predicate = getPredicateForNotCondition((Not) pCondition, from);
					
					criteriaQuery.where(predicate);
					
					criteriaQuery.select(from);					
				}
			}	
		}
		
		if (pSort != null) 
		{
			if (pCondition == null) 
			{
				criteriaQuery.select(from);
			}
			
			setOrdering(criteriaQuery, from, pSort);	
		}			
				
		return criteriaQuery;		
	}

	
	/**
	 * Returns the Count-Select for the given ICondition and entity Class.
	 * 
	 * @param pCondition the ICondition
	 * @param pFromEntity the Entity to select
	 * @param pJoinAttributeName the Attribute Name for a join. It is used in a ManyToMany Realtion
	 * @return
	 * @throws DataSourceException throws a DataSourceException when the defined Columns are not in the ServerMetaData
	 */
	public CriteriaQuery getCountCriteriaQuery(ICondition pCondition, Class pFromEntity, String pJoinAttributeName) throws DataSourceException 
	{
		CriteriaQuery criteriaQuery = criteriaBuilder.createQuery();
		From from = criteriaQuery.from(pFromEntity);
				
		if (pJoinAttributeName != null) 
		{
			Predicate predicate = getPredicateForCompareCondition((CompareCondition) pCondition, from);

			Join educations = from.join(pJoinAttributeName);
			criteriaQuery.select(educations).where(predicate);
		} 
		else 
		{
			if (pCondition instanceof CompareCondition) 
			{
				Predicate predicate = getPredicateForCompareCondition((CompareCondition) pCondition, from);
				
				criteriaQuery.where(predicate);
				
				criteriaQuery.select(criteriaBuilder.count(from));
			}
			else if (pCondition instanceof OperatorCondition) 
			{	
				Predicate predicate = getPredicateForOperatorCondition((OperatorCondition) pCondition, from);
				
				criteriaQuery.where(predicate);
				
				criteriaQuery.select(criteriaBuilder.count(from));
			} 
			else if (pCondition instanceof Not) 
			{
				Predicate predicate = getPredicateForNotCondition((Not) pCondition, from);
				
				criteriaQuery.where(predicate);
				
				criteriaQuery.select(criteriaBuilder.count(from));		
			}
		}

		return criteriaQuery;		
	}	
	
	/**
	 * Returns the javax.persistence.criteria.Predicate for the given OperatorCondition.
	 * 
	 * @param pOperatorCondition
	 * @param pFrom
	 * @return
	 * @throws DataSourceException throws a DataSourceException when the defined Columns are not in the ServerMetaData
	 */
	private Predicate getPredicateForOperatorCondition(OperatorCondition pOperatorCondition, From pFrom) throws DataSourceException 
	{
		ArrayList<Predicate> predicates = new ArrayList<Predicate>();
		
		for (ICondition subCondition : pOperatorCondition.getConditions()) 
		{
			Predicate predicate = null;
			
			if (subCondition instanceof OperatorCondition) 
			{
				predicate = getPredicateForOperatorCondition((OperatorCondition) subCondition, pFrom);
			} 
			else if (subCondition instanceof CompareCondition) 
			{
				predicate = getPredicateForCompareCondition((CompareCondition) subCondition, pFrom);				
			} 
			else if (subCondition instanceof Not) 
			{
				predicate = getPredicateForNotCondition((Not) subCondition, pFrom);
			}
			
			predicates.add(predicate);
		}			
		
		if (pOperatorCondition instanceof Or) 
		{
			Predicate predicate = criteriaBuilder.or(predicates.toArray(new Predicate [1]));
			
			return predicate;
		} 
		else if (pOperatorCondition instanceof And) 
		{
			Predicate predicate = criteriaBuilder.and(predicates.toArray(new Predicate [1]));
			
			return predicate;
		}		
		
		return null;
	}

	/**
	 * Returns the Predicate for the Not Condition.
	 * 
	 * @param pNotCondition
	 * @param pFrom
	 * @return
	 * @throws DataSourceException throws a DataSourceException when the defined Columns are not in the ServerMetaData
	 */
	private Predicate getPredicateForNotCondition(Not pNotCondition, From pFrom) throws DataSourceException 
	{
		Predicate predicate = null;
		
		ICondition inNot = pNotCondition.getCondition();
		
		if (inNot instanceof OperatorCondition) 
		{
			predicate = getPredicateForOperatorCondition((OperatorCondition) inNot, pFrom); 
		} 
		else if (inNot instanceof CompareCondition) 
		{
			predicate = getPredicateForCompareCondition((CompareCondition) inNot, pFrom);
		} 
		else if (inNot instanceof Not) 
		{
			predicate = getPredicateForNotCondition((Not) inNot, pFrom);
		}
		
		return criteriaBuilder.not(predicate);
	}
	
	/**
	 * Returns the javax.persistence.criteria.Predicate for the CompareCondition.
	 * 
	 * @param pCompareCondition
	 * @param pFrom
	 * @return
	 * @throws DataSourceException throws a DataSourceException when the defined Columns are not in the ServerMetaData
	 */
	private Predicate getPredicateForCompareCondition(CompareCondition pCompareCondition, From pFrom) throws DataSourceException 
	{
		Predicate predicate = null;
		
		String columnName = pCompareCondition.getColumnName();
		Object value = pCompareCondition.getValue();

		JPAServerColumnMetaData serverColumnMetaData = serverMetaData.getServerColumnMetaData(columnName);
		
		if (serverColumnMetaData == null) 
		{
			throw new DataSourceException("Column " + columnName + " unkown");
		}
		
		Path path = null;
		
		for (String attributeName : serverColumnMetaData.getJPAMappingType().getPathNavigation()) 
		{
			if (path == null) 
			{
				path = pFrom.get(attributeName);
			} 
			else 
			{
				path = path.get(attributeName);					
			}			
		}	
		
		if (pCompareCondition instanceof Equals) 
		{
			if (value == null) 
			{
				path = pFrom.get(serverColumnMetaData.getJPAMappingType().getPathNavigation().get(0));
				predicate = criteriaBuilder.isNull(path);
			} 
			else 
			{
				predicate = criteriaBuilder.equal(path, value);
			}
		} 
		else if (pCompareCondition instanceof LikeIgnoreCase && value != null) 
		{
			if (value instanceof String) 
			{
				value = ((String) value).replace('*', '%').replace('?', '%');
				
				predicate = criteriaBuilder.like(criteriaBuilder.upper(path), ((String) value).toUpperCase());				
			}
		} 
		else if (pCompareCondition instanceof Like && value != null) 
		{
			if (value instanceof String) 
			{
				value = ((String) value).replace('*', '%').replace('?', '%');
				
				predicate = criteriaBuilder.like(path, ((String) value));
			}
		} 
		else if (pCompareCondition instanceof Greater && value != null) 
		{
			if (value instanceof Byte) 
			{
				predicate = criteriaBuilder.greaterThan(path, (Byte)value);
			} 
			else if (value instanceof Short) 
			{
				predicate = criteriaBuilder.greaterThan(path, (Short)value);
			} 
			else if (value instanceof Integer) 
			{
				predicate = criteriaBuilder.greaterThan(path, (Integer)value);

			} 
			else if (value instanceof Long) 
			{
				predicate = criteriaBuilder.greaterThan(path, (Long)value);
			} 
			else if (value instanceof Float) 
			{
				predicate = criteriaBuilder.greaterThan(path, (Float)value);
			} 
			else if (value instanceof Double) 
			{
				predicate = criteriaBuilder.greaterThan(path, (Double)value);
			} 
			else if (value instanceof BigDecimal) 
			{
				predicate = criteriaBuilder.greaterThan(path, (BigDecimal)value);
			} 
			else if (value instanceof java.util.Date) 
			{
				predicate = criteriaBuilder.greaterThan(path, (java.util.Date)value);
			} 
			else if (value instanceof String) 
			{
				predicate = criteriaBuilder.greaterThan(path, (String)value);
			}					
		} 
		else if (pCompareCondition instanceof GreaterEquals && value != null) 
		{
			if (value instanceof Byte) 
			{
				predicate = criteriaBuilder.greaterThanOrEqualTo(path, (Byte)value);
			} 
			else if (value instanceof Short) 
			{
				predicate = criteriaBuilder.greaterThanOrEqualTo(path, (Short)value);
			} 
			else if (value instanceof Integer) 
			{
				predicate = criteriaBuilder.greaterThanOrEqualTo(path, (Integer)value);
			} 
			else  if (value instanceof Long) 
			{
				predicate = criteriaBuilder.greaterThanOrEqualTo(path, (Long)value);
			} 
			else if (value instanceof Float) 
			{
				predicate = criteriaBuilder.greaterThanOrEqualTo(path, (Float)value);
			} 
			else if (value instanceof Double) 
			{
				predicate = criteriaBuilder.greaterThanOrEqualTo(path, (Double)value);
			} 
			else if (value instanceof BigDecimal) 
			{
				predicate = criteriaBuilder.greaterThanOrEqualTo(path, (BigDecimal)value);
			} 
			else if (value instanceof java.util.Date) 
			{
				predicate = criteriaBuilder.greaterThanOrEqualTo(path, (java.util.Date)value);
			} 
			else if (value instanceof String) 
			{
				predicate = criteriaBuilder.greaterThanOrEqualTo(path, (String)value);
			}					
		} 
		else if (pCompareCondition instanceof Less && value != null) 
		{
			if (value instanceof Byte) 
			{
				predicate = criteriaBuilder.lessThan(path, (Byte)value);

			} 
			else if (value instanceof Short) 
			{
				predicate = criteriaBuilder.lessThan(path, (Short)value);
			} 
			else if (value instanceof Integer)
			{
				predicate = criteriaBuilder.lessThan(path, (Integer)value);
			} 
			else  if (value instanceof Long)
			{
				predicate = criteriaBuilder.lessThan(path, (Long)value);
			}
			else if (value instanceof Float)
			{
				predicate = criteriaBuilder.lessThan(path, (Float)value);
			} 
			else if (value instanceof Double)
			{
				predicate = criteriaBuilder.lessThan(path, (Double)value);
			} 
			else if (value instanceof BigDecimal)
			{
				predicate = criteriaBuilder.lessThan(path, (BigDecimal)value);
			} 
			else if (value instanceof java.util.Date)
			{
				predicate = criteriaBuilder.lessThan(path, (java.util.Date)value);
			} 
			else if (value instanceof String)
			{
				predicate = criteriaBuilder.lessThan(path, (String)value);
			}		
		} 
		else if (pCompareCondition instanceof LessEquals && value != null) 
		{
			if (value instanceof Byte) 
			{
				predicate = criteriaBuilder.lessThanOrEqualTo(path, (Byte)value);
			} 
			else if (value instanceof Short) 
			{
				predicate = criteriaBuilder.lessThanOrEqualTo(path, (Short)value);
			} 
			else if (value instanceof Integer) 
			{
				predicate = criteriaBuilder.lessThanOrEqualTo(path, (Integer)value);
			} 
			else  if (value instanceof Long) 
			{
				predicate = criteriaBuilder.lessThanOrEqualTo(path, (Long)value);
			} 
			else if (value instanceof Float) 
			{
				predicate = criteriaBuilder.lessThanOrEqualTo(path, (Float)value);
			} 
			else if (value instanceof Double) 
			{
				predicate = criteriaBuilder.lessThanOrEqualTo(path, (java.lang.Double)value);
			} 
			else if (value instanceof BigDecimal)
			{
				predicate = criteriaBuilder.lessThanOrEqualTo(path, (BigDecimal)value);
			} 
			else if (value instanceof java.util.Date) 
			{
				predicate = criteriaBuilder.lessThanOrEqualTo(path, (java.util.Date)value);
			} 
			else if (value instanceof String) 
			{
				predicate = criteriaBuilder.lessThanOrEqualTo(path, (String)value);
			}		
		}				
		
		return predicate;		
	}
	
	/**
	 * Sets the Order By to the given criteriaQuery.
	 * 
	 * @param pCriteriaQuery
	 * @param pFrom
	 * @param pSort
	 * @throws DataSourceException
	 */
	public void setOrdering(CriteriaQuery pCriteriaQuery, From pFrom, SortDefinition pSort) throws DataSourceException 
	{
		
		String [] columnNames = pSort.getColumns();
		boolean [] isAscending = pSort.isAscending();
		
		Order [] order = new Order[columnNames.length];
		
		for (int i = 0; i < columnNames.length; i++) 
		{
			JPAServerColumnMetaData serverColumnMetaData = serverMetaData.getServerColumnMetaData(columnNames[i]);
			
			if (serverColumnMetaData == null) 
			{
				throw new DataSourceException("Column " + columnNames[i] + " unkown");
			}
			
			Path path = null;
			
			for (String attributeName : serverColumnMetaData.getJPAMappingType().getPathNavigation()) 
			{
				
				if (path == null) 
				{
					path = pFrom.get(attributeName);
				} 
				else 
				{
					path = path.get(attributeName);					
				}
				
			}	

			if (isAscending[i]) 
			{
				order[i] = criteriaBuilder.asc(path);		
			} 
			else 
			{
				order[i] = criteriaBuilder.desc(path);				
			}
		}
		
		pCriteriaQuery.orderBy(order);	
	}

}	// ConditionCriteriaMapper
