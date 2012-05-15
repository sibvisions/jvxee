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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.rad.model.datatype.BigDecimalDataType;
import javax.rad.model.datatype.BinaryDataType;
import javax.rad.model.datatype.BooleanDataType;
import javax.rad.model.datatype.IDataType;
import javax.rad.model.datatype.ObjectDataType;
import javax.rad.model.datatype.StringDataType;
import javax.rad.model.datatype.TimestampDataType;

public final class JPAStorageUtil
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	// TODO Check org.apache.commons.lang.ClassUtils: ClassUtils.wrapperToPrimitive(keyClass)
	private static final Set<Class>	WRAPPER_TYPES	= new HashSet();

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	static
	{

		WRAPPER_TYPES.add(Boolean.class);
		WRAPPER_TYPES.add(Character.class);
		WRAPPER_TYPES.add(Byte.class);
		WRAPPER_TYPES.add(Short.class);
		WRAPPER_TYPES.add(Integer.class);
		WRAPPER_TYPES.add(Long.class);
		WRAPPER_TYPES.add(Float.class);
		WRAPPER_TYPES.add(Double.class);
		WRAPPER_TYPES.add(String.class);
	}

	/**
	 * Invisible constructor because <code>JPAStorageUtil</code> is a utility
	 * class.
	 */
	private JPAStorageUtil()
	{
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public static IDataType getDataTypeIdentifierForJavaType(Class pJavaType)
	{
		if (pJavaType.isArray())
		{
			if (pJavaType == Byte[].class || pJavaType == byte[].class)
			{
				return new BinaryDataType();
			}
			else if (pJavaType == Character[].class || pJavaType == char[].class)
			{
				return new BinaryDataType();
			}
		}

		if (pJavaType == String.class 
			|| pJavaType == Character.class 
			|| pJavaType == char.class)
		{
			return new StringDataType();
		}
		else if (Number.class.isAssignableFrom(pJavaType) 
				 || pJavaType == byte.class 
				 || pJavaType == short.class 
				 || pJavaType == int.class 
				 || pJavaType == long.class 
				 || pJavaType == float.class 
				 || pJavaType == double.class)
		{
			return new BigDecimalDataType();

		}
		
		if (pJavaType == Boolean.class || pJavaType == boolean.class)
		{
			return new BooleanDataType();
		}
		else if (Date.class.isAssignableFrom(pJavaType))
		{
			return new TimestampDataType();
		}

		return new ObjectDataType();
	}

	public static boolean isPrimaryKeyAttribute(Attribute pAttribute, Class pEntityClass) throws Exception
	{
		Annotation[] annotations = getAnnotationsForAttribute(pAttribute, pEntityClass);

		for (Annotation annotation : annotations)
		{
			//TODO don't compare strings - use the class
			if (annotation.annotationType().getName().equals("javax.persistence.Id") 
				|| annotation.annotationType().getName().equals("javax.persistence.EmbeddedId"))
			{
				return true;
			}
		}

		return false;
	}

	public static boolean isPrimitiveOrWrapped(Class pClazz)
	{
		return pClazz.isPrimitive() || WRAPPER_TYPES.contains(pClazz);
	}

	public static String getSetterMethodNameForAttribute(Attribute pAttribute)
	{
		return "set" + pAttribute.getName().substring(0, 1).toUpperCase() + pAttribute.getName().substring(1);
	}

	public static String getGetterMethodNameForAttribute(Attribute pAttribute)
	{

		String methodName = "get" + pAttribute.getName().substring(0, 1).toUpperCase() + pAttribute.getName().substring(1);

		if (pAttribute.getJavaType() == Boolean.class || pAttribute.getJavaType() == boolean.class)
		{

			try
			{

				pAttribute.getDeclaringType().getJavaType().getMethod(methodName);

			}
			catch (NoSuchMethodException e)
			{
				methodName = "is" + pAttribute.getName().substring(0, 1).toUpperCase() + pAttribute.getName().substring(1);
			}

		}

		return methodName;
	}

	public static String getNameForAttribute(Attribute pAttribute)
	{
		return pAttribute.getName().toUpperCase();
	}

	public static String getLabelForAttribute(Attribute pAttribute)
	{
		return pAttribute.getName().substring(0, 1).toUpperCase() + pAttribute.getName().substring(1);
	}

	public static Annotation[] getAnnotationsForAttribute(Attribute pAttribute, Class pEntityClass) throws Exception
	{
		String member = pAttribute.getJavaMember().getName();

		if (member.startsWith("get") || member.startsWith("is"))
		{ 
			// The Annotation is defined on the getter-Methode
			return pEntityClass.getMethod(member).getAnnotations();
		}

		// The Annotation is defined on the field 
		return pEntityClass.getDeclaredField(member).getAnnotations(); 
	}

	public static Object getDefaultValueForAttribute(Attribute pAttribute, Class pEntityClass) throws Exception
	{
		// String member = attribute.getName();
		//
		// Field privateField = entityClass.getDeclaredField(member);
		// privateField.setAccessible(true);

		return pEntityClass.getMethod(getGetterMethodNameForAttribute(pAttribute)).invoke(pEntityClass.newInstance());
	}

	public static Class getTypeClassForAttribute(Attribute pAttribute) throws Exception
	{
		Field stringListField = pAttribute.getDeclaringType().getJavaType().getDeclaredField(pAttribute.getName());

		ParameterizedType stringListType = (ParameterizedType)stringListField.getGenericType();

		Class<?> typeClass = (Class<?>)stringListType.getActualTypeArguments()[0];

		return typeClass;
	}

}	// JPAStorageUtil
