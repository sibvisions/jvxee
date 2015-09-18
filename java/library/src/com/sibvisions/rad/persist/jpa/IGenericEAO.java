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

import java.io.Serializable;
import java.util.Collection;

/**
 * The {@link IGenericEAO} defines the methods to access the database by
 * entities and primary keys.
 * 
 * @param <E> The Type of the Entity.
 * @param <PK> The Type of the Primary Key.
 *			
 * @author Stefan Wurm
 */
public interface IGenericEAO<E, PK extends Serializable>
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Method definitions
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Removes the values of this entity from the database.
	 * 
	 * @param entity the entity to delete.
	 */
	public void delete(E entity);
	
	/**
	 * Finds the entity for the given id.
	 * 
	 * @param id the primary key.
	 * @return the entity for this primary key.
	 */
	public E findById(PK id);
	
	/**
	 * Finds all entities.
	 * 
	 * @return a {@link Collection} of all entities.
	 */
	public Collection<E> findAll();
	
	/**
	 * Saves the values of the entity in the database.
	 * 
	 * @param newEntity the new entity to insert.
	 * @return the inserted entity.
	 */
	public E insert(E newEntity);
	
	/**
	 * Saves the values of the entity in the database.
	 * 
	 * @param entity the entity to update.
	 */
	public void update(E entity);
	
}	// IGenericEAO
